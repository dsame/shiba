package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.metrics.ApplicationMetric;
import org.codeforamerica.shiba.metrics.ApplicationMetricsRepository;
import org.codeforamerica.shiba.metrics.Metrics;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Controller
public class PageController {
    private final ApplicationData applicationData;
    private final PagesConfiguration pagesConfiguration;
    private final Clock clock;
    private final ApplicationMetricsRepository repository;
    private final Metrics metrics;

    public PageController(
            PagesConfiguration pagesConfiguration,
            ApplicationData applicationData,
            Clock clock,
            ApplicationMetricsRepository repository,
            Metrics metrics
    ) {
        this.applicationData = applicationData;
        this.pagesConfiguration = pagesConfiguration;
        this.clock = clock;
        this.repository = repository;
        this.metrics = metrics;
    }

    @GetMapping("/pages/{pageName}/navigation")
    RedirectView navigation(
            @PathVariable String pageName,
            @RequestParam(required = false, defaultValue = "0") Integer option
    ) {
        PageWorkflowConfiguration pageWorkflow = this.pagesConfiguration.getPageWorkflow(pageName);
        InputDataMap inputDataMap = this.applicationData.getInputDataMap(pageWorkflow.getPageConfiguration().getName());
        String nextPageName = pageWorkflow.getNextPageName(inputDataMap, option);
        PageWorkflowConfiguration nextPage = this.pagesConfiguration.getPageWorkflow(nextPageName);

        if (nextPage.shouldSkip(applicationData.getPagesData())) {
            InputDataMap nextPageInputDataMap = this.applicationData.getInputDataMap(nextPage.getPageConfiguration().getName());
            return new RedirectView(String.format("/pages/%s", nextPage.getNextPageName(nextPageInputDataMap, option)));
        } else {
            return new RedirectView(String.format("/pages/%s", nextPageName));
        }
    }

    @GetMapping("/pages/{pageName}")
    ModelAndView getFormPage(
            @PathVariable String pageName,
            HttpServletResponse response
    ) {
        LandmarkPagesConfiguration landmarkPagesConfiguration = pagesConfiguration.getLandmarkPages();

        if (landmarkPagesConfiguration.isLandingPage(pageName)) {
            this.applicationData.clear();
            this.metrics.clear();
        } else if (landmarkPagesConfiguration.isStartTimerPage(pageName)) {
            this.metrics.setStartTimeOnce(clock.instant());
        }

        if (!landmarkPagesConfiguration.isTerminalPage(pageName) && this.applicationData.isSubmitted()) {
            return new ModelAndView(String.format("redirect:/pages/%s", landmarkPagesConfiguration.getTerminalPage()));
        } else if (!landmarkPagesConfiguration.isLandingPage(pageName) && metrics.getStartTime() == null) {
            return new ModelAndView(String.format("redirect:/pages/%s", landmarkPagesConfiguration.getLandingPages().get(0)));
        }

        response.addHeader("Cache-Control", "no-store");

        PageWorkflowConfiguration pageWorkflow = this.pagesConfiguration.getPageWorkflow(pageName);
        PageConfiguration pageConfiguration = pageWorkflow.getPageConfiguration();

        PagesData pagesData = applicationData.getPagesData();
        HashMap<String, Object> model = new HashMap<>(Map.of(
                "page", pageConfiguration,
                "pageName", pageName,
                "postTo", landmarkPagesConfiguration.isSubmitPage(pageName) ? "/submit" : "/pages/" + pageName,
                "pageTitle", pageWorkflow.resolveTitle(pagesData),
                "headerKey", pageWorkflow.resolveHeader(pagesData)
        ));

        if (landmarkPagesConfiguration.isTerminalPage(pageName)) {
            model.put("submissionTime", this.applicationData.getSubmissionTime());
        }

        String pageToRender;
        if (pageConfiguration.isStaticPage()) {
            pageToRender = pageName;
            Optional.ofNullable(pageWorkflow.getDatasources())
                    .map(datasources -> this.applicationData.getPagesData().getInputDataMapBy(datasources, pageDatasource -> pageDatasource.getPageName() + "_"))
                    .ifPresent(inputMap -> model.put("data", inputMap));
        } else {
            pageToRender = "formPage";
            model.put("data", pagesData.getPageDataOrDefault(pageName, pageConfiguration));
        }
        return new ModelAndView(pageToRender, model);
    }

    @PostMapping("/pages/{pageName}")
    ModelAndView postFormPage(
            @RequestBody(required = false) MultiValueMap<String, String> model,
            @PathVariable String pageName
    ) {
        PageWorkflowConfiguration pageWorkflow = pagesConfiguration.getPageWorkflow(pageName);

        PageConfiguration page = pageWorkflow.getPageConfiguration();
        InputDataMap inputDataMap = InputDataMap.fillOut(page, model);

        PagesData pagesData = applicationData.getPagesData();
        pagesData.putPage(pageWorkflow.getPageConfiguration().getName(), inputDataMap);

        return inputDataMap.isValid() ?
                new ModelAndView(String.format("redirect:/pages/%s/navigation", pageName)) :
                new ModelAndView("redirect:/pages/" + pageName);
    }

    @PostMapping("/submit")
    ModelAndView submitApplication(
            @RequestBody(required = false) MultiValueMap<String, String> model,
            Locale locale
    ) {
        LandmarkPagesConfiguration landmarkPagesConfiguration = this.pagesConfiguration.getLandmarkPages();
        String submitPage = landmarkPagesConfiguration.getSubmitPage();
        PageConfiguration page = pagesConfiguration.getPageWorkflow(submitPage).getPageConfiguration();

        InputDataMap inputDataMap = InputDataMap.fillOut(page, model);
        PagesData pagesData = applicationData.getPagesData();
        pagesData.putPage(submitPage, inputDataMap);

        if (inputDataMap.isValid()) {
            ApplicationMetric applicationMetric = new ApplicationMetric(Duration.between(metrics.getStartTime(), clock.instant()));
            repository.save(applicationMetric);

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", locale);
            this.applicationData.setSubmissionTime(dateTimeFormatter.format(ZonedDateTime.ofInstant(clock.instant(), ZoneId.of("UTC"))));
            return new ModelAndView(String.format("redirect:/pages/%s/navigation", submitPage));
        } else {
            PageWorkflowConfiguration pageWorkflow = pagesConfiguration.getPageWorkflow(submitPage);
            return new ModelAndView("formPage", Map.of(
                    "page", page,
                    "data", inputDataMap,
                    "pageName", submitPage,
                    "postTo", "/submit",
                    "pageTitle", pageWorkflow.resolveTitle(pagesData),
                    "headerKey", pageWorkflow.resolveHeader(pagesData)
            ));
        }
    }
}

package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.ApplicationFactory;
import org.codeforamerica.shiba.ApplicationRepository;
import org.codeforamerica.shiba.ConfirmationData;
import org.codeforamerica.shiba.metrics.Metrics;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.LandmarkPagesConfiguration;
import org.codeforamerica.shiba.pages.config.PageConfiguration;
import org.codeforamerica.shiba.pages.config.PageWorkflowConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.Clock;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Controller
public class PageController {
    private final ApplicationData applicationData;
    private final ApplicationConfiguration applicationConfiguration;
    private final Clock clock;
    private final Metrics metrics;
    private final ApplicationRepository applicationRepository;
    private final ApplicationFactory applicationFactory;
    private final ConfirmationData confirmationData;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MessageSource messageSource;

    public PageController(
            ApplicationConfiguration applicationConfiguration,
            ApplicationData applicationData,
            Clock clock,
            Metrics metrics,
            ApplicationRepository applicationRepository,
            ApplicationFactory applicationFactory,
            ConfirmationData confirmationData,
            ApplicationEventPublisher applicationEventPublisher,
            MessageSource messageSource) {
        this.applicationData = applicationData;
        this.applicationConfiguration = applicationConfiguration;
        this.clock = clock;
        this.metrics = metrics;
        this.applicationRepository = applicationRepository;
        this.applicationFactory = applicationFactory;
        this.confirmationData = confirmationData;
        this.applicationEventPublisher = applicationEventPublisher;
        this.messageSource = messageSource;
    }

    @GetMapping("/")
    ModelAndView getRoot() {
        return new ModelAndView("forward:/pages/" + applicationConfiguration.getLandmarkPages().getLandingPages().get(0));
    }

    @GetMapping("/privacy")
    String getPrivacyPolicy() {
        return "privacyPolicy";
    }

    @GetMapping("/pages/{pageName}/navigation")
    RedirectView navigation(
            @PathVariable String pageName,
            @RequestParam(required = false, defaultValue = "0") Integer option
    ) {
        PageWorkflowConfiguration pageWorkflow = this.applicationConfiguration.getPageWorkflow(pageName);
        PagesData pagesData = this.applicationData.getPagesData();
        String nextPageName = applicationData.getNextPageName(pageWorkflow, option);
        PageWorkflowConfiguration nextPage = this.applicationConfiguration.getPageWorkflow(nextPageName);

        if (pagesData.shouldSkip(nextPage)) {
            pagesData.remove(nextPage.getPageConfiguration().getName());
            return new RedirectView(String.format("/pages/%s", applicationData.getNextPageName(nextPage, option)));
        } else {
            return new RedirectView(String.format("/pages/%s", nextPageName));
        }
    }

    @GetMapping("/pages/{pageName}")
    ModelAndView getPage(
            @PathVariable String pageName,
            HttpServletResponse response,
            HttpSession httpSession
    ) {
        LandmarkPagesConfiguration landmarkPagesConfiguration = applicationConfiguration.getLandmarkPages();

        if (landmarkPagesConfiguration.isLandingPage(pageName)) {
            httpSession.invalidate();
        } else if (landmarkPagesConfiguration.isStartTimerPage(pageName)) {
            this.metrics.setStartTimeOnce(clock.instant());
        }

        if (!landmarkPagesConfiguration.isTerminalPage(pageName) && confirmationData.getId() != null) {
            return new ModelAndView(String.format("redirect:/pages/%s", landmarkPagesConfiguration.getTerminalPage()));
        } else if (!landmarkPagesConfiguration.isLandingPage(pageName) && metrics.getStartTime() == null) {
            return new ModelAndView(String.format("redirect:/pages/%s", landmarkPagesConfiguration.getLandingPages().get(0)));
        }

        response.addHeader("Cache-Control", "no-store");

        PageWorkflowConfiguration pageWorkflow = this.applicationConfiguration.getPageWorkflow(pageName);
        PageConfiguration pageConfiguration = pageWorkflow.getPageConfiguration();

        PagesData pagesData;
        if (pageWorkflow.getGroupName() != null) {
            String groupName = pageWorkflow.getGroupName();
            if (applicationConfiguration.getPageGroups().get(groupName).getStartPage().equals(pageName)) {
                pagesData = applicationData.getIncompleteIterations().getOrDefault(groupName, new PagesData());
            } else {
                pagesData = applicationData.getIncompleteIterations().get(groupName);
            }

            if (pagesData == null) {
                String redirectPage = applicationConfiguration.getPageGroups().get(pageWorkflow.getGroupName()).getRedirectPage();
                return new ModelAndView(String.format("redirect:/pages/%s", redirectPage));
            }
        } else {
            pagesData = applicationData.getPagesData();
        }

        PageTemplate pageTemplate = pagesData.evaluate(pageWorkflow);

        HashMap<String, Object> model = new HashMap<>(Map.of(
                "page", pageTemplate,
                "pageName", pageName,
                "postTo", landmarkPagesConfiguration.isSubmitPage(pageName) ? "/submit" : "/pages/" + pageName
        ));

        if (landmarkPagesConfiguration.isTerminalPage(pageName)) {
            Application application = applicationRepository.find(confirmationData.getId());
            model.put("applicationId", application.getId());
            model.put("submissionTime", application.getCompletedAt().withZoneSameInstant(ZoneId.of("America/Chicago")));
            model.put("county", application.getCounty());
            model.put("sentiment", application.getSentiment());
            model.put("feedbackText", application.getFeedback());
        }

        String pageToRender;
        if (pageConfiguration.isStaticPage()) {
            pageToRender = pageName;
            model.put("data", pagesData.getDatasourcePagesBy(pageWorkflow.getDatasources()));
            if (pageWorkflow.hasRequiredSubworkflows(applicationData)) {
                model.put("subworkflows", pageWorkflow.getSubworkflows(applicationData));
            } else {
                return new ModelAndView("redirect:/pages/" + pageWorkflow.getDataMissingRedirect());
            }
        } else {
            pageToRender = "formPage";
            model.put("pageDatasources", pagesData.getDatasourcePagesBy(pageWorkflow.getDatasources()));
            model.put("data", pagesData.getPageDataOrDefault(pageTemplate.getName(), pageConfiguration));
        }
        return new ModelAndView(pageToRender, model);
    }

    @PostMapping("/groups/{groupName}/delete")
    RedirectView deleteGroup(@PathVariable String groupName) {
        this.applicationData.getSubworkflows().remove(groupName);
        String startPage = applicationConfiguration.getPageGroups().get(groupName).getRestartPage();
        return new RedirectView("/pages/" + startPage);
    }

    @PostMapping("/groups/{groupName}/{iteration}/delete")
    ModelAndView deleteIteration(
            @PathVariable String groupName,
            @PathVariable int iteration,
            @RequestHeader("referer") String referer
    ) {
        if (this.applicationData.getSubworkflows().get(groupName).size() == 1) {
            String redirectPage = applicationConfiguration.getPageGroups().get(groupName).getNoDataRedirectPage();

            return new ModelAndView("redirect:/pages/" + redirectPage);
        }
        this.applicationData.getSubworkflows().get(groupName).remove(iteration);
        return new ModelAndView("redirect:" + referer);
    }

    @PostMapping("/pages/{pageName}")
    ModelAndView postFormPage(
            @RequestBody(required = false) MultiValueMap<String, String> model,
            @PathVariable String pageName
    ) {
        PageWorkflowConfiguration pageWorkflow = applicationConfiguration.getPageWorkflow(pageName);

        PageConfiguration page = pageWorkflow.getPageConfiguration();
        PageData pageData = PageData.fillOut(page, model);

        PagesData pagesData;
        if (pageWorkflow.getGroupName() != null) {
            String groupName = pageWorkflow.getGroupName();
            if (applicationConfiguration.getPageGroups().get(groupName).getStartPage().equals(page.getName())) {
                applicationData.getIncompleteIterations().put(groupName, new PagesData());
            }
            pagesData = applicationData.getIncompleteIterations().get(groupName);
        } else {
            pagesData = applicationData.getPagesData();
        }

        pagesData.putPage(page.getName(), pageData);

        if (pageData.isValid() &&
                pageWorkflow.getGroupName() != null &&
                applicationConfiguration.getPageGroups().get(pageWorkflow.getGroupName()).getCompletePages().contains(page.getName())
        ) {
            String groupName = pageWorkflow.getGroupName();
            applicationData.getSubworkflows()
                    .addIteration(groupName, applicationData.getIncompleteIterations().remove(groupName));
        }

        return pageData.isValid() ?
                new ModelAndView(String.format("redirect:/pages/%s/navigation", pageName)) :
                new ModelAndView("redirect:/pages/" + pageName);
    }

    @PostMapping("/submit")
    ModelAndView submitApplication(
            @RequestBody(required = false) MultiValueMap<String, String> model
    ) {
        LandmarkPagesConfiguration landmarkPagesConfiguration = this.applicationConfiguration.getLandmarkPages();
        String submitPage = landmarkPagesConfiguration.getSubmitPage();
        PageConfiguration page = applicationConfiguration.getPageWorkflow(submitPage).getPageConfiguration();

        PageData pageData = PageData.fillOut(page, model);
        PagesData pagesData = applicationData.getPagesData();
        pagesData.putPage(submitPage, pageData);

        if (pageData.isValid()) {
            String id = applicationRepository.getNextId();
            Application application = applicationFactory.newApplication(id, applicationData, metrics);
            confirmationData.setId(application.getId());
            applicationRepository.save(application);
            applicationEventPublisher.publishEvent(new ApplicationSubmittedEvent(application.getId()));

            return new ModelAndView(String.format("redirect:/pages/%s/navigation", submitPage));
        } else {
            return new ModelAndView("redirect:/pages/" + submitPage);
        }
    }

    @PostMapping("/submit-feedback")
    RedirectView submitFeedback(Feedback feedback,
                                RedirectAttributes redirectAttributes,
                                Locale locale) {
        String terminalPage = applicationConfiguration.getLandmarkPages().getTerminalPage();
        if (confirmationData.getId() == null) {
            return new RedirectView("/pages/" + terminalPage);
        }
        String message = messageSource.getMessage(feedback.getMessageKey(), null, locale);
        if (feedback.isInvalid()) {
            redirectAttributes.addFlashAttribute("feedbackFailure", message);
            return new RedirectView("/pages/" + terminalPage);
        }
        redirectAttributes.addFlashAttribute("feedbackSuccess", message);
        Application application = applicationRepository.find(confirmationData.getId());
        Application updatedApplication = application.addFeedback(feedback);
        applicationRepository.save(updatedApplication);
        return new RedirectView("/pages/" + terminalPage);
    }

}

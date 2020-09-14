package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ExpeditedEligibilityParser {
    private final ExpeditedEligibilityConfiguration expeditedEligibilityConfiguration;
    private final GrossMonthlyIncomeParser grossMonthlyIncomeParser;

    public ExpeditedEligibilityParser(ExpeditedEligibilityConfiguration expeditedEligibilityConfiguration, GrossMonthlyIncomeParser grossMonthlyIncomeParser) {
        this.expeditedEligibilityConfiguration = expeditedEligibilityConfiguration;
        this.grossMonthlyIncomeParser = grossMonthlyIncomeParser;
    }

    public Optional<ExpeditedEligibilityParameters> parse(ApplicationData applicationData) {
        PagesData pagesData = applicationData.getPagesData();

        List<String> requiredPages = expeditedEligibilityConfiguration.values().stream()
                .filter(PageInputCoordinates::getRequired)
                .map(PageInputCoordinates::getPageName)
                .collect(Collectors.toList());
        if (!pagesData.keySet().containsAll(requiredPages)) {
            return Optional.empty();
        }

        double assets = getDouble(pagesData, expeditedEligibilityConfiguration.get("assets"));
        double income;

        List<JobIncomeInformation> jobIncomeInformationList = grossMonthlyIncomeParser.parse(applicationData);
        if (jobIncomeInformationList.isEmpty()) {
            income = getDouble(pagesData, expeditedEligibilityConfiguration.get("income"));
        } else {
            income = jobIncomeInformationList.stream().reduce(
                    0.0,
                    (total, jobIncomeInformation) -> total + jobIncomeInformation.grossMonthlyIncome(),
                    Double::sum
            );
        }
        double housingCosts = getDouble(pagesData, expeditedEligibilityConfiguration.get("housingCosts"));
        boolean isMigrantWorker = Boolean.parseBoolean(pagesData.getPage(expeditedEligibilityConfiguration.get("migrantWorker").getPageName())
                .get(expeditedEligibilityConfiguration.get("migrantWorker").getInputName()).getValue().get(0));
        @NotNull List<String> utilityExpensesSelections = pagesData.getPage(expeditedEligibilityConfiguration.get("utilityExpensesSelections").getPageName())
                .get(expeditedEligibilityConfiguration.get("utilityExpensesSelections").getInputName()).getValue();
        return Optional.of(new ExpeditedEligibilityParameters(assets, income, isMigrantWorker, housingCosts, utilityExpensesSelections));
    }

    private static double getDouble(PagesData pagesData, PageInputCoordinates pageInputCoordinates) {
        try {
            return Double.parseDouble(
                    Optional.ofNullable(pagesData.get(pageInputCoordinates.getPageName()))
                            .map(inputDataMap -> inputDataMap.get(pageInputCoordinates.getInputName()).getValue().get(0))
                            .orElse(pageInputCoordinates.getDefaultValue())
            );
        } catch (NumberFormatException e) {
            return Double.parseDouble(pageInputCoordinates.getDefaultValue());
        }
    }
}

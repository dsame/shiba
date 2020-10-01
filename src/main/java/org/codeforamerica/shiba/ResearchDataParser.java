package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.parsers.TotalIncomeParser;
import org.codeforamerica.shiba.output.TotalIncome;
import org.codeforamerica.shiba.output.TotalIncomeCalculator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Component
public class ResearchDataParser {
    private final TotalIncomeCalculator totalIncomeCalculator;
    private final TotalIncomeParser totalIncomeParser;

    public ResearchDataParser(TotalIncomeCalculator totalIncomeCalculator, TotalIncomeParser totalIncomeParser) {
        this.totalIncomeCalculator = totalIncomeCalculator;
        this.totalIncomeParser = totalIncomeParser;
    }

    public ResearchData parse(ApplicationData applicationData) {
        PagesData pagesData = applicationData.getPagesData();
        Optional<PageData> languagePreferencesOptional = Optional.ofNullable(pagesData.getPage("languagePreferences"));
        Optional<PageData> personalInfoOptional = Optional.ofNullable(pagesData.getPage("personalInfo"));
        Optional<PageData> contactInfoOptional = Optional.ofNullable(pagesData.getPage("contactInfo"));
        Optional<PageData> homeAddressOptional = Optional.ofNullable(pagesData.getPage("homeAddress"));
        Optional<PageData> homeExpensesAmountOptional = Optional.ofNullable(pagesData.getPage("homeExpensesAmount"));
        Optional<PageData> currentlyWorkingOptional = Optional.ofNullable(pagesData.getPage("employmentStatus"));
        Optional<PageData> liveAloneOptional = Optional.ofNullable(pagesData.getPage("doYouLiveAlone"));
        TotalIncome totalIncome = totalIncomeParser.parse(applicationData);
        Optional<PageData> expeditedExpensesOptional = Optional.ofNullable(pagesData.getPage("expeditedExpenses"));
        Optional<PageData> homeExpensesOptional = Optional.ofNullable(pagesData.getPage("homeExpenses"));
        Optional<PageData> programsOptional = Optional.ofNullable(pagesData.getPage("choosePrograms"));


        return ResearchData.builder()
                .spokenLanguage(languagePreferencesOptional.map(languagePrefs -> languagePrefs.get("spokenLanguage").getValue(0)).orElse(null))
                .writtenLanguage(languagePreferencesOptional.map(languagePreferences -> languagePreferences.get("writtenLanguage").getValue(0)).orElse(null))
                .sex(personalInfoOptional.map(pInfo -> pInfo.get("sex").getValue(0)).orElse(null))
                .firstName(personalInfoOptional.map(pInfo -> pInfo.get("firstName").getValue(0)).orElse(null))
                .lastName(personalInfoOptional.map(pInfo -> pInfo.get("lastName").getValue(0)).orElse(null))
                .dateOfBirth(personalInfoOptional.map(pInfo -> Date.valueOf(pInfo.get("dateOfBirth").getValue(0))).orElse(null))
                .enteredSsn(personalInfoOptional.map(pInfo -> !(String.join("", pInfo.get("ssn").getValue())).isBlank()).orElse(null))
                .phoneNumber(contactInfoOptional.map(contactInformation -> contactInformation.get("phoneNumber").getValue(0)).orElse(null))
                .email(contactInfoOptional.map(contactInformation -> contactInformation.get("email").getValue(0)).orElse(null))
                .phoneOptIn(contactInfoOptional.map(contactInformation -> contactInformation.get("phoneOrEmail").getValue().contains("TEXT")).orElse(null))
                .emailOptIn(contactInfoOptional.map(contactInformation -> contactInformation.get("phoneOrEmail").getValue().contains("EMAIL")).orElse(null))
                .zipCode(homeAddressOptional.map(homeAddr -> homeAddr.get("zipCode").getValue(0)).orElse(null))
                .snap(programsOptional.map(c -> c.get("programs").getValue().contains("SNAP")).orElse(null))
                .cash(programsOptional.map(c -> c.get("programs").getValue().contains("CASH")).orElse(null))
                .housing(programsOptional.map(c -> c.get("programs").getValue().contains("GRH")).orElse(null))
                .emergency(programsOptional.map(c -> c.get("programs").getValue().contains("EA")).orElse(null))
                .liveAlone(liveAloneOptional.map(liveAlone -> liveAlone.get("liveAlone").getValue(0).contains("true")).orElse(null))
                .moneyMadeLast30Days(totalIncomeCalculator.calculate(totalIncome))
                .homeExpensesAmount(homeExpensesAmountOptional.map(homeExpsAmount -> Double.valueOf(homeExpsAmount.get("homeExpensesAmount").getValue(0))).orElse(null))
                .payRentOrMortgage(getPayRentOrMortgage(homeExpensesOptional, expeditedExpensesOptional))
                .payRentOrMortgage(expeditedExpensesOptional.map(expeditedExpenses -> Boolean.valueOf(expeditedExpenses.get("payRentOrMortgage").getValue(0))).orElse(null))
                .areYouWorking(currentlyWorkingOptional.map(currentlyWrking -> Boolean.valueOf(currentlyWrking.get("areYouWorking").getValue(0))).orElse(null))
                .build();
    }

    private Boolean getPayRentOrMortgage(Optional<PageData> homeExpensesOptional, Optional<PageData> expeditedExpensesOptional) {
        return homeExpensesOptional.map(homeExpenses -> {
            List<String> housingExpenses = homeExpenses.get("homeExpenses").getValue();
            return housingExpenses.contains("MORTGAGE") || housingExpenses.contains("RENT");
        }).or(() -> {
            return expeditedExpensesOptional.map(expeditedExpenses -> {
                return expeditedExpenses.get("payRentOrMortgage").getValue().contains("true");
            });
        }).orElse(null);
    }
}

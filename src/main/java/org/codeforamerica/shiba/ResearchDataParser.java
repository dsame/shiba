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
        TotalIncome totalIncome = totalIncomeParser.parse(applicationData);
        List<String> chosenPrograms = pagesData.getPage("choosePrograms").get("programs").getValue();
        Optional<PageData> programsOptional = Optional.ofNullable(pagesData.getPage("choosePrograms"));


        return ResearchData.builder()
                .spokenLanguage(languagePreferencesOptional.map(languagePrefs -> languagePrefs.get("spokenLanguage").getValue(0)).orElse(null))
                .writtenLanguage(languagePreferencesOptional.map(languagePreferences -> languagePreferences.get("writtenLanguage").getValue(0)).orElse(null))
                .sex(personalInfoOptional.map(pInfo -> pInfo.get("sex").getValue(0)).orElse(null))
                .firstName(personalInfoOptional.map(pInfo -> pInfo.get("firstName").getValue(0)).orElse(null))
                .lastName(personalInfoOptional.map(pInfo -> pInfo.get("lastName").getValue(0)).orElse(null))
                .dateOfBirth(personalInfoOptional.map(pInfo -> Date.valueOf(pInfo.get("dateOfBirth").getValue(0))).orElse(null))
                .enteredSsn(!String.join("", personalInfoOptional.map(pInfo -> pInfo.get("ssn").getValue()).orElse(List.of(""))).isBlank())
                .phoneNumber(contactInfoOptional.map(contactInformation -> contactInformation.get("phoneNumber").getValue(0)).orElse(null))
                .email(contactInfoOptional.map(contactInformation -> contactInformation.get("email").getValue(0)).orElse(null))
                .phoneOptIn(contactInfoOptional.map(contactInformation -> contactInformation.get("phoneOrEmail").getValue().contains("TEXT")).orElse(null))
                .emailOptIn(contactInfoOptional.map(contactInformation -> contactInformation.get("phoneOrEmail").getValue().contains("EMAIL")).orElse(null))
                .zipCode(homeAddressOptional.map(homeAddr -> homeAddr.get("zipCode").getValue(0)).orElse(null))
                .snap(chosenProgramsOptional.map(chosenPrograms -> chosenPrograms.contains("SNAP")).orElse(null))
                .cash(chosenProgramsOptional.map(chosenPrograms -> chosenPrograms.contains("CASH")).orElse(null))
                .housing(chosenProgramsOptional.map(chosenPrograms -> chosenPrograms.contains("GRH")).orElse(null))
                .emergency(chosenProgramsOptional.map(chosenPrograms -> chosenPrograms.contains("EA")).orElse(null))
                .liveAlone(pagesData.get("doYouLiveAlone").get("liveAlone").getValue(0).contains("true"))
                .moneyMadeLast30Days(totalIncomeCalculator.calculate(totalIncome))
                .homeExpensesAmount(Double.valueOf(homeExpensesAmountOptional.map(homeExpsAmount -> homeExpsAmount.get("homeExpensesAmount").getValue(0)).orElse(null)))
                .areYouWorking(Boolean.valueOf(currentlyWorkingOptional.map(currentlyWrking -> currentlyWrking.get("areYouWorking").getValue(0)).orElse(null)))
                .build();
    }
}

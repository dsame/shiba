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
        PageData languagePreferences = pagesData.getPage("languagePreferences");
        Optional<PageData> languagePreferencesOptional = Optional.ofNullable(languagePreferences);
        PageData personalInfo = pagesData.getPage("personalInfo");
        Optional<PageData> personalInfoOptional = Optional.ofNullable(personalInfo);
        PageData contactInfo = pagesData.getPage("contactInfo");
        Optional<PageData> contactInfoOptional = Optional.ofNullable(contactInfo);
        PageData homeAddress = pagesData.getPage("homeAddress");
        Optional<PageData> homeAddressOptional = Optional.ofNullable(homeAddress);
        PageData programs = pagesData.getPage("choosePrograms");
        Optional<PageData> programsOptional = Optional.ofNullable(programs);
        String homeExpensesAmount = pagesData.getPage("homeExpensesAmount").get("homeExpensesAmount").getValue(0);
        String currentlyWorking = pagesData.getPage("employmentStatus").get("areYouWorking").getValue(0);
        TotalIncome totalIncome = totalIncomeParser.parse(applicationData);
        List<String> chosenPrograms = programs.get("programs").getValue();


        return ResearchData.builder()
                .spokenLanguage(languagePreferencesOptional.map(languagePrefs -> languagePrefs.get("spokenLanguage").getValue(0)).orElse(null))
                .writtenLanguage(languagePreferences.get("writtenLanguage").getValue(0))
                .sex(personalInfoOptional.map(pInfo -> pInfo.get("sex").getValue(0)).orElse(null))
                .firstName(personalInfoOptional.map(pInfo -> pInfo.get("firstName").getValue(0)).orElse(null))
                .lastName(personalInfoOptional.map(pInfo -> pInfo.get("lastName").getValue(0)).orElse(null))
                .dateOfBirth(personalInfoOptional.map(pInfo -> Date.valueOf(pInfo.get("dateOfBirth").getValue(0))).orElse(null))
                .enteredSsn(!String.join("", personalInfoOptional.map(pInfo -> pInfo.get("ssn").getValue()).orElse(List.of(""))).isBlank())
                .phoneNumber(contactInfo.get("phoneNumber").getValue(0))
                .email(contactInfo.get("email").getValue(0))
                .phoneOptIn(contactInfo.get("phoneOrEmail").getValue().contains("TEXT"))
                .emailOptIn(contactInfo.get("phoneOrEmail").getValue().contains("EMAIL"))
                .zipCode(homeAddress.get("zipCode").getValue(0))
                .snap(chosenPrograms.contains("SNAP"))
                .cash(chosenPrograms.contains("CASH"))
                .housing(chosenPrograms.contains("GRH"))
                .emergency(chosenPrograms.contains("EA"))
                .liveAlone(pagesData.get("doYouLiveAlone").get("liveAlone").getValue(0).contains("true"))
                .moneyMadeLast30Days(totalIncomeCalculator.calculate(totalIncome))
                .homeExpensesAmount(Double.valueOf(homeExpensesAmount))
                .areYouWorking(Boolean.valueOf(currentlyWorking))
                .build();
    }
}

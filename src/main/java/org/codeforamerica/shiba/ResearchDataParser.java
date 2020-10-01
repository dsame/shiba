package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.parsers.TotalIncomeParser;
import org.codeforamerica.shiba.output.TotalIncomeCalculator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

import java.sql.Date;

@Component
public class ResearchDataParser {
    private final TotalIncomeCalculator totalIncomeCalculator;
    private final TotalIncomeParser totalIncomeParser;

    public ResearchDataParser(TotalIncomeCalculator totalIncomeCalculator, TotalIncomeParser totalIncomeParser) {
        this.totalIncomeCalculator = totalIncomeCalculator;
        this.totalIncomeParser = totalIncomeParser;
    }

    public ResearchData parse(ApplicationData applicationData) {
        PagesData pd = applicationData.getPagesData();
        PageData languagePreferences = pd.getPage("languagePreferences");
        PageData personalInfo = pd.getPage("personalInfo");
        PageData contactInfo = pd.getPage("contactInfo");
        return ResearchData.builder()
                .spokenLanguage(languagePreferences.get("spokenLanguage").getValue(0))
                .writtenLanguage(languagePreferences.get("writtenLanguage").getValue(0))
                .sex(personalInfo.get("sex").getValue(0))
                .firstName(personalInfo.get("firstName").getValue(0))
                .lastName(personalInfo.get("lastName").getValue(0))
                .dateOfBirth(Date.valueOf(personalInfo.get("dateOfBirth").getValue(0)))
                .phoneNumber(contactInfo.get("phoneNumber").getValue(0))
                .email(contactInfo.get("email").getValue(0))
                .phoneOptIn(determineContactOptIn(contactInfo, ""))
                .emailOptIn(determineContactOptIn(contactInfo, ""))
                .zipCode("")
                .snap(true)
                .cash(true)
                .housing(true)
                .emergency(true)
                .liveAlone(false)
                .phoneNumber("6038791111")
                .email("fake@email.com")
                .homeExpensesAmount(111.0)
                .areYouWorking(true)
                .build();
    }
}

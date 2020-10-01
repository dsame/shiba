package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.parsers.TotalIncomeParser;
import org.codeforamerica.shiba.output.TotalIncome;
import org.codeforamerica.shiba.output.TotalIncomeCalculator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResearchDataParserTest {
    TotalIncomeCalculator totalIncomeCalculator = mock(TotalIncomeCalculator.class);
    TotalIncomeParser totalIncomeParser = mock(TotalIncomeParser.class);

    ResearchDataParser researchDataParser = new ResearchDataParser(
        totalIncomeCalculator, totalIncomeParser
    );
    private PagesDataBuilder pagesDataBuilder;

    @Test
    void shouldParseResearchData() {
        ApplicationData applicationData = new ApplicationData();

        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("languagePreferences", Map.of(
                        "spokenLanguage", List.of("English"),
                        "writtenLanguage", List.of("Spanish")
                )),
                new PageDataBuilder("personalInfo", Map.of(
                        "sex", List.of("female"),
                        "firstName", List.of("Person"),
                        "lastName", List.of("Fake"),
                        "dateOfBirth", List.of("10/04/2020")
                )),
                new PageDataBuilder("contactInfo", Map.of(
                        "phoneNumber", List.of("6038791111"),
                        "email", List.of("fake@email.com"),
                        "phoneOrEmail", List.of("TEXT")
                )),
                new PageDataBuilder("homeAddress", Map.of(
                        "zipCode", List.of("1111-1111")
                )),
                new PageDataBuilder("choosePrograms", Map.of(
                        "programs", List.of("SNAP", "CASH", "GRH", "EA")
                )),
                new PageDataBuilder("doYouLiveAlone", Map.of(
                        "liveAlone", List.of("false")
                )),
                new PageDataBuilder("contactInfo", Map.of(
                        "phoneNumber", List.of("6038791111"),
                        "email", List.of("fake@email.com"),
                        "phoneOrEmail", List.of("TEXT", "EMAIL")
                )),
                new PageDataBuilder("homeExpenseAmount", Map.of(
                        "homeExpenseAmount", List.of("111")
                )),
                new PageDataBuilder("employmentStatus", Map.of(
                        "areYouWorking", List.of("true")
                ))
        ));
        applicationData.setPagesData(pagesData);

        when(totalIncomeCalculator.calculate(any())).thenReturn(123.0);
        when(totalIncomeParser.parse(any())).thenReturn(new TotalIncome(789.0, emptyList()));

        ResearchData researchData = researchDataParser.parse(applicationData);

        ResearchData expectedResearchData = ResearchData.builder()
                .spokenLanguage("English")
                .writtenLanguage("Spanish")
                .sex("female")
                .firstName("Person")
                .lastName("Fake")
                .dateOfBirth(Date.valueOf("10/04/2020"))
                .phoneNumber("")
                .email("")
                .phoneOptIn(true)
                .emailOptIn(false)
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
        assertThat(researchData).isEqualTo(expectedResearchData);
    }
}
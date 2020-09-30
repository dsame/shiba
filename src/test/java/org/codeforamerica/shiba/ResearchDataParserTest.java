package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ResearchDataParserTest {
    ResearchDataParser researchDataParser = new ResearchDataParser();
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
                        "phoneOrEmail", List.of("TEXT", "EMAIL")
                )),
                new PageDataBuilder("homeAddress", Map.of(
                        "zipCode", List.of("1111-1111")
                )),
                new PageDataBuilder("choosePrograms", Map.of(
                        "programs", List.of("SNAP", "CASH", "GRH", "EA")
                )),
                new PageDataBuilder("doYouLiveAlone", Map.of(
                        "liveAlone", List.of("true")
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
                )),


        ));
        applicationData.setPagesData(pagesData);

        ResearchData researchData = researchDataParser.parse(applicationData);

        ResearchData expectedResearchData = ResearchData.builder()
                .spokenLanguage("English")
                .writtenLanguage("Spanish")
                .sex("female")
                .firstName("Person")
                .lastName("Fake")
                .build();
        assertThat(researchData).isEqualTo(expectedResearchData);
    }
}
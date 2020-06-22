package org.codeforamerica.shiba;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OneToOneApplicationInputsMapperTest {
    PagesConfiguration pagesConfiguration = new PagesConfiguration();
    PagesData data = new PagesData();
    OneToOneApplicationInputsMapper oneToOneApplicationInputsMapper = new OneToOneApplicationInputsMapper(pagesConfiguration);

    @Test
    void shouldProduceAnApplicationInputForAFormInput() {
        FormInput input1 = new FormInput();
        String input1Name = "input 1";
        input1.name = input1Name;
        input1.type = FormInputType.TEXT;

        PageConfiguration page = new PageConfiguration();
        page.setInputs(List.of(input1));
        String pageName = "screen1";
        pagesConfiguration.put(pageName, page);

        List<String> input1Value = List.of("input1Value");
        data.putPage(pageName, new FormData(Map.of(input1Name, new InputData(Validation.NONE, input1Value))));

        List<ApplicationInput> applicationInputs = oneToOneApplicationInputsMapper.map(data);

        assertThat(applicationInputs).contains(
                new ApplicationInput(pageName, input1Value, input1Name, ApplicationInputType.SINGLE_VALUE)
        );
    }

    @Test
    void shouldIncludeApplicationInputsForFollowups() {
        FormInput input2 = new FormInput();
        String input2Name = "input 2";
        input2.name = input2Name;
        input2.type = FormInputType.TEXT;

        FormInput input3 = new FormInput();
        String input3Name = "input 3";
        input3.name = input3Name;
        input3.type = FormInputType.TEXT;

        input2.followUps = List.of(input3);

        PageConfiguration page = new PageConfiguration();
        page.setInputs(List.of(input2));
        String pageName = "screen1";
        pagesConfiguration.put(pageName, page);

        List<String> input2Value = List.of("input2Value");
        List<String> input3Value = List.of("input3Value");
        data.putPage(pageName, new FormData(Map.of(
                input2Name, new InputData(Validation.NONE, input2Value),
                input3Name, new InputData(Validation.NONE, input3Value)
        )));

        List<ApplicationInput> applicationInputs = oneToOneApplicationInputsMapper.map(data);

        assertThat(applicationInputs).contains(
                new ApplicationInput(pageName, input2Value, input2Name, ApplicationInputType.SINGLE_VALUE),
                new ApplicationInput(pageName, input3Value, input3Name, ApplicationInputType.SINGLE_VALUE)
        );
    }

}
package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.output.*;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ThirtyDayIncomeMapperTest {
    private final TotalIncomeCalculator totalIncomeCalculator = mock(TotalIncomeCalculator.class);
    private final TotalIncomeParser totalIncomeParser = mock(TotalIncomeParser.class);

    @Test
    void returnsCalculatedTotalIncome() {
        ThirtyDayIncomeMapper mapper = new ThirtyDayIncomeMapper(totalIncomeCalculator, totalIncomeParser);

        ApplicationData appData = new ApplicationData();
        Application application = Application.builder().applicationData(appData).build();

        List<JobIncomeInformation> jobIncomeInformationList = List.of();
        Double thirtyDayIncome = 1.0;
        when(totalIncomeParser.parse(appData)).thenReturn(new TotalIncome(thirtyDayIncome, jobIncomeInformationList));
        when(totalIncomeCalculator.calculate(new TotalIncome(thirtyDayIncome, jobIncomeInformationList))).thenReturn(111.0);

        assertThat(mapper.map(application, Recipient.CLIENT)).isEqualTo(List.of(
                new ApplicationInput(
                        "totalIncome",
                        "thirtyDayIncome",
                        List.of("111.0"),
                        ApplicationInputType.SINGLE_VALUE
                )
        ));
    }
}
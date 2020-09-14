package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GrossMonthlyIncomeMapperTest {
    private final ApplicationData applicationData = new ApplicationData();
    private final GrossMonthlyIncomeParser grossMonthlyIncomeParser = mock(GrossMonthlyIncomeParser.class);
    private final GrossMonthlyIncomeMapper grossMonthlyIncomeMapper = new GrossMonthlyIncomeMapper(grossMonthlyIncomeParser);

    @Test
    void shouldCalculateGrossMonthlyIncomeForEachHourlyJob() {
        Application application = new Application("someId", ZonedDateTime.now(), applicationData, County.OTHER, "");

        JobIncomeInformation mockInformation = mock(JobIncomeInformation.class);
        JobIncomeInformation mockInformation1 = mock(JobIncomeInformation.class);
        when(grossMonthlyIncomeParser.parse(applicationData)).thenReturn(List.of(mockInformation,mockInformation1));
        when(mockInformation.getIteration()).thenReturn(0);
        when(mockInformation.grossMonthlyIncome()).thenReturn(1440.0);
        when(mockInformation1.getIteration()).thenReturn(1);
        when(mockInformation1.grossMonthlyIncome()).thenReturn(1080.0);
        List<ApplicationInput> applicationInputs = grossMonthlyIncomeMapper.map(application, Recipient.CLIENT);

        assertThat(applicationInputs).contains(
                new ApplicationInput("employee", "grossMonthlyIncome", List.of("1440.0"), SINGLE_VALUE, 0),
                new ApplicationInput("employee", "grossMonthlyIncome", List.of("1080.0"), SINGLE_VALUE, 1)
        );
    }
}
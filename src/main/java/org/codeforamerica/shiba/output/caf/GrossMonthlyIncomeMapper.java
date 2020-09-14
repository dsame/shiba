package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

@Component
public class GrossMonthlyIncomeMapper implements ApplicationInputsMapper {

    private final GrossMonthlyIncomeParser grossMonthlyIncomeParser;

    public GrossMonthlyIncomeMapper(GrossMonthlyIncomeParser grossMonthlyIncomeParser) {
        this.grossMonthlyIncomeParser = grossMonthlyIncomeParser;
    }

    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient) {
        return grossMonthlyIncomeParser.parse(application.getApplicationData())
                .stream()
                .map(jobIncomeInformation ->
                        new ApplicationInput(
                                "employee",
                                "grossMonthlyIncome",
                                List.of(String.valueOf(jobIncomeInformation.grossMonthlyIncome())),
                                SINGLE_VALUE,
                                jobIncomeInformation.getIteration()))
                .collect(Collectors.toList());
    }

}

package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.FullNameFormatter;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.codeforamerica.shiba.pages.data.PageData;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class EmploymentMapper implements ApplicationInputsMapper {

    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient) {


        return Stream.ofNullable(application.getApplicationData().getSubworkflows().get("jobs"))
                .flatMap(Collection::stream)
                .map(iteration -> {
                    String isSelfEmployed = iteration.getPagesData().get("selfEmployment").get("selfEmployment").getValue(0);


                    return new ApplicationInput("selfEmployment", "employeeFullName",
                            List.of(isSelfEmployed), ApplicationInputType.SINGLE_VALUE, subworkflow.indexOf(iteration));
                }).collect(Collectors.toList());

    }
}

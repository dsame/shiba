//package org.codeforamerica.shiba.output.applicationinputsmappers;
//
//import org.codeforamerica.shiba.application.Application;
//import org.codeforamerica.shiba.output.ApplicationInput;
//import org.codeforamerica.shiba.output.ApplicationInputType;
//import org.codeforamerica.shiba.output.FullNameFormatter;
//import org.codeforamerica.shiba.output.Recipient;
//import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
//import org.springframework.stereotype.Component;
//
//import java.util.Collection;
//import java.util.List;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//@Component
//public class HouseholdChildrenMapper implements ApplicationInputsMapper {
//
//    @Override
//    public List<ApplicationInput> map(Application application, Recipient recipient) {
//
//        List<String> householdChildren = Stream.ofNullable(application.getApplicationData().getSubworkflows().get("household"))
//                .flatMap(Collection::stream)
//                .filter(iteration -> iteration.getPagesData().getPage("householdMemberInfo").get("dateOfBirth").getValue(0))
//                .stream().map(FullNameFormatter::format).collect(Collectors.toList());
//
//        return List.of(
//                new ApplicationInput()
//        );
//    }
//}
//}

package org.codeforamerica.shiba.application;

import org.codeforamerica.shiba.Address;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.LocationClient;
import org.codeforamerica.shiba.MnitCountyInformation;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.metrics.Metrics;
import org.codeforamerica.shiba.pages.CountyMap;
import org.codeforamerica.shiba.pages.Sentiment;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.codeforamerica.shiba.County.OTHER;

@Component
public class ApplicationFactory {
    private final Clock clock;
    private final Map<String, County> countyZipCode;
    private final LocationClient locationClient;
    private final ApplicationDataParser<Address> homeAddressParser;
    public static final Map<String, Set<String>> LETTER_TO_PROGRAMS = Map.of(
            "E", Set.of("EA"),
            "K", Set.of("CASH", "GRH"),
            "F", Set.of("SNAP"),
            "C", Set.of("CCAP")
    );

    public ApplicationFactory(
            Clock clock,
            Map<String, County> countyZipCode,
            LocationClient locationClient,
            ApplicationDataParser<Address> homeAddressParser) {
        this.clock = clock;
        this.countyZipCode = countyZipCode;
        this.locationClient = locationClient;
        this.homeAddressParser = homeAddressParser;
    }

    public Application newApplication(String id, ApplicationData applicationData, Metrics metrics) {
        ApplicationData copy = new ApplicationData();
        copy.setPagesData(applicationData.getPagesData());
        copy.setSubworkflows(applicationData.getSubworkflows());
        copy.setIncompleteIterations(applicationData.getIncompleteIterations());
        Address homeAddress = homeAddressParser.parse(applicationData);
        Optional<String> countyString = locationClient.getCounty(homeAddress);
        County county = countyString.map(County::fromString)
                .orElse(countyZipCode.getOrDefault(homeAddress.getZipcode(), OTHER));
        ZonedDateTime completedAt = ZonedDateTime.now(clock);

        return Application.builder()
                .id(id)
                .completedAt(completedAt)
                .applicationData(copy)
                .county(county)
                .timeToComplete(Duration.between(metrics.getStartTime(), completedAt))
                .build();
    }

}

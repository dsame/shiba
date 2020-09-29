package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public abstract class AddressEnrichment implements Enrichment {
    ApplicationDataParser<Address> parser;
    LocationClient locationClient;

    @Override
    public EnrichmentResult process(ApplicationData applicationData) {
        return locationClient.validateAddress(parser.parse(applicationData))
                .map(address -> Map.of(
                        "enrichedStreetAddress", new InputData(List.of(address.getStreet())),
                        "enrichedCity", new InputData(List.of(address.getCity())),
                        "enrichedState", new InputData(List.of(address.getState())),
                        "enrichedZipCode", new InputData(List.of(address.getZipcode()))
                ))
                .map(EnrichmentResult::new)
                .orElse(new EnrichmentResult());
    }
}

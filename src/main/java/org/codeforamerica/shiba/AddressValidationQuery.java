package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public abstract class AddressValidationQuery implements Query {
    ApplicationDataParser<Address> parser;
    LocationClient locationClient;

    @Override
    public Map<String, Address> run(ApplicationData applicationData) {
        Address clientProvidedAddress = parser.parse(applicationData);

        Map<String, Address> addresses = new HashMap<>(Map.of("original", clientProvidedAddress));

        locationClient.validateAddress(clientProvidedAddress)
                .ifPresent(address -> addresses.put("validated", address));

        return addresses;
    }
}

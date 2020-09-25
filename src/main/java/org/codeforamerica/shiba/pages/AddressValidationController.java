package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.Address;
import org.codeforamerica.shiba.LocationClient;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class AddressValidationController {
    private final LocationClient locationClient;
    private final Map<String, ApplicationDataParser<Address>> parserMap;
    private final ApplicationData applicationData;

    public AddressValidationController(LocationClient locationClient,
                                       Map<String, ApplicationDataParser<Address>> parserMap,
                                       ApplicationData applicationData) {
        this.locationClient = locationClient;
        this.parserMap = parserMap;
        this.applicationData = applicationData;
    }

    @GetMapping("/pages/addressValidation/{addressType}")
    ResponseEntity<Map<String, Address>> addressValidation(@PathVariable("addressType") String addressType) {
        Address clientProvidedAddress = parserMap.get(addressType).parse(applicationData);

        Map<String, Address> addresses = new HashMap<>(Map.of("original", clientProvidedAddress));

        locationClient.validateAddress(clientProvidedAddress).ifPresent(address -> {
            addresses.put("validated", address);
        });

        return ResponseEntity.of(Optional.of(addresses));
    }
}

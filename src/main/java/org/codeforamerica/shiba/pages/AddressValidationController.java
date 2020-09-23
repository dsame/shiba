package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.Address;
import org.codeforamerica.shiba.LocationClient;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AddressValidationController {
    private final LocationClient locationClient;
    private final ApplicationDataParser<Address> applicationParser;
    private final ApplicationData applicationData;

    public AddressValidationController(LocationClient locationClient,
                                       ApplicationDataParser<Address> applicationParser,
                                       ApplicationData applicationData) {
        this.locationClient = locationClient;
        this.applicationParser = applicationParser;
        this.applicationData = applicationData;
    }

    @GetMapping("/pages/addressValidation/{isHome}")
    ModelAndView addressValidation() {
        Address clientProvidedAddress = applicationParser.parse(applicationData);

        Map<String, Object> addresses = new HashMap<>(Map.of("original", clientProvidedAddress));

        locationClient.validateAddress(clientProvidedAddress).ifPresent(address -> {
            addresses.put("validated", address);
        });
        return new ModelAndView("", Map.of(
                "data", Map.of("addressValidation", addresses)
        ));
    }
}

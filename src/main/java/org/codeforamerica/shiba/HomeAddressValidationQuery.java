package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.parsers.HomeAddressParser;
import org.springframework.stereotype.Component;

@Component
public class HomeAddressValidationQuery extends AddressValidationQuery {
    public HomeAddressValidationQuery(
            HomeAddressParser homeAddressParser,
            LocationClient locationClient) {
        this.parser = homeAddressParser;
        this.locationClient = locationClient;
    }
}

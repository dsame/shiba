package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.parsers.MailingAddressParser;
import org.springframework.stereotype.Component;

@Component
public class MailingAddressValidationQuery extends AddressValidationQuery {
    public MailingAddressValidationQuery(
            MailingAddressParser mailingAddressParser,
            LocationClient locationClient) {
        this.parser = mailingAddressParser;
        this.locationClient = locationClient;
    }
}

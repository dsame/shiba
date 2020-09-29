package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.parsers.HomeAddressParser;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AddressValidationQueryTest {
    private final HomeAddressParser homeAddressParser = mock(HomeAddressParser.class);
    private final LocationClient locationClient = mock(LocationClient.class);
    HomeAddressValidationQuery homeAddressValidationQuery = new HomeAddressValidationQuery(
            homeAddressParser, locationClient
    );

    @Test
    void shouldCallAddressValidationOnAddressValidationPage() throws Exception {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData());
        Address address = new Address("street", "city", "CA", "02103");
        when(homeAddressParser.parse(applicationData)).thenReturn(address);
        when(locationClient.validateAddress(address)).thenReturn(Optional.of(address));

        Map<String, Address> response = Map.of(
                "validated", address,
                "original", address
        );

        Map<String, Address> queryResult = homeAddressValidationQuery.run(applicationData);

        assertThat(queryResult).isEqualTo(response);
    }

    @Test
    void shouldNotIncludeValidatedAddress_whenLocationClientDoesNotReturnAnAddress() throws Exception {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData());
        Address address = new Address("street", "city", "CA", "02103");
        when(homeAddressParser.parse(applicationData)).thenReturn(address);
        when(locationClient.validateAddress(address)).thenReturn(empty());

        Map<String, Address> response = Map.of("original", address);

        Map<String, Address> queryResult = homeAddressValidationQuery.run(applicationData);

        assertThat(queryResult).isEqualTo(response);
    }

}
package org.codeforamerica.shiba.pages;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.codeforamerica.shiba.Address;
import org.codeforamerica.shiba.LocationClient;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SuppressWarnings("ConstantConditions")
class AddressValidationControllerTest {

    MockMvc mockMvc;
    private final LocationClient locationClient = mock(LocationClient.class);
    private final ApplicationDataParser<Address> homeAddressParser = mock(ApplicationDataParser.class);
    private final ApplicationDataParser<Address> mailingAddressParser = mock(ApplicationDataParser.class);
    private final ApplicationData applicationData = new ApplicationData();

    @BeforeEach
    void setUp() {
        AddressValidationController addressValidationController = new AddressValidationController(
                locationClient,
                Map.of("home", homeAddressParser, "mailing", mailingAddressParser),
                applicationData
        );
        mockMvc = MockMvcBuilders.standaloneSetup(addressValidationController).build();
    }

    @Test
    void shouldCallAddressValidationOnAddressValidationPage() throws Exception {
        Address address = new Address("street", "city", "CA", "02103");
        when(homeAddressParser.parse(any())).thenReturn(address);
        when(locationClient.validateAddress(address)).thenReturn(Optional.of(address));

        Map<String, Address> response = Map.of(
                "validated", address,
                "original", address
        );
        mockMvc.perform(get("/pages/addressValidation/home"))
                .andExpect(content().json(new ObjectMapper().writeValueAsString(response)));
    }

    @Test
    void shouldNotIncludeValidatedAddress_whenLocationClientDoesNotReturnAnAddress() throws Exception {
        Address address = new Address("street", "city", "CA", "02103");
        when(homeAddressParser.parse(any())).thenReturn(address);
        when(locationClient.validateAddress(address)).thenReturn(empty());

        Map<String, Address> response = Map.of(
                "original", address
        );
        mockMvc.perform(get("/pages/addressValidation/home"))
                .andExpect(content().json(new ObjectMapper().writeValueAsString(response)));
    }
}
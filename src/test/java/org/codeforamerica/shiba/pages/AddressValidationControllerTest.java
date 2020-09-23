package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.Address;
import org.codeforamerica.shiba.LocationClient;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SuppressWarnings("ConstantConditions")
class AddressValidationControllerTest {

    MockMvc mockMvc;
    private final LocationClient locationClient = mock(LocationClient.class);
    private final ApplicationDataParser<Address> applicationParser = mock(ApplicationDataParser.class);
    private final ApplicationData applicationData = new ApplicationData();

    @BeforeEach
    void setUp() {
        AddressValidationController addressValidationController = new AddressValidationController(
                locationClient,
                applicationParser,
                applicationData
        );
        mockMvc = MockMvcBuilders.standaloneSetup(addressValidationController).build();
    }

    @Test
    void shouldCallAddressValidationOnAddressValidationPage() throws Exception {
        Address address = new Address("street", "city", "CA", "02103");
        when(applicationParser.parse(any())).thenReturn(address);
        when(locationClient.validateAddress(address)).thenReturn(Optional.of(address));

        MvcResult mvcResult = mockMvc.perform(get("/pages/addressValidation"))
                .andReturn();

        assertThat(mvcResult.getModelAndView().getModel()).isEqualTo(
                Map.of("data", Map.of(
                        "addressValidation",
                        Map.of("original", address, "validated", address)
                ))
        );
    }

    @Test
    void shouldNotIncludeValidatedAddress_whenLocationClientDoesNotReturnAnAddress() throws Exception {
        Address address = new Address("street", "city", "CA", "02103");
        when(applicationParser.parse(any())).thenReturn(address);
        when(locationClient.validateAddress(address)).thenReturn(empty());

        MvcResult mvcResult = mockMvc.perform(get("/pages/addressValidation"))
                .andReturn();

        assertThat(mvcResult.getModelAndView().getModel()).isEqualTo(
                Map.of("data", Map.of(
                        "addressValidation",
                        Map.of("original", address)
                ))
        );
    }
}
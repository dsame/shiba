package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.Address;
import org.codeforamerica.shiba.Query;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueryTest extends AbstractBasePageTest {

    private static Query mockQuery;

    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-query.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration extends MetricsTestConfigurationWithExistingStartTime {
        @Bean
        @ConfigurationProperties(prefix = "query")
        public ApplicationConfiguration applicationConfiguration() {
            return new ApplicationConfiguration();
        }

        @Bean
        public Map<String, Query> queries() {
            mockQuery = mock(Query.class);
            return Map.of("validateAddress", mockQuery);
        }
    }

//    @MockBean
//    Query<Map<String, Address>> addressMapQuery;

    @Test
    void runsQueryWhenSpecifiedInConfig() {
        navigateTo("firstPage");
        testPage.clickPrimaryButton();
        Address address1 = new Address("street", "city", "CA", "02103");
        Address address2 = new Address("different street", "cool city", "NH", "66666");
        when(mockQuery.run(any())).thenReturn(
                Map.of("validated", address1, "original", address2)
        );
        assertThat(driver.findElementById("validated").getText()).isEqualTo(address1.toString());
        assertThat(driver.findElementById("original").getText()).isEqualTo(address2.toString());

    }
}

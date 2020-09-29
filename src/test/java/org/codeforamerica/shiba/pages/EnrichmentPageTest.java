package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.ApplicationEnrichment;
import org.codeforamerica.shiba.Enrichment;
import org.codeforamerica.shiba.EnrichmentResult;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EnrichmentPageTest extends AbstractBasePageTest {

    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-enrichment.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration extends MetricsTestConfigurationWithExistingStartTime {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-enrichment")
        public ApplicationConfiguration applicationConfiguration() {
            return new ApplicationConfiguration();
        }

        @Bean
        public Enrichment testEnrichment() {
            return new TestEnrichment();
        }

    }

    static class TestEnrichment implements Enrichment {
        @Override
        public EnrichmentResult process(ApplicationData applicationData) {
            String pageInputValue = applicationData
                    .getInputDataMap("firstPage")
                    .get("someTextInput")
                    .getValue().get(0);
            return new EnrichmentResult(Map.of(
                    "someEnrichmentInput", new InputData(List.of(pageInputValue + "-someEnrichmentValue"))
            ));
        }
    }

    @Autowired
    ApplicationEnrichment applicationEnrichment;

    @Test
    void enrichesThePageDataWithTheEnrichmentResults() {
        navigateTo("firstPage");
        testPage.enterInput("someTextInput", "someText");
        testPage.clickPrimaryButton();

        assertThat(driver.findElementById("originalInput").getText()).isEqualTo("someText");
        assertThat(driver.findElementById("enrichmentInput").getText()).isEqualTo("someText-someEnrichmentValue");
    }
}

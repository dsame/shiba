package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.*;
import org.codeforamerica.shiba.metrics.Metrics;
import org.codeforamerica.shiba.output.ApplicationDataConsumer;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.*;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest(classes = PageControllerTest.TestPageConfiguration.class, properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class PageControllerTest {

    private StaticMessageSource messageSource = new StaticMessageSource();

    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-pages-controller.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-pages-controller")
        public ApplicationConfiguration applicationConfiguration() {
            return new ApplicationConfiguration();
        }
    }

    ApplicationData applicationData = new ApplicationData();

    MockMvc mockMvc;

    Metrics metrics = new Metrics();

    ConfirmationData confirmationData = new ConfirmationData();

    Clock clock = mock(Clock.class);

    ApplicationDataConsumer applicationDataConsumer = mock(ApplicationDataConsumer.class);

    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);

    ApplicationFactory applicationFactory = mock(ApplicationFactory.class);

    ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);

    @Autowired
    ApplicationConfiguration applicationConfiguration;

    @BeforeEach
    void setUp() {
        PageController pageController = new PageController(
                applicationConfiguration,
                applicationData,
                clock,
                metrics,
                applicationRepository,
                applicationFactory,
                confirmationData,
                applicationEventPublisher,
                messageSource
        );

        mockMvc = MockMvcBuilders.standaloneSetup(pageController)
                .build();
        when(clock.instant()).thenReturn(Instant.now());
        when(applicationFactory.newApplication(any(), any(), any())).thenReturn(Application.builder()
                .id("defaultId")
                .completedAt(ZonedDateTime.now())
                .applicationData(null)
                .county(null)
                .fileName("")
                .timeToComplete(null)
                .build());
        messageSource.addMessage("success.feedback-success", Locale.ENGLISH, "default success message");
        messageSource.addMessage("success.feedback-failure", Locale.ENGLISH, "default failure message");
    }

    @Test
    void shouldWriteTheInputDataMapForSubmitPage() throws Exception {
        metrics.setStartTimeOnce(Instant.now());
        when(clock.instant()).thenReturn(LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant());

        mockMvc.perform(post("/submit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("foo[]", "some value"))
                .andExpect(redirectedUrl("/pages/firstPage/navigation"));

        PageData firstPage = applicationData.getPagesData().getPage("firstPage");
        assertThat(firstPage.get("foo").getValue()).contains("some value");
    }

    @Test
    void shouldPublishApplicationSubmittedEvent() throws Exception {
        metrics.setStartTimeOnce(Instant.now());

        String applicationId = "someId";
        Application application = Application.builder()
                .id(applicationId)
                .completedAt(ZonedDateTime.now())
                .applicationData(applicationData)
                .county(null)
                .fileName("")
                .timeToComplete(null)
                .build();
        when(applicationFactory.newApplication(any(), eq(applicationData), eq(metrics))).thenReturn(application);

        mockMvc.perform(post("/submit")
                .param("foo[]", "some value")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        InOrder inOrder = inOrder(applicationRepository, applicationEventPublisher);
        inOrder.verify(applicationRepository).save(application);
        inOrder.verify(applicationEventPublisher).publishEvent(new ApplicationSubmittedEvent(applicationId));
    }

    @Test
    void shouldNotConsumeApplicationDataIfPageDataIsNotValid() throws Exception {
        metrics.setStartTimeOnce(Instant.now());

        mockMvc.perform(post("/submit")
                .param("foo[]", "")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        verifyNoInteractions(applicationDataConsumer);
    }

    @Test
    void shouldSaveApplication() throws Exception {
        metrics.setStartTimeOnce(Instant.now());

        ZonedDateTime completedAt = ZonedDateTime.now();
        String applicationId = "someId";
        Application application = Application.builder()
                .id(applicationId)
                .completedAt(completedAt)
                .applicationData(applicationData)
                .county(null)
                .fileName("")
                .timeToComplete(null)
                .build();
        when(applicationRepository.getNextId()).thenReturn(applicationId);
        when(applicationFactory.newApplication(applicationId, applicationData, metrics)).thenReturn(application);

        mockMvc.perform(post("/submit")
                .param("foo[]", "some value")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        verify(applicationRepository).save(application);
        assertThat(confirmationData.getId()).isEqualTo(applicationId);
        assertThat(confirmationData.getCompletedAt()).isEqualTo(completedAt);
    }

    @Test
    void shouldUpdateApplicationWithAllFeedbackIndicatorsAndIncludeSuccessMessage() throws Exception {
        String successMessage = "yay thanks for the feedback!";
        Locale locale = Locale.JAPANESE;
        messageSource.addMessage("success.feedback-success", locale, successMessage);

        String applicationId = "14356236";
        confirmationData.setId(applicationId);
        Application application = Application.builder()
                .id("appIdFromDb")
                .build();
        when(applicationRepository.find(applicationId)).thenReturn(application);

        String feedback = "this was awesome!";
        mockMvc.perform(post("/submit-feedback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .locale(locale)
                .param("sentiment", "HAPPY")
                .param("feedback", feedback))
                .andExpect(redirectedUrl("/pages/terminalPage"))
                .andExpect(flash().attribute("feedbackSuccess", equalTo(successMessage)));

        verify(applicationRepository).save(Application.builder()
                .id(application.getId())
                .sentiment(Sentiment.HAPPY)
                .feedback(feedback)
                .build());
    }

    @Test
    void shouldUpdateApplicationWithFeedback() throws Exception {
        String applicationId = "14356236";
        confirmationData.setId(applicationId);
        Application application = Application.builder()
                .id("appIdFromDb")
                .build();
        when(applicationRepository.find(applicationId)).thenReturn(application);

        String feedback = "this was awesome!";
        mockMvc.perform(post("/submit-feedback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("feedback", feedback))
                .andExpect(redirectedUrl("/pages/terminalPage"));

        verify(applicationRepository).save(Application.builder()
                .id(application.getId())
                .feedback(feedback)
                .build());
    }

    @Test
    void shouldUpdateApplicationWithSentiment() throws Exception {
        String applicationId = "14356236";
        confirmationData.setId(applicationId);
        Application application = Application.builder()
                .id("appIdFromDb")
                .build();
        when(applicationRepository.find(applicationId)).thenReturn(application);

        mockMvc.perform(post("/submit-feedback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("sentiment", "HAPPY"))
                .andExpect(redirectedUrl("/pages/terminalPage"));

        verify(applicationRepository).save(Application.builder()
                .id(application.getId())
                .sentiment(Sentiment.HAPPY)
                .build());
    }

    @Test
    void shouldFailToSubmitFeedbackIfConfirmationDataIdIsNotSet() throws Exception {
        mockMvc.perform(post("/submit-feedback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("sentiment", "HAPPY")
                .param("feedback", "this was awesome!"))
                .andExpect(redirectedUrl("/pages/terminalPage"));

        verifyNoInteractions(applicationRepository);
    }

    @Test
    void shouldFailToSubmitFeedbackAndIncludeFailureMessageIfNeitherSentimentNorFeedbackIsSupplied() throws Exception {
        String failureMessage = "bummer, that didn't work";
        Locale locale = Locale.ITALIAN;
        messageSource.addMessage("success.feedback-failure", locale, failureMessage);

        String applicationId = "14356236";
        confirmationData.setId(applicationId);

        mockMvc.perform(post("/submit-feedback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("feedback", "")
                .locale(locale))
                .andExpect(redirectedUrl("/pages/terminalPage"))
                .andExpect(flash().attribute("feedbackFailure", equalTo(failureMessage)));

        verifyNoInteractions(applicationRepository);
    }
}
package org.codeforamerica.shiba.metrics;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.ApplicationRepository;
import org.codeforamerica.shiba.Encryptor;
import org.codeforamerica.shiba.pages.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.OLMSTED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Sql(statements = {"ALTER SEQUENCE application_id RESTART WITH 12", "TRUNCATE TABLE applications"})
class ApplicationRepositoryTest {

    @Autowired
    ApplicationRepository applicationRepository;

    Encryptor mockEncryptor = mock(Encryptor.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

    ApplicationRepository applicationRepositoryWithMockEncryptor;

    @BeforeEach
    void setUp() {
        applicationRepositoryWithMockEncryptor = new ApplicationRepository(jdbcTemplate, mockEncryptor);
        when(mockEncryptor.encrypt(any())).thenReturn("default encrypted data".getBytes());
    }

    @Test
    void shouldGenerateIdForNextApplication() {
        String nextId = applicationRepository.getNextId();

        assertThat(nextId).endsWith("12");

        String nextIdAgain = applicationRepository.getNextId();

        assertThat(nextIdAgain).endsWith("13");
    }

    @Test
    void shouldPrefixIdWithRandom3DigitSalt() {
        String nextId = applicationRepository.getNextId();

        assertThat(nextId).matches("^[1-9]\\d{2}.*");

        String nextIdAgain = applicationRepository.getNextId();

        assertThat(nextIdAgain.substring(0, 3)).isNotEqualTo(nextId.substring(0, 3));
    }

    @Test
    void shouldPadTheIdWithZeroesUntilReach10Digits() {
        String nextId = applicationRepository.getNextId();

        assertThat(nextId).hasSize(10);
        assertThat(nextId.substring(3, 8)).isEqualTo("00000");
    }

    @Test
    void shouldSaveApplication() {
        ApplicationData applicationData = new ApplicationData();
        PageData pageData = new PageData();
        pageData.put("someInput", InputData.builder().value(List.of("someValue")).build());
        applicationData.setPagesData(new PagesData(Map.of("somePage", pageData)));
        Subworkflows subworkflows = new Subworkflows();
        PagesData subflowIteration = new PagesData();
        PageData groupedPage = new PageData();
        groupedPage.put("someGroupedPageInput", InputData.builder().value(List.of("someGroupedPageValue")).build());
        subflowIteration.put("someGroupedPage", groupedPage);
        subworkflows.addIteration("someGroup", subflowIteration);
        applicationData.setSubworkflows(subworkflows);

        Application application = new Application("someid", ZonedDateTime.now(ZoneOffset.UTC), applicationData, OLMSTED);

        applicationRepository.save(application);

        assertThat(applicationRepository.find("someid")).isEqualTo(application);
    }

    @Test
    void shouldEncryptApplicationData() {
        ApplicationData applicationData = new ApplicationData();
        Application application = new Application("someid", ZonedDateTime.now(ZoneOffset.UTC), applicationData, OLMSTED);

        applicationRepositoryWithMockEncryptor.save(application);

        verify(mockEncryptor).encrypt(applicationData);
    }

    @Test
    void shouldStoreEncryptedApplicationData() {
        ApplicationData applicationData = new ApplicationData();
        Application application = new Application("someid", ZonedDateTime.now(ZoneOffset.UTC), applicationData, OLMSTED);
        byte[] expectedEncryptedData = "here is the encrypted data".getBytes();
        when(mockEncryptor.encrypt(any())).thenReturn(expectedEncryptedData);

        applicationRepositoryWithMockEncryptor.save(application);

        byte[] actualEncryptedData = jdbcTemplate.queryForObject(
                "SELECT encrypted_data " +
                        "FROM applications " +
                        "WHERE id = 'someid'", byte[].class);
        assertThat(actualEncryptedData).isEqualTo(expectedEncryptedData);
    }

    @Test
    void shouldDecryptApplicationData() {
        ApplicationData applicationData = new ApplicationData();
        String applicationId = "someid";
        Application application = new Application(applicationId, ZonedDateTime.now(ZoneOffset.UTC), applicationData, OLMSTED);
        byte[] encryptedData = "here is the encrypted data".getBytes();
        when(mockEncryptor.encrypt(any())).thenReturn(encryptedData);

        applicationRepositoryWithMockEncryptor.save(application);

        applicationRepositoryWithMockEncryptor.find(applicationId);

        verify(mockEncryptor).decrypt(encryptedData);
    }

    @Test
    void shouldUseDecryptedApplicationDataForTheRetrievedApplication() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData(Map.of("somePage", new PageData())));
        String applicationId = "someid";
        Application application = new Application(applicationId, ZonedDateTime.now(ZoneOffset.UTC), applicationData, OLMSTED);
        ApplicationData decryptedApplicationData = new ApplicationData();
        decryptedApplicationData.setPagesData(new PagesData(Map.of("someDecryptedPage", new PageData())));
        when(mockEncryptor.decrypt(any())).thenReturn(decryptedApplicationData);

        applicationRepositoryWithMockEncryptor.save(application);

        Application retrievedApplication = applicationRepositoryWithMockEncryptor.find(applicationId);

        assertThat(retrievedApplication.getApplicationData()).isEqualTo(decryptedApplicationData);
    }
}
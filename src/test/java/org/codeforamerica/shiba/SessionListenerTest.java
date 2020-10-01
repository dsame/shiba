package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SessionListenerTest {
    ResearchDataRepository researchDataRepository = mock(ResearchDataRepository.class);
    ResearchDataParser researchDataParser = mock(ResearchDataParser.class);
    SessionListener sessionListener = new SessionListener(researchDataRepository, researchDataParser);

    @Test
    void shouldSaveResearchData() {
        HttpSessionEvent mockSessionEvent = mock(HttpSessionEvent.class);
        HttpSession session = new MockHttpSession();
        ApplicationData applicationData = new ApplicationData();
        session.setAttribute("scopedTarget.applicationData", applicationData);
        when(mockSessionEvent.getSession()).thenReturn(session);
        ResearchData researchData = ResearchData.builder().build();
        when(researchDataParser.parse(applicationData)).thenReturn(researchData);

        sessionListener.sessionDestroyed(mockSessionEvent);

        verify(researchDataRepository).save(researchData);
    }

    @Test
    void shouldNotSaveResearchDataWhenTheDataIsBlank() {
        assertThat(false).isTrue();
    }
}
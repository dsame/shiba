package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.data.ApplicationData;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@WebListener
public class SessionListener implements HttpSessionListener {

    private final ResearchDataRepository researchDataRepository;
    private final ResearchDataParser researchDataParser;

    public SessionListener(ResearchDataRepository researchDataRepository,
                           ResearchDataParser researchDataParser) {
        this.researchDataRepository = researchDataRepository;
        this.researchDataParser = researchDataParser;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent sessionEvent) {
        ApplicationData applicationData = ((ApplicationData) sessionEvent.getSession().getAttribute("scopedTarget.applicationData"));
        if (!applicationData.getPagesData().isEmpty()) {
            researchDataRepository.save(researchDataParser.parse(applicationData));
        }
    }

}
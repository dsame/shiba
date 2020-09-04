package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.ApplicationRepository;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;

@Component
public class ApplicationSubmittedListener implements EnvironmentAware {
    @Autowired
    private org.springframework.core.env.Environment environment;
    private final MnitDocumentConsumer mnitDocumentConsumer;
    private final ApplicationRepository applicationRepository;
    private final EmailClient emailClient;
    private final ExpeditedEligibilityDecider expeditedEligibilityDecider;
    private final ApplicationInputsMappers applicationInputsMappers;
    private final PdfGenerator pdfGenerator;

    public ApplicationSubmittedListener(MnitDocumentConsumer mnitDocumentConsumer,
                                        ApplicationRepository applicationRepository,
                                        EmailClient emailClient,
                                        ExpeditedEligibilityDecider expeditedEligibilityDecider,
                                        ApplicationInputsMappers applicationInputsMappers,
                                        PdfGenerator pdfGenerator) {
        this.mnitDocumentConsumer = mnitDocumentConsumer;
        this.applicationRepository = applicationRepository;
        this.emailClient = emailClient;
        this.expeditedEligibilityDecider = expeditedEligibilityDecider;
        this.applicationInputsMappers = applicationInputsMappers;
        this.pdfGenerator = pdfGenerator;
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    @Async
    @EventListener
    public void handleApplicationSubmittedEvent(ApplicationSubmittedEvent applicationSubmittedEvent) {
        //TODO: put this back when MN-IT integration is done
//        this.mnitDocumentConsumer.process(this.applicationRepository.find(applicationSubmittedEvent.getApplicationId()));
    }

    @Async
    @EventListener
    public void sendConfirmationEmail(ApplicationSubmittedEvent event) {
        Application application = applicationRepository.find(event.getApplicationId());
        PagesData pagesData = application.getApplicationData().getPagesData();
        Optional.ofNullable(pagesData
                .getPage("contactInfo")
                .get("email"))
                .ifPresent(input -> {
                    List<ApplicationInput> applicationInputs = applicationInputsMappers.map(application, CLIENT);
                    String applicationId = application.getId();
                    ApplicationFile pdf = pdfGenerator.generate(applicationInputs, applicationId);
                    ExpeditedEligibility expeditedEligibility = expeditedEligibilityDecider.decide(pagesData);
                    emailClient.sendConfirmationEmail(input.getValue().get(0), applicationId, expeditedEligibility, pdf);
                });
    }

    @Async
    @EventListener
    public void sendCaseWorkerEmail(ApplicationSubmittedEvent event) {
//        Application application = applicationRepository.find(event.getApplicationId());
//        PageData personalInfo = application.getApplicationData().getInputDataMap("personalInfo");
//
//        emailClient.sendCaseWorkerEmail(
//                countyEmailMap.get(application.getCounty()),
//                String.join(" ", personalInfo.get("firstName").getValue().get(0), personalInfo.get("lastName").getValue().get(0)),
//                null//TODO: figure out how to convert byte[] to File
//        );
        Application application = applicationRepository.find(event.getApplicationId());
        PagesData pagesData = application.getApplicationData().getPagesData();
        PageData personalInfo = application.getApplicationData().getInputDataMap("personalInfo");
        List<ApplicationInput> applicationInputs = applicationInputsMappers.map(application, CASEWORKER);
        String applicationId = application.getId();
        ApplicationFile pdf = pdfGenerator.generate(applicationInputs, applicationId);
        ExpeditedEligibility expeditedEligibility = expeditedEligibilityDecider.decide(pagesData);

        String fullName = String.join(" ", personalInfo.get("firstName").getValue().get(0), personalInfo.get("lastName").getValue().get(0));
        emailClient.sendCaseWorkerEmail(getCountyEmail(application.getCounty()), fullName, applicationId, expeditedEligibility, pdf);
    }

    private String getCountyEmail(County county) {
        String[] notFoundArray = new String[1];
        notFoundArray[0] = "";
        String[] activeProfiles = Optional.ofNullable(environment.getActiveProfiles()).orElse(notFoundArray);
        String activeProfile = activeProfiles[0];
        String email = "";

        switch(activeProfile) {
            case "demo":
                email = "help+demo@mnbenefits.org";
                break;
            case "staging":
                email = "help+staging@mnbenefits.org";
                break;
            case "production":
                switch (county) {
                    case HENNEPIN:
                        email = "hhsews@hennepin.us";
                        break;
                    case OLMSTED:
                        email = "PAQ@co.olmsted.mn.us";
                        break;
                    default:
                        email = "hhsews@hennepin.us";
                }
            default:
                email = "help+dev@mnbenefits.org";
        }

        return email;
    }
}

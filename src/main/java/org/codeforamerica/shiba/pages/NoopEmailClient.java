package org.codeforamerica.shiba.pages;

import lombok.extern.log4j.Log4j2;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@ConditionalOnProperty(value = "SUBMIT_VIA_EMAIL", havingValue = "false", matchIfMissing = true)
@Log4j2
public class NoopEmailClient implements EmailClient {
    @Override
    public void sendConfirmationEmail(String recipient, String confirmationId, ExpeditedEligibility expeditedEligibility) {
        log.debug("Did you forget to set SUBMIT_VIA_EMAIL=true?");
    }

    @Override
    public void sendCaseWorkerEmail(String recipient, String clientName, File attachment) {
        log.debug("Did you forget to set SUBMIT_VIA_EMAIL=true?");
    }
}

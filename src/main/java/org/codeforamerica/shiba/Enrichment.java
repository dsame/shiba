package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.data.ApplicationData;

public interface Enrichment {
    default EnrichmentResult process(ApplicationData applicationData) {
        return null;
    }
}

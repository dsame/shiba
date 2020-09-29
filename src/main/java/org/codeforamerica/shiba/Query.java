package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.data.ApplicationData;

public interface Query {
    Object run(ApplicationData applicationData);
}

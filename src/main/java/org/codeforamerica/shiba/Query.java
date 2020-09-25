package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.data.ApplicationData;

public interface Query<T> {
    T run(ApplicationData applicationData);
}

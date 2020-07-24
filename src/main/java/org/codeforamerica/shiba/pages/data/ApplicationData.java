package org.codeforamerica.shiba.pages.data;

import lombok.Data;

@Data
public class ApplicationData {
    private PagesData pagesData = new PagesData();
    private String submissionTime;

    public void clear() {
        this.pagesData.clear();
        this.submissionTime = null;
    }

    public boolean isSubmitted() {
        return this.submissionTime != null;
    }

    public InputDataMap getInputDataMap(String pageName) {
        return this.pagesData.getPage(pageName);
    }
}
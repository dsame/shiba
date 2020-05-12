package org.codeforamerica.shiba.pages;

import org.openqa.selenium.remote.RemoteWebDriver;

public class PrepareToApplyPage extends BasePage {
    private final RemoteWebDriver driver;

    public PrepareToApplyPage(RemoteWebDriver driver) {
        super(driver);
        this.driver = driver;
    }

    @Override
    public LandingPage goBack() {
        backButton.click();

        return new LandingPage(driver);
    }

    public LanguagePreferencesPage clickPrimaryButton() {
        primaryButton.click();

        return new LanguagePreferencesPage(driver);
    }

}

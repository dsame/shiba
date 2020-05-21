package org.codeforamerica.shiba.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

public class SuccessPage extends BasePage{
    @FindBy(linkText="Download My Receipt")
    WebElement downloadReceiptButton;

    public SuccessPage(RemoteWebDriver driver) {
        super(driver);
    }

    public void downloadReceipt() {
        downloadReceiptButton.click();
    }
}
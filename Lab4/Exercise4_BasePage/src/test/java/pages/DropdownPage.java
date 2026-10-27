package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import utils.ConfigReader;

public class DropdownPage extends BasePage {
    private final By dropdown = By.id("dropdown");

    public DropdownPage(WebDriver driver) {
        super(driver);
    }

    public DropdownPage open() {
        navigateTo(ConfigReader.get("baseUrl", "https://the-internet.herokuapp.com") + "/dropdown");
        return this;
    }

    public void select(String visibleText) {
        new Select(waitForVisibility(dropdown)).selectByVisibleText(visibleText);
    }

    public String getSelected() {
        return new Select(waitForVisibility(dropdown)).getFirstSelectedOption().getText();
    }
}
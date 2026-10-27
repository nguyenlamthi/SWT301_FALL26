package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import utils.ConfigReader;

import java.util.List;

public class CheckboxesPage extends BasePage {
    private final By boxes = By.cssSelector("#checkboxes input[type='checkbox']");

    public CheckboxesPage(WebDriver driver) {
        super(driver);
    }

    public CheckboxesPage open() {
        navigateTo(ConfigReader.get("baseUrl", "https://the-internet.herokuapp.com") + "/checkboxes");
        return this;
    }

    private List<WebElement> all() {
        waitForVisibility(boxes);
        return driver.findElements(boxes);
    }

    public void check(int index) {
        WebElement box = all().get(index);
        if (!box.isSelected()) {
            box.click();
        }
    }

    public void uncheck(int index) {
        WebElement box = all().get(index);
        if (box.isSelected()) {
            box.click();
        }
    }

    public boolean isChecked(int index) {
        return all().get(index).isSelected();
    }
}
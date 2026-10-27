package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.ConfigReader;

public class DynamicLoadingPage extends BasePage {
    private final By startBtn = By.cssSelector("#start button");
    private final By finish   = By.cssSelector("#finish h4");

    public DynamicLoadingPage(WebDriver driver) {
        super(driver);
    }

    public DynamicLoadingPage open(int example) {
        navigateTo(ConfigReader.get("baseUrl", "https://the-internet.herokuapp.com") + "/dynamic_loading/" + example);
        return this;
    }

    /**
     * Bấm Start rồi đợi (mất ~5s loading) cho đến khi chữ kết quả hiện lên
     */
    public String startAndGetResult() {
        click(startBtn);
        return getText(finish);
    }
}
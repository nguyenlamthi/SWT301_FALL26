package tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pages.CheckboxesPage;
import pages.DropdownPage;
import pages.DynamicLoadingPage;
import pages.JsAlertsPage;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Common web widgets")
public class WidgetsTest extends BaseTest {

    @ParameterizedTest(name = "Select option: {0}")
    @ValueSource(strings = {"Option 1", "Option 2"})
    void dropdownSelect(String option) {
        DropdownPage page = new DropdownPage(driver).open();
        page.select(option);
        assertEquals(option, page.getSelected(), "Option được chọn không khớp");
    }

    @Test
    @DisplayName("Toggle checkboxes state")
    void checkboxToggle() {
        CheckboxesPage page = new CheckboxesPage(driver).open();
        page.check(0);   // Checkbox 1 mặc định chưa chọn -> chọn nó
        page.uncheck(1); // Checkbox 2 mặc định đang chọn -> bỏ chọn nó

        assertAll("Checkboxes state",
                () -> assertTrue(page.isChecked(0), "Checkbox 1 phải đang được chọn"),
                () -> assertFalse(page.isChecked(1), "Checkbox 2 phải không được chọn")
        );
    }

    @Test
    @DisplayName("Interact with JavaScript Alerts, Confirm and Prompt")
    void jsAlerts() {
        JsAlertsPage page = new JsAlertsPage(driver).open();

        // 1. Alert (bấm OK)
        page.acceptAlert();
        assertEquals("You successfully clicked an alert", page.getResult());

        // 2. Confirm (bấm Cancel)
        page.dismissConfirm();
        assertEquals("You clicked: Cancel", page.getResult());

        // 3. Prompt (nhập text rồi bấm OK)
        page.answerPrompt("SWT301");
        assertEquals("You entered: SWT301", page.getResult());
    }

    @ParameterizedTest(name = "Dynamic loading example {0}")
    @ValueSource(ints = {1, 2})
    @DisplayName("Wait for asynchronously loaded element")
    void dynamicLoading(int example) {
        // Tự động chờ loading bar ~5s biến mất và chữ Hello World hiển thị
        String text = new DynamicLoadingPage(driver).open(example).startAndGetResult();
        assertEquals("Hello World!", text, "Không lấy được nội dung sau khi tải bất đồng bộ");
    }
}
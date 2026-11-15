package be.pxl.qacademy.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** "My attendance": eigen aanwezigheden per cursus. */
public class MyAttendancePage extends BasePage {

    public MyAttendancePage(Page page) {
        super(page);
    }

    public MyAttendancePage open() {
        navigateTo("/my-attendance");
        assertThat(byTestId("my-attendance-page")).isVisible();
        waitUntilLoaded();
        return this;
    }

    public Locator row(String courseTitle) {
        return byTestIdPrefix("my-attendance-row-").filter(new Locator.FilterOptions().setHasText(courseTitle));
    }

    public Locator emptyMessage() {
        return byTestId("empty-attendance-message");
    }
}

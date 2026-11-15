package be.pxl.qacademy.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Aanwezigheden registreren voor een cursus ("/courses/{id}/attendance"). */
public class CourseAttendancePage extends BasePage {

    public CourseAttendancePage(Page page) {
        super(page);
    }

    public CourseAttendancePage open(String courseId) {
        open("/courses/" + courseId + "/attendance");
        return waitUntilShown();
    }

    public CourseAttendancePage waitUntilShown() {
        assertThat(byTestId("course-attendance-page")).isVisible();
        waitUntilLoaded();
        return this;
    }

    public Locator row(String userId) {
        return byTestId("attendance-row-" + userId);
    }

    public Locator rows() {
        return byTestIdPrefix("attendance-row-");
    }

    /** status: PRESENT, ABSENT of EXCUSED. */
    public CourseAttendancePage mark(String userId, String status) {
        byTestId("attendance-select-" + userId).selectOption(new SelectOption().setLabel(status));
        return this;
    }

    public Locator selectedStatus(String userId) {
        return byTestId("attendance-select-" + userId);
    }

    public void save() {
        byTestId("save-attendance-button").click();
    }

    public Locator saveMessage() {
        return byTestId("save-attendance-message");
    }

    public Locator emptyMessage() {
        return byTestId("empty-attendance-message");
    }
}

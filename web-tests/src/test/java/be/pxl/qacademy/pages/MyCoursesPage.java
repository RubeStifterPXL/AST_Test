package be.pxl.qacademy.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** "My courses" van een instructor: enkel de cursussen waaraan hij toegewezen is. */
public class MyCoursesPage extends BasePage {

    public MyCoursesPage(Page page) {
        super(page);
    }

    public MyCoursesPage open() {
        open("/my-courses");
        assertThat(byTestId("my-courses-page")).isVisible();
        waitUntilLoaded();
        return this;
    }

    public Locator pageRoot() {
        return byTestId("my-courses-page");
    }

    public Locator row(String courseId) {
        return byTestId("my-course-row-" + courseId);
    }

    public Locator rows() {
        return byTestIdPrefix("my-course-row-");
    }

    public Locator title(String courseId) {
        return byTestId("my-course-title-" + courseId);
    }

    public Locator status(String courseId) {
        return byTestId("my-course-status-" + courseId);
    }

    public CourseSubscribersPage openSubscribers(String courseId) {
        byTestId("my-course-subscribers-link-" + courseId).click();
        return new CourseSubscribersPage(page).waitUntilShown();
    }

    public CourseAttendancePage openAttendance(String courseId) {
        byTestId("my-course-attendance-link-" + courseId).click();
        return new CourseAttendancePage(page).waitUntilShown();
    }

    public CourseExamResultsPage openExamResults(String courseId) {
        byTestId("my-course-exam-results-link-" + courseId).click();
        return new CourseExamResultsPage(page).waitUntilShown();
    }
}

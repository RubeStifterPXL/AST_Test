package be.pxl.qacademy.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Detail van een cursus ("/courses/{id}"), met curriculum en (voor studenten) Subscribe. */
public class CourseDetailPage extends BasePage {

    public CourseDetailPage(Page page) {
        super(page);
    }

    public CourseDetailPage open(String courseId) {
        open("/courses/" + courseId);
        return waitUntilShown();
    }

    public CourseDetailPage waitUntilShown() {
        assertThat(byTestId("course-detail-page")).isVisible();
        waitUntilLoaded();
        return this;
    }

    public Locator title() {
        return byTestId("course-title");
    }

    public Locator statusBadge() {
        return byTestId("course-status-badge");
    }

    public Locator description() {
        return byTestId("course-description");
    }

    public Locator price() {
        return byTestId("course-price");
    }

    /** Hele detailblok (prijs, leeftijd, capaciteit, locatie, datums). */
    public Locator detailsBlock() {
        return byTestId("course-detail-page").locator("dl");
    }

    public Locator modules() {
        return byTestIdPrefix("module-item-");
    }

    public List<String> moduleTexts() {
        return modules().allInnerTexts();
    }

    public Locator noModulesMessage() {
        return byTestId("no-modules-message");
    }

    public Locator subscribeButton() {
        return byTestId("subscribe-button");
    }

    public void subscribe() {
        subscribeButton().click();
    }

    public Locator subscribeSuccess() {
        return byTestId("subscribe-success-alert");
    }

    public Locator subscribeError() {
        return byTestId("subscribe-error-alert");
    }

    public Locator editCourseLink() {
        return byTestId("edit-course-link");
    }

    public Locator viewSubscribersLink() {
        return byTestId("view-subscribers-link");
    }

    public Locator manageAttendanceLink() {
        return byTestId("manage-attendance-link");
    }

    public Locator recordExamResultsLink() {
        return byTestId("record-exam-results-link");
    }
}

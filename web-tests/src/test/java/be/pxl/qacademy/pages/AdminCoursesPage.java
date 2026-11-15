package be.pxl.qacademy.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Cursusbeheer ("/admin/courses"): lijst en "New course"-formulier. */
public class AdminCoursesPage extends BasePage {

    public AdminCoursesPage(Page page) {
        super(page);
    }

    public AdminCoursesPage open() {
        navigateTo("/admin/courses");
        return waitUntilShown();
    }

    public AdminCoursesPage waitUntilShown() {
        assertThat(byTestId("admin-courses-page")).isVisible();
        waitUntilLoaded();
        return this;
    }

    public Locator pageRoot() {
        return byTestId("admin-courses-page");
    }

    public AdminCoursesPage startNewCourse() {
        byTestId("new-course-button").click();
        assertThat(byTestId("create-course-form")).isVisible();
        return this;
    }

    public AdminCoursesPage fill(String title, String slug, String description, String price,
                                 String minAge, String capacityPolicy) {
        byTestId("course-title-input").fill(title);
        byTestId("course-slug-input").fill(slug);
        byTestId("course-description-input").fill(description == null ? "" : description);
        byTestId("course-price-input").fill(price == null ? "" : price);
        byTestId("course-min-age-input").fill(minAge == null ? "" : minAge);
        byTestId("course-capacity-policy-select").selectOption(new SelectOption().setValue(capacityPolicy));
        return this;
    }

    public void save() {
        byTestId("save-course-button").click();
    }

    public Locator createForm() {
        return byTestId("create-course-form");
    }

    public Locator formError() {
        return byTestId("form-error-alert");
    }

    /** Validatiemelding onder het slug-veld (client-side, vóór er iets naar de API gaat). */
    public Locator slugValidationMessage() {
        return createForm().getByText("Lowercase letters, digits and single hyphens only.");
    }

    /** Validatiemelding onder het titelveld. */
    public Locator titleValidationMessage() {
        return createForm().getByText("A title is required");
    }

    public Locator row(String courseTitle) {
        return byTestIdPrefix("admin-course-row-").filter(new Locator.FilterOptions().setHasText(courseTitle));
    }

    public AdminCourseEditPage edit(String courseId) {
        byTestId("edit-course-link-" + courseId).click();
        return new AdminCourseEditPage(page).waitUntilShown();
    }

    public void refresh() {
        byTestId("refresh-courses-button").click();
        waitUntilLoaded();
    }
}

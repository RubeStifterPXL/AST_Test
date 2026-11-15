package be.pxl.qacademy.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Cursus bewerken ("/admin/courses/{id}/edit"): velden, docenten, publiceren/annuleren/verwijderen en modules. */
public class AdminCourseEditPage extends BasePage {

    private static final DateTimeFormatter DATETIME_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public AdminCourseEditPage(Page page) {
        super(page);
    }

    public AdminCourseEditPage open(String courseId) {
        navigateTo("/admin/courses/" + courseId + "/edit");
        return waitUntilShown();
    }

    public AdminCourseEditPage waitUntilShown() {
        assertThat(byTestId("admin-course-edit-page")).isVisible();
        assertThat(byTestId("edit-course-form")).isVisible();
        return this;
    }

    public Locator statusBadge() {
        return byTestId("course-status-badge");
    }

    public Locator actionMessage() {
        return byTestId("action-message");
    }

    // ---- velden ----

    public AdminCourseEditPage title(String title) {
        byTestId("course-title-input").fill(title);
        return this;
    }

    public AdminCourseEditPage price(String price) {
        byTestId("course-price-input").fill(price == null ? "" : price);
        return this;
    }

    public AdminCourseEditPage location(String locationName) {
        byTestId("course-location-select").selectOption(new SelectOption().setLabel(locationName));
        return this;
    }

    public AdminCourseEditPage capacityPolicy(String policy) {
        byTestId("course-capacity-policy-select").selectOption(new SelectOption().setValue(policy));
        return this;
    }

    public AdminCourseEditPage start(LocalDateTime start) {
        byTestId("course-start-input").fill(start.format(DATETIME_LOCAL));
        return this;
    }

    public AdminCourseEditPage end(LocalDateTime end) {
        byTestId("course-end-input").fill(end.format(DATETIME_LOCAL));
        return this;
    }

    public AdminCourseEditPage maxSubscribers(int max) {
        byTestId("course-max-subscribers-input").fill(String.valueOf(max));
        return this;
    }

    public AdminCourseEditPage assignInstructor(String instructorUserId, boolean assigned) {
        byTestId("instructor-checkbox-" + instructorUserId).setChecked(assigned);
        return this;
    }

    public void save() {
        byTestId("save-course-edit-button").click();
    }

    // ---- levenscyclus ----

    public void publish() {
        byTestId("publish-course-button").click();
    }

    public void unpublish() {
        byTestId("unpublish-course-button").click();
    }

    public void cancelCourse() {
        byTestId("cancel-course-button").click();
    }

    /** De app vraagt een bevestiging via window.confirm: die wordt hier bewust aanvaard. */
    public void deleteCourse() {
        page.onceDialog(dialog -> dialog.accept());
        byTestId("delete-course-button").click();
    }

    public Locator publishButton() {
        return byTestId("publish-course-button");
    }

    public Locator deleteButton() {
        return byTestId("delete-course-button");
    }

    // ---- modules ----

    public AdminCourseEditPage addModule(String title, String description, int sequenceIndex) {
        byTestId("add-module-button").click();
        fillModule(title, description, sequenceIndex);
        byTestId("save-module-button").click();
        assertThat(moduleRows().filter(new Locator.FilterOptions().setHasText(title))).hasCount(1);
        return this;
    }

    public void fillModule(String title, String description, int sequenceIndex) {
        assertThat(byTestId("module-form")).isVisible();
        byTestId("module-title-input").fill(title);
        byTestId("module-description-input").fill(description);
        byTestId("module-sequence-input").fill(String.valueOf(sequenceIndex));
    }

    public void editModule(String currentTitle, String newTitle, String description, int sequenceIndex) {
        Locator row = moduleRow(currentTitle);
        row.locator("[data-testid^='edit-module-button-']").click();
        fillModule(newTitle, description, sequenceIndex);
        byTestId("save-module-button").click();
    }

    /** Ook hier een window.confirm ("Delete module ...?"). */
    public void deleteModule(String title) {
        page.onceDialog(dialog -> dialog.accept());
        moduleRow(title).locator("[data-testid^='delete-module-button-']").click();
    }

    public Locator moduleRows() {
        return byTestIdPrefix("admin-module-row-");
    }

    public Locator moduleRow(String title) {
        return moduleRows().filter(new Locator.FilterOptions().setHasText(title));
    }

    public Locator moduleFormError() {
        return byTestId("module-form-error");
    }
}

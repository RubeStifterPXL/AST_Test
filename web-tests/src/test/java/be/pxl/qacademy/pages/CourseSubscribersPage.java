package be.pxl.qacademy.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Inschrijvingen van één cursus ("/courses/{id}/subscribers"), voor admin en toegewezen instructor. */
public class CourseSubscribersPage extends BasePage {

    public CourseSubscribersPage(Page page) {
        super(page);
    }

    public CourseSubscribersPage open(String courseId) {
        navigateTo("/courses/" + courseId + "/subscribers");
        return waitUntilShown();
    }

    public CourseSubscribersPage waitUntilShown() {
        assertThat(byTestId("course-subscribers-page")).isVisible();
        waitUntilLoaded();
        return this;
    }

    public Locator table() {
        return byTestId("subscriber-table");
    }

    public Locator rows() {
        return byTestIdPrefix("subscriber-row-");
    }

    public Locator row(String userId) {
        return byTestId("subscriber-row-" + userId);
    }

    public Locator columnHeaders() {
        return table().locator("th");
    }

    /** Volledige zichtbare tekst van de pagina: om te controleren dat er geen e-mailadressen in staan. */
    public String visibleText() {
        return byTestId("course-subscribers-page").innerText();
    }
}

package be.pxl.qacademy.pages;

import be.pxl.qacademy.config.Config;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Basis voor alle page objects. Locators gebruiken de data-testid-attributen van de Angular-app
 * (Playwright's standaard testIdAttribute), nooit CSS-classes of teksten die kunnen veranderen.
 */
public abstract class BasePage {

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    public Page page() {
        return page;
    }

    protected Locator byTestId(String testId) {
        return page.getByTestId(testId);
    }

    /** Alle elementen waarvan de data-testid met dit voorvoegsel begint (bv. rijen "admin-course-row-"). */
    protected Locator byTestIdPrefix(String prefix) {
        return page.locator("[data-testid^='" + prefix + "']");
    }

    protected void open(String path) {
        page.navigate(Config.baseUrl() + path);
    }

    /** Wacht tot een eventuele laadindicator weg is. */
    protected void waitUntilLoaded() {
        assertThat(byTestId("loading-spinner")).hasCount(0);
    }

    public Locator errorAlert() {
        return byTestId("error-alert");
    }

    public NavBar nav() {
        return new NavBar(page);
    }
}

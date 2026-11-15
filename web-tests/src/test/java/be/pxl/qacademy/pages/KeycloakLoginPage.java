package be.pxl.qacademy.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** De loginpagina van Keycloak (thema keycloak.v2, standaard id's). */
public class KeycloakLoginPage {

    private final Page page;

    public KeycloakLoginPage(Page page) {
        this.page = page;
    }

    public KeycloakLoginPage waitUntilShown() {
        assertThat(usernameInput()).isVisible();
        return this;
    }

    public void loginAs(String username, String password) {
        waitUntilShown();
        usernameInput().fill(username);
        page.locator("#password").fill(password);
        page.locator("#kc-login").click();
    }

    public Locator usernameInput() {
        return page.locator("#username");
    }

    /** Foutmelding van Keycloak, bv. "Invalid username or password." */
    public Locator errorMessage() {
        return page.locator("#input-error, .kc-feedback-text, [id^='input-error']").first();
    }

    public boolean isShown() {
        return usernameInput().isVisible();
    }
}

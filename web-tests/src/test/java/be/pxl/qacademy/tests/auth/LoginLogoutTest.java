package be.pxl.qacademy.tests.auth;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.CatalogPage;
import be.pxl.qacademy.pages.KeycloakLoginPage;
import be.pxl.qacademy.pages.NavBar;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-05: Als gebruiker log ik in en uit via Keycloak (geldige en ongeldige credentials).
 */
@Tag("auth")
@Tag("US-05")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-05 Inloggen en uitloggen via Keycloak")
class LoginLogoutTest extends BaseUiTest {

    @Test
    @DisplayName("Geldige credentials: gebruikersnaam zichtbaar in de navbar, uitloggen toont opnieuw 'Log in'")
    void validLoginAndLogout() {
        Page page = anonymousPage();
        new CatalogPage(page).open();
        NavBar nav = new NavBar(page);
        assertThat(nav.loginButton()).isVisible();

        nav.clickLogin();
        assertThat(page).hasURL(Pattern.compile("/realms/qacademy/"));
        new KeycloakLoginPage(page).loginAs(TestUser.STUDENT_1.username(), TestUser.STUDENT_1.password());

        nav.waitUntilLoggedInAs(TestUser.STUDENT_1.username());
        assertThat(nav.loginButton()).hasCount(0);
        assertThat(nav.mySubscriptionsLink()).isVisible();

        nav.logout();
        assertThat(nav.loginButton()).isVisible();
        assertThat(nav.currentUsername()).hasCount(0);
        assertThat(nav.mySubscriptionsLink()).hasCount(0);
    }

    @Test
    @DisplayName("Fout wachtwoord: Keycloak toont een foutmelding en de gebruiker blijft uitgelogd")
    void wrongPasswordIsRejected() {
        Page page = anonymousPage();
        new CatalogPage(page).open();
        new NavBar(page).clickLogin();

        KeycloakLoginPage login = new KeycloakLoginPage(page);
        login.loginAs(TestUser.STUDENT_1.username(), "definitely-wrong-" + System.nanoTime());

        assertThat(login.errorMessage()).isVisible();
        assertThat(login.errorMessage()).containsText("Invalid username or password");
        assertThat(login.usernameInput()).isVisible();
        assertThat(page).not().hasURL(Pattern.compile("^https://qacademy[ab]\\.duckdns\\.org/"));
    }

    @Test
    @DisplayName("Onbekende gebruiker: zelfde algemene foutmelding (verraadt niet of het account bestaat)")
    void unknownUserGetsGenericError() {
        Page page = anonymousPage();
        new CatalogPage(page).open();
        new NavBar(page).clickLogin();

        KeycloakLoginPage login = new KeycloakLoginPage(page);
        login.loginAs("no.such.user." + System.nanoTime(), "whatever123");

        assertThat(login.errorMessage()).containsText("Invalid username or password");
    }

    @Test
    @DisplayName("Lege velden: geen login, Keycloak blijft op het formulier")
    void emptyCredentialsAreRejected() {
        Page page = anonymousPage();
        new CatalogPage(page).open();
        new NavBar(page).clickLogin();

        KeycloakLoginPage login = new KeycloakLoginPage(page);
        login.loginAs("", "");

        assertThat(login.usernameInput()).isVisible();
        assertThat(login.errorMessage()).isVisible();
    }
}

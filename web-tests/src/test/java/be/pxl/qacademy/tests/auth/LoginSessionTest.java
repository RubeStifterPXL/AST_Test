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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-05 (tweede aanpak): Als gebruiker log ik in en uit via Keycloak.
 *
 * <p>Andere invalshoek dan {@link LoginLogoutTest}: het sessiegedrag. Inloggen voor elke rol,
 * terugkeren naar de oorspronkelijk gevraagde pagina na de login (deep link), en na het
 * uitloggen echt uitgelogd zijn (ook bij terugkeren naar een beveiligde pagina).</p>
 */
@Tag("auth")
@Tag("US-05")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-05 Sessiegedrag: elke rol, deep link na login, echt uitgelogd")
class LoginSessionTest extends BaseUiTest {

    @ParameterizedTest(name = "{0}")
    @EnumSource(TestUser.class)
    @DisplayName("Elke testgebruiker kan inloggen; de navbar toont zijn gebruikersnaam")
    void everyRoleCanLogIn(TestUser user) {
        Page page = anonymousPage();
        new CatalogPage(page).open();
        new NavBar(page).clickLogin();

        new KeycloakLoginPage(page).loginAs(user.username(), user.password());

        new NavBar(page).waitUntilLoggedInAs(user.username());
        assertThat(page).hasURL(Pattern.compile("^" + Pattern.quote(baseUrl())));
    }

    @Test
    @DisplayName("Deep link: /my-attendance openen zonder login, na het inloggen staat de student op die pagina")
    void loginReturnsToRequestedPage() {
        Page page = anonymousPage();
        page.navigate(baseUrl() + "/my-attendance");

        new KeycloakLoginPage(page).loginAs(TestUser.STUDENT_2.username(), TestUser.STUDENT_2.password());

        assertThat(page).hasURL(Pattern.compile("/my-attendance"));
        assertThat(page.getByTestId("my-attendance-page")).isVisible();
        new NavBar(page).waitUntilLoggedInAs(TestUser.STUDENT_2.username());
    }

    @Test
    @DisplayName("Na uitloggen vraagt een beveiligde pagina opnieuw om in te loggen (geen sessie meer)")
    void logoutEndsTheSession() {
        Page page = anonymousPage();
        new CatalogPage(page).open();
        NavBar nav = new NavBar(page);
        nav.clickLogin();
        new KeycloakLoginPage(page).loginAs(TestUser.STUDENT_3.username(), TestUser.STUDENT_3.password());
        nav.waitUntilLoggedInAs(TestUser.STUDENT_3.username());

        nav.logout();
        assertThat(nav.loginButton()).isVisible();
        page.navigate(baseUrl() + "/my-subscriptions");

        new KeycloakLoginPage(page).waitUntilShown();
        assertThat(page.getByTestId("my-subscriptions-page")).hasCount(0);
    }

    @Test
    @DisplayName("Na herladen van de pagina blijft de gebruiker ingelogd (sessie overleeft een refresh)")
    void sessionSurvivesReload() {
        Page page = pageAs(TestUser.STUDENT_1);
        new CatalogPage(page).open();
        new NavBar(page).waitUntilLoggedInAs(TestUser.STUDENT_1.username());

        page.reload();

        new NavBar(page).waitUntilLoggedInAs(TestUser.STUDENT_1.username());
    }

    @Test
    @DisplayName("Wachtwoord is hoofdlettergevoelig: hetzelfde wachtwoord in kleine letters wordt geweigerd")
    void passwordIsCaseSensitive() {
        Page page = anonymousPage();
        new CatalogPage(page).open();
        new NavBar(page).clickLogin();
        KeycloakLoginPage login = new KeycloakLoginPage(page);

        login.loginAs(TestUser.STUDENT_1.username(), TestUser.STUDENT_1.password().toLowerCase());

        assertThat(login.errorMessage()).containsText("Invalid username or password");
    }
}

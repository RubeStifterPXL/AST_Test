package be.pxl.qacademy.tests.admin;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.factories.RunId;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.AdminUsersPage;
import be.pxl.qacademy.pages.CatalogPage;
import be.pxl.qacademy.pages.KeycloakLoginPage;
import be.pxl.qacademy.pages.NavBar;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.Page;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-17 (deel gebruikers): Als admin beheer ik gebruikers (aanmaken van student/instructor,
 * nooit een admin).
 */
@Tag("admin")
@Tag("US-17")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-17 Gebruikers beheren als admin")
class AdminUserTest extends BaseUiTest {

    /** Wachtwoord voor een account dat de test zelf aanmaakt (bestaat enkel in de testomgeving). */
    private static final String NEW_USER_PASSWORD = "Qa-" + RunId.value() + "-Pw1";

    private record NewUser(String username, String email) {
    }

    private NewUser uniqueUser(String label) {
        String username = "qa." + label + "." + RunId.value() + RunId.next();
        return new NewUser(username, username + "@example.com");
    }

    private void track(NewUser user) {
        JsonNode created = api.findUserByEmail(user.email());
        if (created != null) {
            data.trackUser(created.path("id").asText());
        }
    }

    @Test
    @DisplayName("Student aanmaken: verschijnt in de lijst met rol STUDENT en kan inloggen")
    void createStudentWhoCanLogIn() {
        NewUser user = uniqueUser("student");
        AdminUsersPage users = new AdminUsersPage(pageAs(TestUser.ADMIN)).open();

        users.startNewUser().fill(user.username(), user.email(), "Nieuwe", "Student", NEW_USER_PASSWORD, "STUDENT").save();
        track(user);

        assertThat(users.row(user.email())).isVisible();
        assertThat(users.row(user.email())).containsText("STUDENT");
        assertThat(users.row(user.email())).containsText("Nieuwe Student");

        Page page = anonymousPage();
        new CatalogPage(page).open();
        new NavBar(page).clickLogin();
        new KeycloakLoginPage(page).loginAs(user.username(), NEW_USER_PASSWORD);
        new NavBar(page).waitUntilLoggedInAs(user.username());
        assertThat(new NavBar(page).mySubscriptionsLink()).isVisible();
    }

    @Test
    @DisplayName("Instructor aanmaken: rol INSTRUCTOR")
    void createInstructor() {
        NewUser user = uniqueUser("instructor");
        AdminUsersPage users = new AdminUsersPage(pageAs(TestUser.ADMIN)).open();

        users.startNewUser().fill(user.username(), user.email(), "Nieuwe", "Docent", NEW_USER_PASSWORD, "INSTRUCTOR").save();
        track(user);

        assertThat(users.row(user.email())).containsText("INSTRUCTOR");
    }

    @Test
    @DisplayName("De rolkeuze biedt enkel STUDENT en INSTRUCTOR aan, nooit ADMIN")
    void adminRoleIsNotOffered() {
        AdminUsersPage users = new AdminUsersPage(pageAs(TestUser.ADMIN)).open();
        users.startNewUser();

        Assertions.assertThat(users.roleOptions()).containsExactlyInAnyOrder("STUDENT", "INSTRUCTOR");
    }

    @Test
    @DisplayName("Wachtwoord korter dan 8 tekens wordt geweigerd: geen account")
    void shortPasswordIsRejected() {
        NewUser user = uniqueUser("shortpw");
        AdminUsersPage users = new AdminUsersPage(pageAs(TestUser.ADMIN)).open();

        users.startNewUser().fill(user.username(), user.email(), "Kort", "Wachtwoord", "Ab1!", "STUDENT").save();

        assertThat(users.row(user.email())).hasCount(0);
        Assertions.assertThat(api.findUserByEmail(user.email())).isNull();
    }

    @Test
    @DisplayName("Ongeldige gebruikersnaam (te kort) toont de validatiemelding")
    void invalidUsernameShowsValidation() {
        AdminUsersPage users = new AdminUsersPage(pageAs(TestUser.ADMIN)).open();

        users.startNewUser().fill("ab", "ab." + RunId.next() + "@example.com", "A", "B", NEW_USER_PASSWORD, "STUDENT").save();

        assertThat(users.usernameError()).isVisible();
    }

    @Test
    @DisplayName("Uitgeschakeld account kan niet meer inloggen")
    void disabledUserCannotLogIn() {
        NewUser user = uniqueUser("disabled");
        JsonNode created = api.createUser(user.username(), user.email(), "Uit", "Geschakeld", NEW_USER_PASSWORD, "STUDENT");
        data.trackUser(created.path("id").asText());

        AdminUsersPage users = new AdminUsersPage(pageAs(TestUser.ADMIN)).open();
        assertThat(users.toggleButton(user.email())).hasText("Disable");
        users.toggleEnabled(user.email());
        assertThat(users.toggleButton(user.email())).hasText("Enable");

        Page page = anonymousPage();
        new CatalogPage(page).open();
        new NavBar(page).clickLogin();
        KeycloakLoginPage login = new KeycloakLoginPage(page);
        login.loginAs(user.username(), NEW_USER_PASSWORD);
        assertThat(login.errorMessage()).isVisible();
        assertThat(new NavBar(page).currentUsername()).hasCount(0);
    }

    @Test
    @DisplayName("Naam wijzigen wordt opgeslagen en getoond")
    void renameUser() {
        NewUser user = uniqueUser("rename");
        JsonNode created = api.createUser(user.username(), user.email(), "Oude", "Naam", NEW_USER_PASSWORD, "STUDENT");
        data.trackUser(created.path("id").asText());

        AdminUsersPage users = new AdminUsersPage(pageAs(TestUser.ADMIN)).open();
        users.editName(user.email(), "Nieuwe", "Naam");

        assertThat(users.row(user.email())).containsText("Nieuwe Naam");
    }
}

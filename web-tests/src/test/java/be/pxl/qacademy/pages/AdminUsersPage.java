package be.pxl.qacademy.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Gebruikersbeheer ("/admin/users"): STUDENT/INSTRUCTOR aanmaken, bewerken, in-/uitschakelen. */
public class AdminUsersPage extends BasePage {

    public AdminUsersPage(Page page) {
        super(page);
    }

    public AdminUsersPage open() {
        open("/admin/users");
        return waitUntilShown();
    }

    public AdminUsersPage waitUntilShown() {
        assertThat(byTestId("admin-users-page")).isVisible();
        waitUntilLoaded();
        return this;
    }

    public AdminUsersPage startNewUser() {
        byTestId("new-user-button").click();
        assertThat(createForm()).isVisible();
        return this;
    }

    public AdminUsersPage fill(String username, String email, String firstName, String lastName,
                               String password, String role) {
        byTestId("user-username-input").fill(username);
        byTestId("user-email-input").fill(email);
        byTestId("user-first-name-input").fill(firstName);
        byTestId("user-last-name-input").fill(lastName);
        byTestId("user-password-input").fill(password);
        byTestId("user-role-select").selectOption(new SelectOption().setValue(role));
        return this;
    }

    public void save() {
        byTestId("save-user-button").click();
    }

    public Locator createForm() {
        return byTestId("create-user-form");
    }

    public Locator formError() {
        return byTestId("form-error-alert");
    }

    public Locator usernameError() {
        return byTestId("user-username-error");
    }

    public Locator saveButton() {
        return byTestId("save-user-button");
    }

    /** Alle waarden waaruit de rol gekozen kan worden. */
    public List<String> roleOptions() {
        return byTestId("user-role-select").locator("option").allInnerTexts().stream().map(String::trim).toList();
    }

    public Locator row(String email) {
        return byTestIdPrefix("admin-user-row-").filter(new Locator.FilterOptions().setHasText(email));
    }

    public void toggleEnabled(String email) {
        row(email).locator("[data-testid^='toggle-enabled-button-']").click();
    }

    public Locator toggleButton(String email) {
        return row(email).locator("[data-testid^='toggle-enabled-button-']");
    }

    public void editName(String email, String firstName, String lastName) {
        row(email).locator("[data-testid^='edit-user-button-']").click();
        assertThat(byTestId("edit-user-form")).isVisible();
        byTestId("user-edit-first-name-input").fill(firstName);
        byTestId("user-edit-last-name-input").fill(lastName);
        byTestId("save-user-edit-button").click();
    }
}

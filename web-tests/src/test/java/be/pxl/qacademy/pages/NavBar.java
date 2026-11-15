package be.pxl.qacademy.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** De navigatiebalk bovenaan elke pagina. */
public class NavBar {

    private final Page page;

    public NavBar(Page page) {
        this.page = page;
    }

    public void clickLogin() {
        loginButton().click();
    }

    public void logout() {
        page.getByTestId("logout-button").click();
    }

    public void waitUntilLoggedInAs(String username) {
        assertThat(currentUsername()).hasText(username);
    }

    public Locator loginButton() {
        return page.getByTestId("login-button");
    }

    public Locator currentUsername() {
        return page.getByTestId("current-username");
    }

    public Locator catalogLink() {
        return page.getByTestId("nav-catalog-link");
    }

    public Locator mySubscriptionsLink() {
        return page.getByTestId("nav-my-subscriptions-link");
    }

    public Locator myExamResultsLink() {
        return page.getByTestId("nav-my-exam-results-link");
    }

    public Locator myAttendanceLink() {
        return page.getByTestId("nav-my-attendance-link");
    }

    public Locator myCertificatesLink() {
        return page.getByTestId("nav-my-certificates-link");
    }

    public Locator myCoursesLink() {
        return page.getByTestId("nav-my-courses-link");
    }

    public Locator adminLink() {
        return page.getByTestId("nav-admin-link");
    }

    public Locator adminUsersLink() {
        return page.getByTestId("nav-admin-users-link");
    }
}

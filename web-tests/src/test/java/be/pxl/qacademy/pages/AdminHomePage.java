package be.pxl.qacademy.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Admin-startpagina ("/admin"). Bewust GEEN methode voor "Reset test data": die knop wist de
 * inschrijvingen van het hele team (ook lopende runs van teamgenoten) en hoort niet in een test.
 */
public class AdminHomePage extends BasePage {

    public AdminHomePage(Page page) {
        super(page);
    }

    public AdminHomePage open() {
        open("/admin");
        assertThat(pageRoot()).isVisible();
        return this;
    }

    public Locator pageRoot() {
        return byTestId("admin-home-page");
    }

    public AdminCoursesPage openCourses() {
        byTestId("admin-home-courses-link").click();
        return new AdminCoursesPage(page).waitUntilShown();
    }

    public AdminLocationsPage openLocations() {
        byTestId("admin-home-locations-link").click();
        return new AdminLocationsPage(page).waitUntilShown();
    }

    public AdminUsersPage openUsers() {
        byTestId("admin-home-users-link").click();
        return new AdminUsersPage(page).waitUntilShown();
    }
}

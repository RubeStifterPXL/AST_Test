package be.pxl.qacademy.tests.auth;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.CatalogPage;
import be.pxl.qacademy.pages.NavBar;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-07: Als instructor zie ik geen admin-functionaliteit.
 */
@Tag("auth")
@Tag("US-07")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-07 Instructor heeft geen toegang tot admin-functies")
class InstructorAuthorizationTest extends BaseUiTest {

    @Test
    @DisplayName("Navbar van een instructor: My courses wel, Admin en Users niet, geen studentlinks")
    void instructorNavigation() {
        Page page = pageAs(TestUser.INSTRUCTOR);
        new CatalogPage(page).open();
        NavBar nav = new NavBar(page);

        nav.waitUntilLoggedInAs(TestUser.INSTRUCTOR.username());
        assertThat(nav.myCoursesLink()).isVisible();
        assertThat(nav.adminLink()).hasCount(0);
        assertThat(nav.adminUsersLink()).hasCount(0);
        assertThat(nav.mySubscriptionsLink()).hasCount(0);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "/admin,                  admin-home-page",
            "/admin/courses,          admin-courses-page",
            "/admin/locations,        admin-locations-page",
            "/admin/users,            admin-users-page",
            "/my-subscriptions,       my-subscriptions-page",
            "/my-certificates,        my-certificates-page"
    })
    @DisplayName("Rechtstreekse URL naar een admin- of studentpagina: niet getoond")
    void directUrlIsBlocked(String path, String pageTestId) {
        Page page = pageAs(TestUser.INSTRUCTOR);
        new CatalogPage(page).open();

        page.navigate(baseUrl() + path);
        new NavBar(page).waitUntilLoggedInAs(TestUser.INSTRUCTOR.username());

        assertThat(page.getByTestId(pageTestId)).hasCount(0);
    }

    @Test
    @DisplayName("Ter controle: de admin zelf ziet de adminpagina wel (de blokkering hangt aan de rol)")
    void adminCanOpenAdmin() {
        Page page = pageAs(TestUser.ADMIN);
        new CatalogPage(page).open();

        page.navigate(baseUrl() + "/admin");

        assertThat(page.getByTestId("admin-home-page")).isVisible();
        assertThat(new NavBar(page).adminLink()).isVisible();
    }
}

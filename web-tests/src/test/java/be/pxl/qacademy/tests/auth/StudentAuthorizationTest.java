package be.pxl.qacademy.tests.auth;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.factories.TestCourse;
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
import org.junit.jupiter.params.provider.CsvSource;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-06: Als student zie ik geen admin- of instructor-functionaliteit; als ik die URL's
 * rechtstreeks open, krijg ik geen toegang.
 */
@Tag("auth")
@Tag("US-06")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-06 Student heeft geen toegang tot admin- en instructorfuncties")
class StudentAuthorizationTest extends BaseUiTest {

    @Test
    @DisplayName("Navbar van een student: eigen links wel, Admin, Users en My courses niet")
    void studentNavigationHidesStaffLinks() {
        Page page = pageAs(TestUser.STUDENT_1);
        new CatalogPage(page).open();
        NavBar nav = new NavBar(page);

        nav.waitUntilLoggedInAs(TestUser.STUDENT_1.username());
        assertThat(nav.mySubscriptionsLink()).isVisible();
        assertThat(nav.myExamResultsLink()).isVisible();
        assertThat(nav.myAttendanceLink()).isVisible();
        assertThat(nav.myCertificatesLink()).isVisible();
        assertThat(nav.adminLink()).hasCount(0);
        assertThat(nav.adminUsersLink()).hasCount(0);
        assertThat(nav.myCoursesLink()).hasCount(0);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "/admin,                  admin-home-page",
            "/admin/courses,          admin-courses-page",
            "/admin/locations,        admin-locations-page",
            "/admin/users,            admin-users-page",
            "/my-courses,             my-courses-page"
    })
    @DisplayName("Rechtstreekse URL naar een admin- of instructorpagina: de pagina wordt niet getoond")
    void directStaffUrlIsBlocked(String path, String pageTestId) {
        Page page = pageAs(TestUser.STUDENT_1);
        new CatalogPage(page).open();

        page.navigate(baseUrl() + path);
        new NavBar(page).waitUntilLoggedInAs(TestUser.STUDENT_1.username());

        assertThat(page.getByTestId(pageTestId)).hasCount(0);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({"subscribers", "attendance", "exam-results"})
    @DisplayName("Rechtstreekse URL naar een staff-pagina van een cursus: niet toegankelijk")
    void directCourseStaffUrlIsBlocked(String subPage) {
        TestCourse course = aCourse().titled("Student blocked " + subPage).create();
        Page page = pageAs(TestUser.STUDENT_1);
        new CatalogPage(page).open();

        page.navigate(baseUrl() + "/courses/" + course.id() + "/" + subPage);
        new NavBar(page).waitUntilLoggedInAs(TestUser.STUDENT_1.username());

        assertThat(page.getByTestId("course-" + subPage + "-page")).hasCount(0);
    }

    @Test
    @DisplayName("Op het cursusdetail ziet een student Subscribe, maar geen Edit course of staff-links")
    void courseDetailShowsOnlyStudentActions() {
        TestCourse course = aCourse().titled("Student detail").create();
        Page page = pageAs(TestUser.STUDENT_1);

        page.navigate(baseUrl() + "/courses/" + course.id());

        assertThat(page.getByTestId("subscribe-button")).isVisible();
        assertThat(page.getByTestId("edit-course-link")).hasCount(0);
        assertThat(page.getByTestId("view-subscribers-link")).hasCount(0);
        assertThat(page.getByTestId("manage-attendance-link")).hasCount(0);
        assertThat(page.getByTestId("record-exam-results-link")).hasCount(0);
    }

    @Test
    @DisplayName("Niet ingelogd: een studentpagina openen stuurt door naar de Keycloak-login")
    void anonymousIsRedirectedToLogin() {
        Page page = anonymousPage();

        page.navigate(baseUrl() + "/my-subscriptions");

        new KeycloakLoginPage(page).waitUntilShown();
        assertThat(page.getByTestId("my-subscriptions-page")).hasCount(0);
    }

    @Test
    @DisplayName("Niet ingelogd: /admin openen stuurt door naar de Keycloak-login")
    void anonymousAdminUrlRedirectsToLogin() {
        Page page = anonymousPage();

        page.navigate(baseUrl() + "/admin");

        new KeycloakLoginPage(page).waitUntilShown();
    }
}

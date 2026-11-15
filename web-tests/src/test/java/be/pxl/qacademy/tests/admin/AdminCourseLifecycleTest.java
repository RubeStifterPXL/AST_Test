package be.pxl.qacademy.tests.admin;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.factories.TestCourse;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.AdminCourseEditPage;
import be.pxl.qacademy.pages.AdminCoursesPage;
import be.pxl.qacademy.pages.CatalogPage;
import be.pxl.qacademy.pages.CourseDetailPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import com.microsoft.playwright.Page;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-16 (tweede aanpak): Als admin beheer ik cursussen en modules.
 *
 * <p>Andere invalshoek dan {@link AdminCourseManagementTest}: het effect van admin-acties op wat
 * bezoekers zien. Een gewijzigde titel en prijs verschijnen in de catalogus. Unpublish haalt
 * een cursus terug naar DRAFT en weer uit de catalogus. Een module die de admin toevoegt, staat
 * meteen op het publieke detail.</p>
 */
@Tag("admin")
@Tag("US-16")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-16 Admin-acties en hun effect op de publieke catalogus")
class AdminCourseLifecycleTest extends BaseUiTest {

    @Test
    @DisplayName("Titel en prijs wijzigen: de catalogus toont de nieuwe titel en prijs, de oude titel niet meer")
    void editIsVisibleInCatalog() {
        TestCourse course = aCourse().titled("Before edit").price("50").create();
        String newTitle = course.title().replace("Before edit", "After edit");

        AdminCourseEditPage edit = new AdminCourseEditPage(pageAs(TestUser.ADMIN)).open(course.id());
        edit.title(newTitle).price("65").save();
        assertThat(edit.actionMessage()).containsText("Sav");

        CatalogPage catalog = new CatalogPage(anonymousPage()).open().search(newTitle);
        catalog.apply();
        Assertions.assertThat(catalog.visibleTitles()).containsExactly(newTitle);
        assertThat(catalog.priceOf(course.id())).containsText("65");
        catalog.search(course.title());
        catalog.apply();
        assertThat(catalog.card(course.id())).hasCount(0);
    }

    @Test
    @DisplayName("Unpublish: status terug naar DRAFT, publiek geen Subscribe meer; opnieuw publiceren herstelt dat")
    void unpublishAndRepublish() {
        TestCourse course = aCourse().titled("Unpublish").create();
        Page admin = pageAs(TestUser.ADMIN);
        Page student = pageAs(TestUser.STUDENT_1);

        AdminCourseEditPage edit = new AdminCourseEditPage(admin).open(course.id());
        edit.unpublish();
        assertThat(edit.statusBadge()).hasText("DRAFT");
        CourseDetailPage asStudent = new CourseDetailPage(student).open(course.id());
        assertThat(asStudent.subscribeButton()).hasCount(0);

        edit.publish();
        assertThat(edit.statusBadge()).hasText("PUBLISHED");
        asStudent.open(course.id());
        assertThat(asStudent.subscribeButton()).isVisible();
    }

    @Test
    @DisplayName("Een module die de admin toevoegt, staat meteen op het publieke cursusdetail")
    void addedModuleIsPublic() {
        TestCourse course = aCourse().titled("Public module").create();

        AdminCourseEditPage edit = new AdminCourseEditPage(pageAs(TestUser.ADMIN)).open(course.id());
        edit.addModule("Publieke module", "Zichtbaar voor iedereen", 1);

        CourseDetailPage detail = new CourseDetailPage(anonymousPage()).open(course.id());
        assertThat(detail.modules()).hasCount(1);
        assertThat(detail.modules().first()).containsText("Publieke module");
    }

    @Test
    @DisplayName("Vanuit de admin-lijst naar bewerken en terug: de lijst toont de nieuwe status")
    void listReflectsStatusChange() {
        TestCourse course = aCourse().titled("List status").create();
        Page admin = pageAs(TestUser.ADMIN);

        AdminCoursesPage list = new AdminCoursesPage(admin).open();
        assertThat(list.row(course.title())).containsText("PUBLISHED");
        AdminCourseEditPage edit = list.edit(course.id());
        edit.cancelCourse();
        assertThat(edit.statusBadge()).hasText("CANCELLED");

        list.open();
        assertThat(list.row(course.title())).containsText("CANCELLED");
    }
}

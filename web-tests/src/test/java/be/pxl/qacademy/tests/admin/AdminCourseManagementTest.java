package be.pxl.qacademy.tests.admin;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.factories.CourseBuilder;
import be.pxl.qacademy.factories.RunId;
import be.pxl.qacademy.factories.TestCourse;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.AdminCourseEditPage;
import be.pxl.qacademy.pages.AdminCoursesPage;
import be.pxl.qacademy.pages.CatalogPage;
import be.pxl.qacademy.pages.CourseDetailPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.Page;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-16: Als admin beheer ik cursussen (aanmaken, wijzigen, publiceren, annuleren, verwijderen)
 * en modules.
 */
@Tag("admin")
@Tag("US-16")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-16 Cursussen en modules beheren als admin")
class AdminCourseManagementTest extends BaseUiTest {

    @Test
    @DisplayName("Nieuwe cursus via het formulier: verschijnt als DRAFT in de lijst, niet in de catalogus")
    void createCourseViaForm() {
        String uid = RunId.next();
        String title = "QA " + RunId.value() + " UI created " + uid;
        String slug = "qa-" + RunId.value() + "-ui-created-" + uid;

        AdminCoursesPage courses = new AdminCoursesPage(pageAs(TestUser.ADMIN)).open();
        courses.startNewCourse().fill(title, slug, "Via de UI", "99", "16", "WAITLIST").save();

        assertThat(courses.row(title)).isVisible();
        assertThat(courses.row(title)).containsText("DRAFT");
        assertThat(courses.row(title)).containsText(slug);
        JsonNode created = api.findCourseBySlug(slug);
        Assertions.assertThat(created).as("cursus bestaat in de backend").isNotNull();
        data.trackCourse(created.path("id").asText());
        Assertions.assertThat(created.path("capacityPolicy").asText()).isEqualTo("WAITLIST");
        Assertions.assertThat(created.path("minAge").asInt()).isEqualTo(16);
    }

    @Test
    @DisplayName("Ongeldige slug (hoofdletters/spaties) wordt client-side geweigerd")
    void invalidSlugIsRejected() {
        String title = "QA " + RunId.value() + " Bad slug " + RunId.next();
        AdminCoursesPage courses = new AdminCoursesPage(pageAs(TestUser.ADMIN)).open();

        courses.startNewCourse().fill(title, "Bad Slug!", null, null, null, "HARD_CAP").save();

        assertThat(courses.slugValidationMessage()).isVisible();
        assertThat(courses.createForm()).isVisible();
        Assertions.assertThat(api.findCourseBySlug("Bad Slug!")).isNull();
    }

    @Test
    @DisplayName("Dubbele slug wordt door de server geweigerd met een foutmelding")
    void duplicateSlugIsRejected() {
        TestCourse existing = aCourse().titled("Existing slug").bare().create();
        AdminCoursesPage courses = new AdminCoursesPage(pageAs(TestUser.ADMIN)).open();

        courses.startNewCourse().fill(existing.title() + " copy", existing.slug(), null, null, null, "HARD_CAP").save();

        assertThat(courses.formError()).isVisible();
        assertThat(courses.row(existing.title() + " copy")).hasCount(0);
    }

    @Test
    @DisplayName("Wijzigen en publiceren: locatie, datums, plaatsen en instructor invullen, daarna Publish")
    void editAndPublish() {
        TestCourse course = aCourse().titled("Edit publish").bare().create();
        LocalDateTime start = LocalDateTime.now().plusDays(40).truncatedTo(ChronoUnit.HOURS).withHour(9);

        Page page = pageAs(TestUser.ADMIN);
        AdminCourseEditPage edit = new AdminCourseEditPage(page).open(course.id());
        assertThat(edit.statusBadge()).hasText("DRAFT");
        edit.price("210")
                .location(CourseBuilder.runLocationName())
                .start(start)
                .end(start.plusHours(3))
                .maxSubscribers(8)
                .assignInstructor(api.userId(TestUser.INSTRUCTOR), true)
                .save();
        assertThat(edit.actionMessage()).containsText("Sav");
        edit.publish();

        assertThat(edit.statusBadge()).hasText("PUBLISHED");
        JsonNode saved = api.getCourse(course.id());
        Assertions.assertThat(saved.path("maxSubscribers").asInt()).isEqualTo(8);
        Assertions.assertThat(saved.path("price").decimalValue()).isEqualByComparingTo("210");
        Assertions.assertThat(saved.path("instructorIds").toString()).contains(api.userId(TestUser.INSTRUCTOR));

        CatalogPage catalog = new CatalogPage(anonymousPage()).open().search(course.title());
        catalog.apply();
        Assertions.assertThat(catalog.visibleTitles()).containsExactly(course.title());
    }

    @Test
    @DisplayName("Publiceren zonder locatie, datums en plaatsen wordt geweigerd en blijft DRAFT")
    void publishWithoutRequiredFieldsFails() {
        TestCourse course = aCourse().titled("Incomplete").bare().create();

        AdminCourseEditPage edit = new AdminCourseEditPage(pageAs(TestUser.ADMIN)).open(course.id());
        edit.publish();

        assertThat(edit.actionMessage()).isVisible();
        edit.open(course.id());
        assertThat(edit.statusBadge()).hasText("DRAFT");
    }

    @Test
    @DisplayName("Annuleren: status CANCELLED en de cursus verdwijnt uit de publieke catalogus")
    void cancelCourse() {
        TestCourse course = aCourse().titled("Admin cancel").create();

        AdminCourseEditPage edit = new AdminCourseEditPage(pageAs(TestUser.ADMIN)).open(course.id());
        edit.cancelCourse();

        assertThat(edit.statusBadge()).hasText("CANCELLED");
        CourseDetailPage detail = new CourseDetailPage(anonymousPage()).open(course.id());
        assertThat(detail.subscribeButton()).hasCount(0);
    }

    @Test
    @DisplayName("Verwijderen van een cursus zonder inschrijvingen: weg uit de lijst")
    void deleteCourseWithoutSubscriptions() {
        TestCourse course = aCourse().titled("Admin delete").bare().create();

        Page page = pageAs(TestUser.ADMIN);
        AdminCourseEditPage edit = new AdminCourseEditPage(page).open(course.id());
        edit.deleteCourse();

        AdminCoursesPage list = new AdminCoursesPage(page).waitUntilShown();
        assertThat(list.row(course.title())).hasCount(0);
        Assertions.assertThat(api.findCourseBySlug(course.slug())).isNull();
    }

    @Test
    @DisplayName("Verwijderen van een cursus met een inschrijving wordt geweigerd (annuleren is het alternatief)")
    void deleteCourseWithSubscriptionIsBlocked() {
        TestCourse course = aCourse().titled("Delete blocked").free().create();
        api.subscribe(TestUser.STUDENT_1, course.id());

        AdminCourseEditPage edit = new AdminCourseEditPage(pageAs(TestUser.ADMIN)).open(course.id());
        edit.deleteCourse();

        assertThat(edit.actionMessage()).isVisible();
        Assertions.assertThat(api.findCourseBySlug(course.slug())).isNotNull();
    }

    @Test
    @DisplayName("Modules: toevoegen, bewerken en verwijderen; de volgorde volgt sequenceIndex")
    void manageModules() {
        TestCourse course = aCourse().titled("Modules").bare().create();

        AdminCourseEditPage edit = new AdminCourseEditPage(pageAs(TestUser.ADMIN)).open(course.id());
        edit.addModule("Tweede module", "seq 2", 2);
        edit.addModule("Eerste module", "seq 1", 1);
        assertThat(edit.moduleRows()).hasCount(2);
        assertThat(edit.moduleRows().first()).containsText("Eerste module");

        edit.editModule("Tweede module", "Tweede module (herzien)", "seq 2 bijgewerkt", 2);
        assertThat(edit.moduleRow("Tweede module (herzien)")).hasCount(1);

        edit.deleteModule("Eerste module");
        assertThat(edit.moduleRows()).hasCount(1);
        assertThat(edit.moduleRows().first()).containsText("Tweede module (herzien)");
    }
}

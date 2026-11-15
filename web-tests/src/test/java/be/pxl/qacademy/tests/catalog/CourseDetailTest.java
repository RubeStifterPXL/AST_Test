package be.pxl.qacademy.tests.catalog;

import be.pxl.qacademy.factories.CourseBuilder;
import be.pxl.qacademy.factories.TestCourse;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.CatalogPage;
import be.pxl.qacademy.pages.CourseDetailPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-04: Als bezoeker bekijk ik het detail van een cursus, inclusief modules.
 */
@Tag("catalog")
@Tag("US-04")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-04 Cursusdetail met modules")
class CourseDetailTest extends BaseUiTest {

    @Test
    @DisplayName("Vanuit de catalogus: titel, beschrijving, prijs, capaciteit, locatie en modules in volgorde")
    void detailShowsAllFieldsAndModulesInOrder() {
        TestCourse course = aCourse().titled("Detail").price("320").maxSubscribers(15).waitlist()
                .description("Leer testen met Playwright.")
                .withModule("Module A - Basis", "Locators en asserties")
                .withModule("Module B - Gevorderd", "Fixtures en traces")
                .create();

        CatalogPage catalog = new CatalogPage(anonymousPage()).open().search(course.title());
        catalog.apply();
        CourseDetailPage detail = catalog.openCourse(course.id());

        assertThat(detail.title()).hasText(course.title());
        assertThat(detail.statusBadge()).hasText("PUBLISHED");
        assertThat(detail.description()).hasText("Leer testen met Playwright.");
        assertThat(detail.price()).hasText("320 EUR");
        assertThat(detail.detailsBlock()).containsText("WAITLIST (15 seats)");
        assertThat(detail.detailsBlock()).containsText(CourseBuilder.runLocationName());
        assertThat(detail.modules()).hasCount(2);
        assertThat(detail.modules().nth(0)).containsText("Module A - Basis");
        assertThat(detail.modules().nth(0)).containsText("Locators en asserties");
        assertThat(detail.modules().nth(1)).containsText("Module B - Gevorderd");
    }

    @Test
    @DisplayName("Modules volgen sequenceIndex, niet de volgorde van aanmaken")
    void modulesFollowSequenceIndex() {
        TestCourse course = aCourse().titled("Ordering").create();
        api.addModule(course.id(), "Derde", "seq 3", 3);
        api.addModule(course.id(), "Eerste", "seq 1", 1);
        api.addModule(course.id(), "Tweede", "seq 2", 2);

        CourseDetailPage detail = new CourseDetailPage(anonymousPage()).open(course.id());

        List<String> texts = detail.moduleTexts();
        Assertions.assertThat(texts).hasSize(3);
        Assertions.assertThat(texts.get(0)).contains("Eerste");
        Assertions.assertThat(texts.get(1)).contains("Tweede");
        Assertions.assertThat(texts.get(2)).contains("Derde");
    }

    @Test
    @DisplayName("Cursus zonder modules toont 'No modules yet.'")
    void courseWithoutModules() {
        TestCourse course = aCourse().titled("No modules").create();

        CourseDetailPage detail = new CourseDetailPage(anonymousPage()).open(course.id());

        assertThat(detail.noModulesMessage()).hasText("No modules yet.");
        assertThat(detail.modules()).hasCount(0);
    }

    @Test
    @DisplayName("Gratis cursus zonder minimumleeftijd: 'Free' en 'None'")
    void freeCourseWithoutMinimumAge() {
        TestCourse course = aCourse().titled("Free detail").free().create();

        CourseDetailPage detail = new CourseDetailPage(anonymousPage()).open(course.id());

        assertThat(detail.price()).hasText("Free");
        assertThat(detail.detailsBlock()).containsText("None");
    }

    @Test
    @DisplayName("Anonieme bezoeker ziet geen Subscribe-knop en geen staff-links")
    void anonymousVisitorHasNoActions() {
        TestCourse course = aCourse().titled("No actions").create();

        CourseDetailPage detail = new CourseDetailPage(anonymousPage()).open(course.id());

        assertThat(detail.subscribeButton()).hasCount(0);
        assertThat(detail.editCourseLink()).hasCount(0);
        assertThat(detail.viewSubscribersLink()).hasCount(0);
    }

    @Test
    @DisplayName("Onbekend cursus-id: foutmelding 'Course not found.'")
    void unknownCourseShowsError() {
        CourseDetailPage detail = new CourseDetailPage(anonymousPage()).open("00000000-0000-4000-8000-000000000000");

        assertThat(detail.errorAlert()).hasText("Course not found.");
        assertThat(detail.title()).hasCount(0);
    }

    @Test
    @DisplayName("Demo-cursus uit de catalogus openen toont zijn detail (lezen van seed-data)")
    void demoCourseDetail() {
        CatalogPage catalog = new CatalogPage(anonymousPage()).open().search("Foundations of Software Testing");
        catalog.apply();
        catalog.cardTitles().first().click();
        CourseDetailPage detail = new CourseDetailPage(catalog.page()).waitUntilShown();

        assertThat(detail.title()).hasText("Foundations of Software Testing");
        assertThat(detail.statusBadge()).hasText("PUBLISHED");
    }
}

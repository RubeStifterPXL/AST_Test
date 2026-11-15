package be.pxl.qacademy.tests.catalog;

import be.pxl.qacademy.factories.RunId;
import be.pxl.qacademy.factories.TestCourse;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.CatalogPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-01: Als bezoeker bekijk ik de gepubliceerde cursussen.
 * Leest de demo-catalogus en controleert met eigen cursussen dat enkel PUBLISHED zichtbaar is.
 */
@Tag("catalog")
@Tag("US-01")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-01 Gepubliceerde cursussen bekijken")
class CatalogBrowseTest extends BaseUiTest {

    @Test
    @DisplayName("Anonieme bezoeker ziet de catalogus met de gepubliceerde demo-cursussen")
    void anonymousVisitorSeesPublishedDemoCourses() {
        CatalogPage catalog = new CatalogPage(anonymousPage()).open();

        assertThat(catalog.cardTitles().first()).isVisible();
        Assertions.assertThat(catalog.visibleTitles()).contains("Foundations of Software Testing", "API Test Automation");
        assertThat(catalog.nav().loginButton()).isVisible();
    }

    @Test
    @DisplayName("Een eigen gepubliceerde cursus verschijnt met titel en prijs")
    void ownPublishedCourseIsListedWithPrice() {
        TestCourse course = aCourse().titled("Published").price("149.50").create();

        CatalogPage catalog = new CatalogPage(anonymousPage()).open().search(course.title());
        catalog.apply();

        Assertions.assertThat(catalog.visibleTitles()).containsExactly(course.title());
        assertThat(catalog.priceOf(course.id())).containsText("149.50");
    }

    @Test
    @DisplayName("Een gratis cursus toont 'Free' in plaats van een prijs")
    void freeCourseShowsFree() {
        TestCourse course = aCourse().titled("Free").free().create();

        CatalogPage catalog = new CatalogPage(anonymousPage()).open().search(course.title());
        catalog.apply();

        assertThat(catalog.priceOf(course.id())).hasText("Free");
    }

    @Test
    @DisplayName("Een DRAFT-cursus staat niet in de publieke catalogus")
    void draftCourseIsNotListed() {
        TestCourse draft = aCourse().titled("Draft").draft().create();

        CatalogPage catalog = new CatalogPage(anonymousPage()).open().search(draft.title());
        catalog.apply();

        assertThat(catalog.card(draft.id())).hasCount(0);
        assertThat(catalog.emptyMessage()).isVisible();
    }

    @Test
    @DisplayName("Een geannuleerde cursus staat niet in de publieke catalogus")
    void cancelledCourseIsNotListed() {
        TestCourse course = aCourse().titled("Cancelled").create();
        api.cancelCourse(course.id()).expectOk();

        CatalogPage catalog = new CatalogPage(anonymousPage()).open().search(course.title());
        catalog.apply();

        assertThat(catalog.card(course.id())).hasCount(0);
    }

    @Test
    @DisplayName("Zoekterm op de run-id geeft enkel cursussen van deze run terug")
    void searchOnRunIdOnlyReturnsOwnCourses() {
        TestCourse first = aCourse().titled("Alpha").create();
        TestCourse second = aCourse().titled("Beta").create();

        CatalogPage catalog = new CatalogPage(anonymousPage()).open().search(RunId.value());
        catalog.apply();

        Assertions.assertThat(catalog.visibleTitles()).contains(first.title(), second.title());
        Assertions.assertThat(catalog.visibleTitles()).allMatch(title -> title.contains(RunId.value()));
    }
}

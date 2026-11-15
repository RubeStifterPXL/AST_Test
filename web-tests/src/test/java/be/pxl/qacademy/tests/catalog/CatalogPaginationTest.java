package be.pxl.qacademy.tests.catalog;

import be.pxl.qacademy.factories.RunId;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.CatalogPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-03: Als bezoeker blader ik door de pagina's van de catalogus.
 * De UI toont 12 cursussen per pagina; de dataset heeft er 13 (eigen cursussen).
 */
@Tag("catalog")
@Tag("US-03")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-03 Bladeren door de catalogus")
class CatalogPaginationTest extends BaseUiTest {

    private static final int PAGE_SIZE = 12;
    private static final int COURSES = PAGE_SIZE + 1;

    private final String prefix = "QA " + RunId.value() + " Paging";
    private final List<String> titles = new ArrayList<>();

    @BeforeAll
    void createThirteenCourses() {
        for (int i = 1; i <= COURSES; i++) {
            titles.add(aSharedCourse().titlePrefix(prefix).titled(String.format("Course %02d", i)).create().title());
        }
    }

    @Test
    @DisplayName("Pagina 1 toont 12 cursussen, pagina 2 de 13e; samen exact de dataset, zonder dubbels")
    void pagesSplitTheResultWithoutOverlap() {
        CatalogPage catalog = new CatalogPage(anonymousPage()).open().search(prefix);
        catalog.apply();

        assertThat(catalog.pagination()).isVisible();
        assertThat(catalog.pageButton(0)).isVisible();
        assertThat(catalog.pageButton(1)).isVisible();
        assertThat(catalog.pageButton(2)).hasCount(0);
        List<String> firstPage = catalog.visibleTitles();
        Assertions.assertThat(firstPage).hasSize(PAGE_SIZE);

        catalog.goToPage(1);
        List<String> secondPage = catalog.visibleTitles();
        Assertions.assertThat(secondPage).hasSize(1);

        Set<String> all = new HashSet<>(firstPage);
        all.addAll(secondPage);
        Assertions.assertThat(all).containsExactlyInAnyOrderElementsOf(titles);
    }

    @Test
    @DisplayName("Standaard gesorteerd op titel (oplopend), ook over de paginagrens heen")
    void sortedByTitleAcrossPages() {
        CatalogPage catalog = new CatalogPage(anonymousPage()).open().search(prefix);
        catalog.apply();
        List<String> shown = new ArrayList<>(catalog.visibleTitles());
        catalog.goToPage(1);
        shown.addAll(catalog.visibleTitles());

        Assertions.assertThat(shown).isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER);
    }

    @Test
    @DisplayName("Terug naar pagina 1 toont opnieuw de eerste 12")
    void backToFirstPage() {
        CatalogPage catalog = new CatalogPage(anonymousPage()).open().search(prefix);
        catalog.apply();
        List<String> firstPage = catalog.visibleTitles();

        catalog.goToPage(1);
        catalog.goToPage(0);

        Assertions.assertThat(catalog.visibleTitles()).isEqualTo(firstPage);
    }

    @Test
    @DisplayName("Een nieuwe zoekopdracht begint terug op pagina 1")
    void newSearchStartsOnFirstPage() {
        CatalogPage catalog = new CatalogPage(anonymousPage()).open().search(prefix);
        catalog.apply();
        catalog.goToPage(1);

        catalog.search(prefix);
        catalog.apply();

        Assertions.assertThat(catalog.visibleTitles()).hasSize(PAGE_SIZE);
    }

    @Test
    @DisplayName("Minder dan 12 resultaten: geen tweede paginaknop")
    void singlePageHasNoSecondButton() {
        CatalogPage catalog = new CatalogPage(anonymousPage()).open().search(titles.get(0));
        catalog.apply();

        Assertions.assertThat(catalog.visibleTitles()).containsExactly(titles.get(0));
        assertThat(catalog.pageButton(1)).hasCount(0);
    }
}

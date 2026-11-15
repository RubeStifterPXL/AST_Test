package be.pxl.qacademy.tests.catalog;

import be.pxl.qacademy.factories.RunId;
import be.pxl.qacademy.factories.TestCourse;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.CatalogPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-02: Als bezoeker filter ik op zoekterm, prijs (min/max), gratis, leeftijd en "upcoming"
 * (ook gecombineerd, ook zonder resultaat, ook ongeldige grenswaarden).
 *
 * <p>Eigen dataset per klasse (alleen gelezen), elke zoekopdracht bevat het unieke voorvoegsel
 * zodat data van andere runs of teamleden het resultaat niet beïnvloedt.</p>
 */
@Tag("catalog")
@Tag("US-02")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-02 Catalogus filteren")
class CatalogFilterTest extends BaseUiTest {

    private final String prefix = "QA " + RunId.value() + " Filter";
    private TestCourse cheapAdults;
    private TestCourse freeForAll;
    private TestCourse expensiveTeens;
    private TestCourse startedAlready;

    @BeforeAll
    void createDataset() {
        cheapAdults = aSharedCourse().titlePrefix(prefix).titled("Cheap adults").price("100").minAge(18).create();
        freeForAll = aSharedCourse().titlePrefix(prefix).titled("Free for all").free().create();
        expensiveTeens = aSharedCourse().titlePrefix(prefix).titled("Expensive teens").price("500").minAge(12).create();
        startedAlready = aSharedCourse().titlePrefix(prefix).titled("Started already").price("250")
                .startsIn(Duration.ofDays(-2)).create();
    }

    private CatalogPage catalog() {
        return new CatalogPage(anonymousPage()).open().search(prefix);
    }

    @Test
    @DisplayName("Zoekterm: enkel de cursussen van de dataset, op titel")
    void searchByTitle() {
        CatalogPage catalog = catalog();
        catalog.apply();

        Assertions.assertThat(catalog.visibleTitles())
                .containsExactlyInAnyOrder(cheapAdults.title(), freeForAll.title(), expensiveTeens.title(), startedAlready.title());
    }

    @Test
    @DisplayName("Zoekterm is niet hoofdlettergevoelig")
    void searchIsCaseInsensitive() {
        CatalogPage catalog = new CatalogPage(anonymousPage()).open().search(cheapAdults.title().toUpperCase());
        catalog.apply();

        Assertions.assertThat(catalog.visibleTitles()).containsExactly(cheapAdults.title());
    }

    @Test
    @DisplayName("Gratis: enkel cursussen zonder prijs")
    void freeOnly() {
        CatalogPage catalog = catalog().freeOnly(true);
        catalog.apply();

        Assertions.assertThat(catalog.visibleTitles()).containsExactly(freeForAll.title());
    }

    @Test
    @DisplayName("Prijs tussen min en max (grenzen inclusief)")
    void priceRangeIsInclusive() {
        CatalogPage catalog = catalog().minPrice("100").maxPrice("250");
        catalog.apply();

        Assertions.assertThat(catalog.visibleTitles())
                .containsExactlyInAnyOrder(cheapAdults.title(), startedAlready.title());
    }

    @Test
    @DisplayName("Leeftijd 15: cursussen zonder minimumleeftijd of met minimum <= 15")
    void ageFilter() {
        CatalogPage catalog = catalog().age("15");
        catalog.apply();

        Assertions.assertThat(catalog.visibleTitles())
                .containsExactlyInAnyOrder(freeForAll.title(), expensiveTeens.title(), startedAlready.title());
    }

    @Test
    @DisplayName("Upcoming: een cursus die al gestart is valt weg")
    void upcomingOnly() {
        CatalogPage catalog = catalog().upcomingOnly(true);
        catalog.apply();

        Assertions.assertThat(catalog.visibleTitles())
                .containsExactlyInAnyOrder(cheapAdults.title(), freeForAll.title(), expensiveTeens.title());
    }

    @Test
    @DisplayName("Gecombineerd: min 50 + max 600 + leeftijd 20 + upcoming")
    void combinedFilters() {
        CatalogPage catalog = catalog().minPrice("50").maxPrice("600").age("20").upcomingOnly(true);
        catalog.apply();

        Assertions.assertThat(catalog.visibleTitles())
                .containsExactlyInAnyOrder(cheapAdults.title(), expensiveTeens.title());
    }

    @Test
    @DisplayName("Combinatie zonder resultaat toont de melding 'No courses match your filters.'")
    void noResultsMessage() {
        CatalogPage catalog = catalog().freeOnly(true).minPrice("100");
        catalog.apply();

        assertThat(catalog.emptyMessage()).hasText("No courses match your filters.");
        assertThat(catalog.cardTitles()).hasCount(0);
    }

    @Test
    @DisplayName("Clear zet alle filters terug en toont opnieuw de volledige catalogus")
    void clearResetsFilters() {
        CatalogPage catalog = catalog().freeOnly(true);
        catalog.apply();
        assertThat(catalog.cardTitles()).hasCount(1);

        catalog.clear();

        assertThat(catalog.page().getByTestId("filter-q-input")).hasValue("");
        assertThat(catalog.page().getByTestId("filter-free-checkbox")).not().isChecked();
        Assertions.assertThat(catalog.cardTitles().count()).isGreaterThan(1);
    }

    @ParameterizedTest(name = "min={0} max={1} age={2}")
    @CsvSource({
            "-1, , ",       // negatieve minimumprijs
            ", -5, ",       // negatieve maximumprijs
            "300, 100, ",   // min > max
            ", , -1",       // leeftijd onder 0
            ", , 121"       // leeftijd boven 120
    })
    @DisplayName("Ongeldige grenswaarden: de API weigert (400) en de UI toont een foutmelding, geen lege lijst")
    void invalidBoundariesShowAnError(String min, String max, String age) {
        CatalogPage catalog = catalog();
        if (min != null) {
            catalog.minPrice(min);
        }
        if (max != null) {
            catalog.maxPrice(max);
        }
        if (age != null) {
            catalog.age(age);
        }

        int status = catalog.apply();

        Assertions.assertThat(status).as("HTTP-status van de catalogus-call").isEqualTo(400);
        assertThat(catalog.errorAlert()).isVisible();
        assertThat(catalog.emptyMessage()).hasCount(0);
    }

    @ParameterizedTest(name = "age={0}")
    @CsvSource({"0", "120"})
    @DisplayName("Geldige randwaarden voor leeftijd (0 en 120) worden aanvaard")
    void validAgeBoundaries(String age) {
        CatalogPage catalog = catalog().age(age);

        int status = catalog.apply();

        Assertions.assertThat(status).isEqualTo(200);
        assertThat(catalog.errorAlert()).hasCount(0);
    }
}

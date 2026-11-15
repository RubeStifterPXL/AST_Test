package be.pxl.qacademy.tests.catalog;

import be.pxl.qacademy.api.ApiClient;
import be.pxl.qacademy.factories.RunId;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.CatalogPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * US-02 (tweede aanpak): Als bezoeker filter ik de catalogus.
 *
 * <p>Andere invalshoek dan {@link CatalogFilterTest}: grenswaardenanalyse op de prijs rond één
 * vaste prijs (99.99 / 100 / 100.01) en een orakel. Voor elke filtercombinatie moet wat de UI
 * toont exact gelijk zijn aan wat de publieke API voor dezelfde filter teruggeeft.</p>
 */
@Tag("catalog")
@Tag("US-02")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-02 Catalogus filteren: prijsgrenzen en UI = API")
class CatalogFilterConsistencyTest extends BaseUiTest {

    private final String prefix = "QA " + RunId.value() + " Boundary";
    private String hundred;
    private String justBelow;
    private String justAbove;

    @BeforeAll
    void createPricePoints() {
        justBelow = aSharedCourse().titlePrefix(prefix).titled("Price 99.99").price("99.99").create().title();
        hundred = aSharedCourse().titlePrefix(prefix).titled("Price 100").price("100").minAge(16).create().title();
        justAbove = aSharedCourse().titlePrefix(prefix).titled("Price 100.01").price("100.01").create().title();
    }

    @ParameterizedTest(name = "min={0} max={1} -> {2}")
    @CsvSource(delimiter = ';', value = {
            "100;    ;      100|100.01",
            "100.01; ;      100.01",
            ";       100;   99.99|100",
            ";       99.99; 99.99",
            "100;    100;   100",
            "99.99;  100.01;99.99|100|100.01",
            "100.02; ;      -"
    })
    @DisplayName("Grenswaarden rond 100 EUR: min en max zijn inclusief")
    void priceBoundaries(String min, String max, String expected) {
        CatalogPage catalog = new CatalogPage(anonymousPage()).open().search(prefix);
        if (min != null) {
            catalog.minPrice(min);
        }
        if (max != null) {
            catalog.maxPrice(max);
        }

        catalog.apply();

        Assertions.assertThat(catalog.visibleTitles())
                .containsExactlyInAnyOrderElementsOf(expectedTitles(expected));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = ';', value = {
            "alle;               ;       ;      ;     false",
            "min 100;            100;    ;      ;     false",
            "max 100;            ;       100;   ;     false",
            "leeftijd 15;        ;       ;      15;   false",
            "leeftijd 16;        ;       ;      16;   false",
            "min+max+upcoming;   99.99;  100;   ;     true"
    })
    @DisplayName("Orakel: de UI toont exact dezelfde cursussen als GET /api/public/courses met dezelfde filter")
    void uiMatchesApi(String label, String min, String max, String age, boolean upcoming) {
        CatalogPage catalog = new CatalogPage(anonymousPage()).open().search(prefix).upcomingOnly(upcoming);
        StringBuilder query = new StringBuilder("/api/public/courses?size=50&q=")
                .append(URLEncoder.encode(prefix, StandardCharsets.UTF_8));
        if (min != null) {
            catalog.minPrice(min);
            query.append("&minPrice=").append(min);
        }
        if (max != null) {
            catalog.maxPrice(max);
            query.append("&maxPrice=").append(max);
        }
        if (age != null) {
            catalog.age(age);
            query.append("&age=").append(age);
        }
        if (upcoming) {
            query.append("&upcoming=true");
        }

        catalog.apply();
        ApiClient.Response api = ApiClient.get().getAnonymous(query.toString()).expectOk();

        List<String> fromApi = new ArrayList<>();
        for (JsonNode course : api.json().path("content")) {
            fromApi.add(course.path("title").asText());
        }
        Assertions.assertThat(fromApi).as("API-resultaat voor " + label).isNotEmpty();
        Assertions.assertThat(catalog.visibleTitles()).as("UI-resultaat voor " + label)
                .containsExactlyInAnyOrderElementsOf(fromApi);
    }

    private List<String> expectedTitles(String expected) {
        List<String> titles = new ArrayList<>();
        if ("-".equals(expected)) {
            return titles;
        }
        for (String price : expected.split("\\|")) {
            switch (price.trim()) {
                case "99.99" -> titles.add(justBelow);
                case "100" -> titles.add(hundred);
                case "100.01" -> titles.add(justAbove);
                default -> throw new IllegalArgumentException("Onbekend prijspunt: " + price);
            }
        }
        return titles;
    }
}

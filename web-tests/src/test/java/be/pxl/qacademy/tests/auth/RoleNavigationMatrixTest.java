package be.pxl.qacademy.tests.auth;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.CatalogPage;
import be.pxl.qacademy.pages.NavBar;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import com.microsoft.playwright.Page;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * US-06 (tweede aanpak): Als student zie ik geen admin- of instructor-functionaliteit.
 *
 * <p>Andere invalshoek dan {@link StudentAuthorizationTest}: een rollenmatrix met een allowlist.
 * Per rol staat vast welke navigatielinks er zijn, en de navbar moet exact die set tonen. Een
 * nieuwe of verkeerd getoonde link valt zo meteen op, ook als niemand er een aparte test voor
 * schreef.</p>
 */
@Tag("auth")
@Tag("US-06")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-06 Rollenmatrix: exact de navigatielinks die bij de rol horen")
class RoleNavigationMatrixTest extends BaseUiTest {

    private static final String NAV_PREFIX = "nav-";

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = ';', value = {
            "ANONYMOUS;  nav-catalog-link",
            "STUDENT_1;  nav-catalog-link nav-my-subscriptions-link nav-my-exam-results-link nav-my-attendance-link nav-my-certificates-link",
            "INSTRUCTOR; nav-catalog-link nav-my-courses-link",
            "ADMIN;      nav-catalog-link nav-admin-link nav-admin-users-link"
    })
    @DisplayName("De navbar toont exact de toegelaten links voor de rol")
    void navigationMatchesAllowlist(String role, String allowedLinks) {
        Page page = "ANONYMOUS".equals(role) ? anonymousPage() : pageAs(TestUser.valueOf(role));
        new CatalogPage(page).open();
        if (!"ANONYMOUS".equals(role)) {
            new NavBar(page).waitUntilLoggedInAs(TestUser.valueOf(role).username());
        }

        Set<String> shown = visibleNavLinks(page);

        Assertions.assertThat(shown).as("navigatielinks voor " + role)
                .containsExactlyInAnyOrderElementsOf(Arrays.asList(allowedLinks.trim().split("\\s+")));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({"STUDENT_1", "STUDENT_2", "STUDENT_3"})
    @DisplayName("Alle studentaccounts krijgen dezelfde, beperkte navigatie")
    void allStudentsHaveTheSameNavigation(String student) {
        Page page = pageAs(TestUser.valueOf(student));
        new CatalogPage(page).open();
        new NavBar(page).waitUntilLoggedInAs(TestUser.valueOf(student).username());

        Assertions.assertThat(visibleNavLinks(page))
                .doesNotContain("nav-admin-link", "nav-admin-users-link", "nav-my-courses-link", "nav-feature-flags-link");
    }

    /** Alle zichtbare links in de navbar (testid's die met "nav-" beginnen, behalve de toggle). */
    private Set<String> visibleNavLinks(Page page) {
        Object raw = page.getByTestId("main-nav")
                .locator("[data-testid^='" + NAV_PREFIX + "']:visible")
                .evaluateAll("els => els.map(e => e.getAttribute('data-testid'))");
        Set<String> result = new TreeSet<>();
        for (Object id : (List<?>) raw) {
            result.add(String.valueOf(id));
        }
        result.remove("nav-toggle-button");
        return result;
    }
}

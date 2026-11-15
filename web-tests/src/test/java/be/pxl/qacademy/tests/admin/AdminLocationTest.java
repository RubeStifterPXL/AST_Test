package be.pxl.qacademy.tests.admin;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.factories.CourseBuilder;
import be.pxl.qacademy.factories.RunId;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.AdminLocationsPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-17 (deel locaties): Als admin beheer ik locaties.
 */
@Tag("admin")
@Tag("US-17")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-17 Locaties beheren als admin")
class AdminLocationTest extends BaseUiTest {

    private String uniqueName(String label) {
        return "QA " + RunId.value() + " " + label + " " + RunId.next();
    }

    @Test
    @DisplayName("Locatie aanmaken: verschijnt in de tabel met stad en capaciteit")
    void createLocation() {
        String name = uniqueName("Lokaal");
        AdminLocationsPage locations = new AdminLocationsPage(pageAs(TestUser.ADMIN)).open();

        locations.startNewLocation().fill(name, "Elfde-Liniestraat 24", "Hasselt", "Belgium", "30").save();

        assertThat(locations.row(name)).isVisible();
        assertThat(locations.row(name)).containsText("Hasselt");
        assertThat(locations.row(name)).containsText("30");
        JsonNode created = api.findLocationByName(name);
        Assertions.assertThat(created).isNotNull();
        data.trackLocation(created.path("id").asText());
    }

    @Test
    @DisplayName("Locatie bewerken: nieuwe naam en stad worden opgeslagen")
    void editLocation() {
        String name = uniqueName("Oud");
        JsonNode location = api.createLocation(name, "Genk", 20);
        data.trackLocation(location.path("id").asText());
        String newName = name.replace(" Oud ", " Nieuw ");

        AdminLocationsPage locations = new AdminLocationsPage(pageAs(TestUser.ADMIN)).open();
        locations.edit(name);
        locations.fill(newName, "Agoralaan 1", "Diepenbeek", "Belgium", "25").save();

        assertThat(locations.row(newName)).containsText("Diepenbeek");
        assertThat(locations.row(name)).hasCount(0);
    }

    @Test
    @DisplayName("Locatie verwijderen na bevestiging ('Really delete?')")
    void deleteLocation() {
        String name = uniqueName("Weg");
        JsonNode location = api.createLocation(name, "Hasselt", 10);
        data.trackLocation(location.path("id").asText());

        AdminLocationsPage locations = new AdminLocationsPage(pageAs(TestUser.ADMIN)).open();
        locations.delete(name);

        assertThat(locations.row(name)).hasCount(0);
        Assertions.assertThat(api.findLocationByName(name)).isNull();
    }

    @Test
    @DisplayName("Een locatie die door een cursus gebruikt wordt, kan niet verwijderd worden")
    void locationInUseCannotBeDeleted() {
        aCourse().titled("Uses location").create();
        String inUse = CourseBuilder.runLocationName();

        AdminLocationsPage locations = new AdminLocationsPage(pageAs(TestUser.ADMIN)).open();
        locations.delete(inUse);

        assertThat(locations.errorAlert()).isVisible();
        assertThat(locations.row(inUse)).isVisible();
    }

    @Test
    @DisplayName("Verplichte velden leeg: niet opgeslagen")
    void requiredFieldsAreEnforced() {
        String name = uniqueName("Leeg");
        AdminLocationsPage locations = new AdminLocationsPage(pageAs(TestUser.ADMIN)).open();

        locations.startNewLocation().fill(name, "", "", "", null).save();

        assertThat(locations.row(name)).hasCount(0);
        Assertions.assertThat(api.findLocationByName(name)).isNull();
    }
}

package be.pxl.qacademy.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Locatiebeheer ("/admin/locations"). */
public class AdminLocationsPage extends BasePage {

    public AdminLocationsPage(Page page) {
        super(page);
    }

    public AdminLocationsPage open() {
        navigateTo("/admin/locations");
        return waitUntilShown();
    }

    public AdminLocationsPage waitUntilShown() {
        assertThat(byTestId("admin-locations-page")).isVisible();
        waitUntilLoaded();
        return this;
    }

    public AdminLocationsPage startNewLocation() {
        byTestId("new-location-button").click();
        assertThat(form()).isVisible();
        return this;
    }

    public AdminLocationsPage fill(String name, String address, String city, String country, String roomCapacity) {
        byTestId("location-name-input").fill(name);
        byTestId("location-address-input").fill(address);
        byTestId("location-city-input").fill(city);
        byTestId("location-country-input").fill(country);
        byTestId("location-room-capacity-input").fill(roomCapacity == null ? "" : roomCapacity);
        return this;
    }

    public void save() {
        byTestId("save-location-button").click();
    }

    public Locator form() {
        return byTestId("location-form");
    }

    public Locator formError() {
        return byTestId("form-error-alert");
    }

    public Locator saveButton() {
        return byTestId("save-location-button");
    }

    public Locator row(String name) {
        return byTestIdPrefix("admin-location-row-").filter(new Locator.FilterOptions().setHasText(name));
    }

    public void edit(String name) {
        row(name).locator("[data-testid^='edit-location-button-']").click();
        assertThat(form()).isVisible();
    }

    /** Verwijderen vraagt een bevestiging ("Really delete?"). */
    public void delete(String name) {
        Locator row = row(name);
        row.locator("[data-testid^='delete-location-button-']").click();
        row.locator("[data-testid^='confirm-delete-location-button-']").click();
    }
}

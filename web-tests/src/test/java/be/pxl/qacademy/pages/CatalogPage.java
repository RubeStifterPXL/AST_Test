package be.pxl.qacademy.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Publieke catalogus ("/"): filters, kaarten en paginering. */
public class CatalogPage extends BasePage {

    public CatalogPage(Page page) {
        super(page);
    }

    public CatalogPage open() {
        page.waitForResponse(this::isCatalogCall, () -> navigateTo("/"));
        assertThat(byTestId("course-list-page")).isVisible();
        waitUntilLoaded();
        return this;
    }

    public CatalogPage search(String text) {
        byTestId("filter-q-input").fill(text);
        return this;
    }

    public CatalogPage minPrice(String value) {
        byTestId("filter-min-price-input").fill(value);
        return this;
    }

    public CatalogPage maxPrice(String value) {
        byTestId("filter-max-price-input").fill(value);
        return this;
    }

    public CatalogPage age(String value) {
        byTestId("filter-age-input").fill(value);
        return this;
    }

    public CatalogPage freeOnly(boolean checked) {
        byTestId("filter-free-checkbox").setChecked(checked);
        return this;
    }

    public CatalogPage upcomingOnly(boolean checked) {
        byTestId("filter-upcoming-checkbox").setChecked(checked);
        return this;
    }

    /** Klikt op Search en wacht tot de API geantwoord heeft. Geeft de HTTP-status terug. */
    public int apply() {
        Response response = page.waitForResponse(this::isCatalogCall,
                () -> byTestId("apply-filters-button").click());
        waitUntilLoaded();
        return response.status();
    }

    public void clear() {
        page.waitForResponse(this::isCatalogCall, () -> byTestId("clear-filters-button").click());
        waitUntilLoaded();
    }

    public Locator cardTitles() {
        return byTestIdPrefix("course-card-title-");
    }

    public List<String> visibleTitles() {
        return cardTitles().allInnerTexts().stream().map(String::trim).toList();
    }

    public Locator priceOf(String courseId) {
        return byTestId("course-card-price-" + courseId);
    }

    public Locator card(String courseId) {
        return byTestId("course-card-" + courseId);
    }

    public Locator emptyMessage() {
        return byTestId("empty-catalog-message");
    }

    public Locator pagination() {
        return byTestId("catalog-pagination");
    }

    public Locator pageButton(int zeroBasedIndex) {
        return byTestId("page-button-" + zeroBasedIndex);
    }

    public void goToPage(int zeroBasedIndex) {
        page.waitForResponse(this::isCatalogCall, () -> pageButton(zeroBasedIndex).click());
        waitUntilLoaded();
    }

    public CourseDetailPage openCourse(String courseId) {
        byTestId("course-card-title-" + courseId).click();
        return new CourseDetailPage(page).waitUntilShown();
    }

    private boolean isCatalogCall(Response response) {
        return response.url().contains("/api/public/courses?") || response.url().endsWith("/api/public/courses");
    }
}

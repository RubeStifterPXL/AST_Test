package be.pxl.qacademy.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** "My exam results": eigen examenresultaten (score, geslaagd/niet geslaagd). */
public class MyExamResultsPage extends BasePage {

    public MyExamResultsPage(Page page) {
        super(page);
    }

    public MyExamResultsPage open() {
        navigateTo("/my-exam-results");
        assertThat(byTestId("my-exam-results-page")).isVisible();
        waitUntilLoaded();
        return this;
    }

    public Locator row(String courseTitle) {
        return byTestIdPrefix("exam-result-row-").filter(new Locator.FilterOptions().setHasText(courseTitle));
    }

    public Locator emptyMessage() {
        return byTestId("empty-exam-results-message");
    }
}

package be.pxl.qacademy.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Examenresultaat registreren voor een cursus ("/courses/{id}/exam-results"). */
public class CourseExamResultsPage extends BasePage {

    public CourseExamResultsPage(Page page) {
        super(page);
    }

    public CourseExamResultsPage open(String courseId) {
        navigateTo("/courses/" + courseId + "/exam-results");
        return waitUntilShown();
    }

    public CourseExamResultsPage waitUntilShown() {
        assertThat(byTestId("course-exam-results-page")).isVisible();
        waitUntilLoaded();
        return this;
    }

    public Locator studentSelect() {
        return byTestId("exam-result-student-select");
    }

    /** Kiest de student op zijn zichtbare naam ("Voornaam Achternaam"). */
    public CourseExamResultsPage chooseStudent(String displayName) {
        Locator option = studentSelect().locator("option").filter(new Locator.FilterOptions().setHasText(displayName));
        String label = option.first().innerText().trim();
        studentSelect().selectOption(new SelectOption().setLabel(label));
        return this;
    }

    public CourseExamResultsPage score(int score) {
        byTestId("exam-result-score-input").fill(String.valueOf(score));
        return this;
    }

    public CourseExamResultsPage passed(boolean passed) {
        byTestId("exam-result-passed-checkbox").setChecked(passed);
        return this;
    }

    public void submit() {
        byTestId("submit-exam-result-button").click();
    }

    public void record(String displayName, int score, boolean passed) {
        chooseStudent(displayName).score(score).passed(passed).submit();
    }

    public Locator saveMessage() {
        return byTestId("exam-result-save-message");
    }

    public Locator noSubscribersMessage() {
        return byTestId("no-confirmed-subscribers-message");
    }
}

package be.pxl.qacademy.tests.student;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.factories.TestCourse;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.MyAttendancePage;
import be.pxl.qacademy.pages.MyExamResultsPage;
import be.pxl.qacademy.pages.MySubscriptionsPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-09: Als student zie ik mijn inschrijvingen, mijn aanwezigheden en mijn examenresultaten.
 * Setup via de API (inschrijven, aanwezigheid en resultaat door de instructor), controle in de UI.
 */
@Tag("student")
@Tag("US-09")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-09 Eigen inschrijvingen, aanwezigheden en resultaten bekijken")
class MyOverviewTest extends BaseUiTest {

    @Test
    @DisplayName("My subscriptions toont de cursus met status; een andere student ziet ze niet")
    void subscriptionsAreOnlyVisibleToOwner() {
        TestCourse course = aCourse().titled("Mine only").free().create();
        api.subscribe(TestUser.STUDENT_1, course.id());

        MySubscriptionsPage own = new MySubscriptionsPage(pageAs(TestUser.STUDENT_1)).open();
        assertThat(own.item(course.title())).isVisible();
        assertThat(own.status(course.title())).hasText("CONFIRMED");

        MySubscriptionsPage other = new MySubscriptionsPage(pageAs(TestUser.STUDENT_2)).open();
        assertThat(other.items().filter(new Locator.FilterOptions()
                .setHasText(course.title()))).hasCount(0);
    }

    @Test
    @DisplayName("My attendance toont de aanwezigheid die de instructor registreerde")
    void attendanceRecordedByInstructorIsVisible() {
        TestCourse course = aCourse().titled("Attendance view").free().create();
        api.subscribe(TestUser.STUDENT_1, course.id());
        api.markAttendance(TestUser.INSTRUCTOR, course.id(), Map.of(api.userId(TestUser.STUDENT_1), "EXCUSED"));

        MyAttendancePage attendance = new MyAttendancePage(pageAs(TestUser.STUDENT_1)).open();

        assertThat(attendance.row(course.title())).hasCount(1);
        assertThat(attendance.row(course.title())).containsText("EXCUSED");
    }

    @Test
    @DisplayName("My exam results toont score en resultaat; enkel van de student zelf")
    void examResultIsVisibleToOwnerOnly() {
        TestCourse course = aCourse().titled("Exam view").free().create();
        api.subscribe(TestUser.STUDENT_1, course.id());
        api.subscribe(TestUser.STUDENT_2, course.id());
        api.recordExamResult(TestUser.INSTRUCTOR, course.id(), api.userId(TestUser.STUDENT_1), 73, true);

        MyExamResultsPage own = new MyExamResultsPage(pageAs(TestUser.STUDENT_1)).open();
        assertThat(own.row(course.title())).containsText("73");

        MyExamResultsPage other = new MyExamResultsPage(pageAs(TestUser.STUDENT_2)).open();
        assertThat(other.row(course.title())).hasCount(0);
    }

    @Test
    @DisplayName("Een niet-geslaagd resultaat wordt ook getoond (met de score)")
    void failedResultIsAlsoShown() {
        TestCourse course = aCourse().titled("Exam failed view").free().create();
        api.subscribe(TestUser.STUDENT_2, course.id());
        api.recordExamResult(TestUser.INSTRUCTOR, course.id(), api.userId(TestUser.STUDENT_2), 31, false);

        Page page = pageAs(TestUser.STUDENT_2);
        MyExamResultsPage results = new MyExamResultsPage(page).open();

        assertThat(results.row(course.title())).containsText("31");
    }

    @Test
    @DisplayName("Een gecorrigeerd resultaat (upsert) overschrijft de oude score, geen dubbele rij")
    void correctedResultReplacesOldOne() {
        TestCourse course = aCourse().titled("Exam corrected").free().create();
        api.subscribe(TestUser.STUDENT_1, course.id());
        String student = api.userId(TestUser.STUDENT_1);
        api.recordExamResult(TestUser.INSTRUCTOR, course.id(), student, 45, false);
        api.recordExamResult(TestUser.INSTRUCTOR, course.id(), student, 68, true);

        MyExamResultsPage results = new MyExamResultsPage(pageAs(TestUser.STUDENT_1)).open();

        assertThat(results.row(course.title())).hasCount(1);
        assertThat(results.row(course.title())).containsText("68");
    }
}

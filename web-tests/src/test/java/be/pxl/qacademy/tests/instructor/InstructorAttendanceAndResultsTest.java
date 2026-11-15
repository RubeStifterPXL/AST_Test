package be.pxl.qacademy.tests.instructor;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.factories.TestCourse;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.CourseAttendancePage;
import be.pxl.qacademy.pages.CourseExamResultsPage;
import be.pxl.qacademy.pages.MyAttendancePage;
import be.pxl.qacademy.pages.MyExamResultsPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-15: Als instructor registreer ik aanwezigheid en examenresultaten van mijn cursus, en niet
 * die van andermans cursus.
 */
@Tag("instructor")
@Tag("US-15")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-15 Aanwezigheid en resultaten registreren, enkel voor eigen cursus")
class InstructorAttendanceAndResultsTest extends BaseUiTest {

    @Test
    @DisplayName("Aanwezigheid registreren: opgeslagen en zichtbaar voor de student")
    void markAttendanceOnOwnCourse() {
        TestCourse course = aCourse().titled("Mark attendance").free().create();
        api.subscribe(TestUser.STUDENT_1, course.id());
        String student = api.userId(TestUser.STUDENT_1);

        CourseAttendancePage attendance = new CourseAttendancePage(pageAs(TestUser.INSTRUCTOR)).open(course.id());
        assertThat(attendance.errorAlert()).hasCount(0);
        attendance.mark(student, "PRESENT").save();

        assertThat(attendance.saveMessage()).hasText("Attendance saved.");
        MyAttendancePage studentView = new MyAttendancePage(pageAs(TestUser.STUDENT_1)).open();
        assertThat(studentView.row(course.title())).containsText("ABSENT"); // MUTATIE
    }

    @Test
    @DisplayName("Aanwezigheid opnieuw opslaan werkt de bestaande registratie bij (geen dubbel)")
    void attendanceIsUpserted() {
        TestCourse course = aCourse().titled("Upsert attendance").free().create();
        api.subscribe(TestUser.STUDENT_1, course.id());
        String student = api.userId(TestUser.STUDENT_1);

        CourseAttendancePage attendance = new CourseAttendancePage(pageAs(TestUser.INSTRUCTOR)).open(course.id());
        attendance.mark(student, "ABSENT").save();
        assertThat(attendance.saveMessage()).hasText("Attendance saved.");
        attendance.open(course.id());
        attendance.mark(student, "PRESENT").save();
        assertThat(attendance.saveMessage()).hasText("Attendance saved.");

        MyAttendancePage studentView = new MyAttendancePage(pageAs(TestUser.STUDENT_1)).open();
        assertThat(studentView.row(course.title())).hasCount(1);
        assertThat(studentView.row(course.title())).containsText("PRESENT");
    }

    @Test
    @DisplayName("Enkel CONFIRMED inschrijvingen staan op de aanwezigheidslijst (niet PENDING_PAYMENT)")
    void onlyConfirmedSubscribersCanBeMarked() {
        TestCourse course = aCourse().titled("Only confirmed").price("10").create();
        String sub = api.subscribe(TestUser.STUDENT_1, course.id()).path("id").asText();
        api.payExternal(TestUser.STUDENT_1, sub);
        api.subscribe(TestUser.STUDENT_2, course.id());   // blijft PENDING_PAYMENT

        CourseAttendancePage attendance = new CourseAttendancePage(pageAs(TestUser.INSTRUCTOR)).open(course.id());

        assertThat(attendance.rows()).hasCount(1);
        assertThat(attendance.row(api.userId(TestUser.STUDENT_1))).isVisible();
        assertThat(attendance.row(api.userId(TestUser.STUDENT_2))).hasCount(0);
    }

    @Test
    @DisplayName("Examenresultaat registreren: bevestiging en zichtbaar voor de student")
    void recordExamResultOnOwnCourse() {
        TestCourse course = aCourse().titled("Record result").free().create();
        api.subscribe(TestUser.STUDENT_2, course.id());

        CourseExamResultsPage results = new CourseExamResultsPage(pageAs(TestUser.INSTRUCTOR)).open(course.id());
        results.record(api.displayName(TestUser.STUDENT_2), 64, true);

        assertThat(results.saveMessage()).hasText("Exam result recorded.");
        MyExamResultsPage studentView = new MyExamResultsPage(pageAs(TestUser.STUDENT_2)).open();
        assertThat(studentView.row(course.title())).containsText("64");
    }

    @Test
    @DisplayName("Cursus zonder bevestigde inschrijvingen: melding in plaats van een formulier")
    void noConfirmedSubscribers() {
        TestCourse course = aCourse().titled("Empty results").free().create();

        CourseExamResultsPage results = new CourseExamResultsPage(pageAs(TestUser.INSTRUCTOR)).open(course.id());

        assertThat(results.noSubscribersMessage()).hasText("No confirmed subscribers yet.");
    }

    @Test
    @DisplayName("Andermans cursus: de aanwezigheidslijst laadt niet ('you may not be assigned')")
    void cannotMarkAttendanceOnOtherCourse() {
        TestCourse other = aCourse().titled("Foreign attendance").free().withoutInstructor().create();
        api.subscribe(TestUser.STUDENT_1, other.id());

        CourseAttendancePage attendance = new CourseAttendancePage(pageAs(TestUser.INSTRUCTOR)).open(other.id());

        assertThat(attendance.errorAlert()).containsText("you may not be assigned to this course");
        assertThat(attendance.rows()).hasCount(0);
    }

    @Test
    @DisplayName("Andermans cursus: geen examenresultaat te registreren")
    void cannotRecordResultOnOtherCourse() {
        TestCourse other = aCourse().titled("Foreign results").free().withoutInstructor().create();
        api.subscribe(TestUser.STUDENT_1, other.id());

        CourseExamResultsPage results = new CourseExamResultsPage(pageAs(TestUser.INSTRUCTOR)).open(other.id());

        assertThat(results.errorAlert()).isVisible();
        assertThat(results.page().getByTestId("exam-result-form")).hasCount(0);
    }
}

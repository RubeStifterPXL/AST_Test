package be.pxl.qacademy.tests.e2e;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.factories.CourseBuilder;
import be.pxl.qacademy.factories.RunId;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.AdminCourseEditPage;
import be.pxl.qacademy.pages.AdminCoursesPage;
import be.pxl.qacademy.pages.CourseAttendancePage;
import be.pxl.qacademy.pages.CourseDetailPage;
import be.pxl.qacademy.pages.CourseExamResultsPage;
import be.pxl.qacademy.pages.CourseSubscribersPage;
import be.pxl.qacademy.pages.MyAttendancePage;
import be.pxl.qacademy.pages.MyCertificatesPage;
import be.pxl.qacademy.pages.MyCoursesPage;
import be.pxl.qacademy.pages.MyExamResultsPage;
import be.pxl.qacademy.pages.MySubscriptionsPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import be.pxl.qacademy.support.PdfText;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.Page;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * End-to-end scenario: de volledige levenscyclus van een cursus, alles via de browser.
 * <ol>
 *   <li>Admin maakt een cursus met 2 modules, een locatie, start- en einddatum, een instructor en
 *       maximum 2 deelnemers, en publiceert ze.</li>
 *   <li>Twee studenten schrijven zich in en betalen; een derde student kan zich niet meer inschrijven.</li>
 *   <li>De instructor ziet de cursus onder My courses, bekijkt de inschrijvingen en registreert de
 *       aanwezigheid van beide studenten.</li>
 *   <li>De instructor registreert het eindresultaat: student 1 geslaagd, student 2 niet.</li>
 *   <li>Elke student ziet zijn aanwezigheid en zijn resultaat.</li>
 *   <li>De geslaagde student downloadt zijn certificaat; de PDF bevat cursus, modules, instructor en score.</li>
 *   <li>Negatief: de niet-geslaagde student ziet geen certificaat en kan er geen downloaden.</li>
 * </ol>
 * Eén testmethode met benoemde stappen: elke stap bouwt op de vorige, dus losse tests zouden
 * van elkaar afhangen (verboden door de opdracht).
 */
@Tag("e2e")
@Author(Authors.TEAM)
@DisplayName("E2E Levenscyclus van een cursus: aanmaken tot certificaat")
class CourseLifecycleE2ETest extends BaseUiTest {

    private static final String MODULE_1 = "E2E Module 1 - Testontwerp";
    private static final String MODULE_2 = "E2E Module 2 - Automatisatie";
    private static final int PASSED_SCORE = 85;
    private static final int FAILED_SCORE = 40;

    private final String uid = RunId.next();
    private final String title = "QA " + RunId.value() + " E2E Lifecycle " + uid;
    private final String slug = "qa-" + RunId.value() + "-e2e-lifecycle-" + uid;

    private String courseId;

    @Test
    @DisplayName("Admin -> 2 studenten (3e geweigerd) -> instructor -> resultaten -> certificaat")
    void fullCourseLifecycle() {
        String locationName = CourseBuilder.runLocationName();
        String instructorId = api.userId(TestUser.INSTRUCTOR);
        String student1Id = api.userId(TestUser.STUDENT_1);
        String student2Id = api.userId(TestUser.STUDENT_2);
        api.userId(TestUser.STUDENT_3);

        step1AdminCreatesAndPublishesCourse(locationName, instructorId);
        step2TwoStudentsSubscribeAndThirdIsRejected();
        step3InstructorRegistersAttendance(student1Id, student2Id);
        step4InstructorRegistersResults();
        step5StudentsSeeAttendanceAndResult();
        step6PassedStudentDownloadsCertificate();
        step7FailedStudentHasNoCertificate();
    }

    private void step1AdminCreatesAndPublishesCourse(String locationName, String instructorId) {
        Page admin = pageAs(TestUser.ADMIN);
        AdminCoursesPage courses = new AdminCoursesPage(admin).open();
        courses.startNewCourse()
                .fill(title, slug, "End-to-end scenario team 2", "250", null, "HARD_CAP")
                .save();
        assertThat(courses.row(title)).containsText("DRAFT");

        JsonNode created = api.findCourseBySlug(slug);
        Assertions.assertThat(created).as("cursus aangemaakt via UI").isNotNull();
        courseId = created.path("id").asText();
        data.trackCourse(courseId);

        AdminCourseEditPage edit = courses.edit(courseId);
        LocalDateTime start = LocalDateTime.now().plusDays(21).truncatedTo(ChronoUnit.HOURS).withHour(9);
        edit.location(locationName)
                .start(start)
                .end(start.plusHours(7))
                .maxSubscribers(2)
                .assignInstructor(instructorId, true)
                .save();
        assertThat(edit.actionMessage()).containsText("Sav");
        edit.addModule(MODULE_1, "Technieken en testgevallen", 1);
        edit.addModule(MODULE_2, "Playwright en CI", 2);
        edit.publish();
        assertThat(edit.statusBadge()).hasText("PUBLISHED");

        CourseDetailPage publicView = new CourseDetailPage(anonymousPage()).open(courseId);
        assertThat(publicView.title()).hasText(title);
        assertThat(publicView.detailsBlock()).containsText("HARD_CAP (2 seats)");
        assertThat(publicView.detailsBlock()).containsText(locationName);
        assertThat(publicView.modules()).hasCount(2);
    }

    private void step2TwoStudentsSubscribeAndThirdIsRejected() {
        for (TestUser student : new TestUser[]{TestUser.STUDENT_1, TestUser.STUDENT_2}) {
            Page page = pageAs(student);
            CourseDetailPage detail = new CourseDetailPage(page).open(courseId);
            detail.subscribe();
            assertThat(detail.subscribeSuccess()).hasText("Subscribed - status: PENDING_PAYMENT.");

            MySubscriptionsPage mine = new MySubscriptionsPage(page).open();
            mine.payWithCard(title);
            assertThat(mine.status(title)).hasText("CONFIRMED");
        }

        CourseDetailPage third = new CourseDetailPage(pageAs(TestUser.STUDENT_3)).open(courseId);
        third.subscribe();
        assertThat(third.subscribeError()).isVisible();
        assertThat(third.subscribeSuccess()).hasCount(0);
    }

    private void step3InstructorRegistersAttendance(String student1Id, String student2Id) {
        Page instructor = pageAs(TestUser.INSTRUCTOR);
        MyCoursesPage myCourses = new MyCoursesPage(instructor).open();
        assertThat(myCourses.title(courseId)).hasText(title);

        CourseSubscribersPage subscribers = myCourses.openSubscribers(courseId);
        assertThat(subscribers.rows()).hasCount(2);
        assertThat(subscribers.row(student1Id)).containsText("CONFIRMED");
        assertThat(subscribers.row(student2Id)).containsText("CONFIRMED");

        CourseAttendancePage attendance = new MyCoursesPage(instructor).open().openAttendance(courseId);
        attendance.mark(student1Id, "PRESENT").mark(student2Id, "PRESENT").save();
        assertThat(attendance.saveMessage()).hasText("Attendance saved.");
    }

    private void step4InstructorRegistersResults() {
        Page instructor = pageAs(TestUser.INSTRUCTOR);
        CourseExamResultsPage results = new MyCoursesPage(instructor).open().openExamResults(courseId);
        results.record(api.displayName(TestUser.STUDENT_1), PASSED_SCORE, true);
        assertThat(results.saveMessage()).hasText("Exam result recorded.");

        results.open(courseId);
        results.record(api.displayName(TestUser.STUDENT_2), FAILED_SCORE, false);
        assertThat(results.saveMessage()).hasText("Exam result recorded.");
    }

    private void step5StudentsSeeAttendanceAndResult() {
        assertStudentOverview(TestUser.STUDENT_1, PASSED_SCORE);
        assertStudentOverview(TestUser.STUDENT_2, FAILED_SCORE);
    }

    private void assertStudentOverview(TestUser student, int expectedScore) {
        Page page = pageAs(student);
        MyAttendancePage attendance = new MyAttendancePage(page).open();
        assertThat(attendance.row(title)).containsText("PRESENT");
        MyExamResultsPage results = new MyExamResultsPage(page).open();
        assertThat(results.row(title)).containsText(String.valueOf(expectedScore));
    }

    private void step6PassedStudentDownloadsCertificate() {
        MyCertificatesPage certificates = new MyCertificatesPage(pageAs(TestUser.STUDENT_1)).open();
        assertThat(certificates.card(courseId)).isVisible();
        assertThat(certificates.score(courseId)).hasText(String.valueOf(PASSED_SCORE));

        MyCertificatesPage.DownloadedFile pdf = certificates.download(courseId);
        Assertions.assertThat(PdfText.isPdf(pdf.bytes())).isTrue();
        String text = PdfText.extract(pdf.bytes());
        Assertions.assertThat(text)
                .as("inhoud van het certificaat")
                .contains(title)
                .contains(MODULE_1)
                .contains(MODULE_2 + " (bestaat niet)") // MUTATIE
                .contains(api.displayName(TestUser.INSTRUCTOR))
                .contains(String.valueOf(PASSED_SCORE));
    }

    private void step7FailedStudentHasNoCertificate() {
        MyCertificatesPage certificates = new MyCertificatesPage(pageAs(TestUser.STUDENT_2)).open();
        assertThat(certificates.card(courseId)).hasCount(0);
        assertThat(certificates.downloadButton(courseId)).hasCount(0);
        Assertions.assertThat(api.downloadCertificate(TestUser.STUDENT_2, courseId).ok())
                .as("PDF downloaden zonder geslaagd examen")
                .isFalse();
    }
}

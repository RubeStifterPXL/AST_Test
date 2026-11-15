package be.pxl.qacademy.tests.instructor;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.factories.TestCourse;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.CourseSubscribersPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-14: Als instructor bekijk ik de inschrijvingen van mijn cursus; e-mailadressen van
 * studenten zijn niet zichtbaar.
 */
@Tag("instructor")
@Tag("US-14")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-14 Instructor bekijkt inschrijvingen zonder e-mailadressen")
class InstructorEnrollmentsTest extends BaseUiTest {

    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

    @Test
    @DisplayName("Instructor ziet beide ingeschreven studenten van zijn cursus, met naam en status")
    void instructorSeesSubscribers() {
        TestCourse course = aCourse().titled("Roster").free().create();
        api.subscribe(TestUser.STUDENT_1, course.id());
        api.subscribe(TestUser.STUDENT_2, course.id());

        CourseSubscribersPage subscribers = new CourseSubscribersPage(pageAs(TestUser.INSTRUCTOR)).open(course.id());

        assertThat(subscribers.errorAlert()).hasCount(0);
        assertThat(subscribers.rows()).hasCount(2);
        assertThat(subscribers.row(api.userId(TestUser.STUDENT_1))).containsText(api.displayName(TestUser.STUDENT_1));
        assertThat(subscribers.row(api.userId(TestUser.STUDENT_1))).containsText("CONFIRMED");
        assertThat(subscribers.row(api.userId(TestUser.STUDENT_2))).containsText(api.displayName(TestUser.STUDENT_2));
    }

    @Test
    @DisplayName("Instructor ziet geen e-mailadressen (geen kolom Email, geen adres in de tabel)")
    void instructorDoesNotSeeEmailAddresses() {
        TestCourse course = aCourse().titled("No emails").free().create();
        api.subscribe(TestUser.STUDENT_1, course.id());

        CourseSubscribersPage subscribers = new CourseSubscribersPage(pageAs(TestUser.INSTRUCTOR)).open(course.id());
        assertThat(subscribers.rows()).hasCount(1);

        Assertions.assertThat(subscribers.columnHeaders().allInnerTexts())
                .as("kolomtitels voor een instructor")
                .noneMatch(header -> header.trim().equalsIgnoreCase("Email"));
        Assertions.assertThat(EMAIL.matcher(subscribers.visibleText()).find())
                .as("e-mailadres zichtbaar voor instructor: " + subscribers.visibleText())
                .isFalse();
    }

    @Test
    @DisplayName("Ter vergelijking: de admin ziet de inschrijvingen wel met e-mailadres")
    void adminSeesEmailAddresses() {
        TestCourse course = aCourse().titled("Admin emails").free().create();
        api.subscribe(TestUser.STUDENT_1, course.id());

        CourseSubscribersPage subscribers = new CourseSubscribersPage(pageAs(TestUser.ADMIN)).open(course.id());

        assertThat(subscribers.rows()).hasCount(1);
        Assertions.assertThat(EMAIL.matcher(subscribers.visibleText()).find()).isTrue();
    }

    @Test
    @DisplayName("Instructor kan de inschrijvingen van een cursus van iemand anders niet bekijken")
    void instructorCannotSeeOtherCourse() {
        TestCourse other = aCourse().titled("Other roster").free().withoutInstructor().create();
        api.subscribe(TestUser.STUDENT_1, other.id());

        CourseSubscribersPage subscribers = new CourseSubscribersPage(pageAs(TestUser.INSTRUCTOR)).open(other.id());

        assertThat(subscribers.rows()).hasCount(0);
        assertThat(subscribers.errorAlert()).isVisible();
    }
}

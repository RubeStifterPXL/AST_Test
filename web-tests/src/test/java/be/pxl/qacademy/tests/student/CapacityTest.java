package be.pxl.qacademy.tests.student;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.factories.TestCourse;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.CourseDetailPage;
import be.pxl.qacademy.pages.MySubscriptionsPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-10: Als student kan ik me niet inschrijven voor een volle cursus (HARD_CAP) of kom ik op de
 * wachtlijst (WAITLIST).
 */
@Tag("student")
@Tag("US-10")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-10 Volle cursus: HARD_CAP weigert, WAITLIST zet op de wachtlijst")
class CapacityTest extends BaseUiTest {

    @Test
    @DisplayName("HARD_CAP met 1 plaats: de tweede student krijgt een foutmelding en geen inschrijving")
    void hardCapRejectsWhenFull() {
        TestCourse course = aCourse().titled("Hard cap").free().hardCap().maxSubscribers(1).create();
        api.subscribe(TestUser.STUDENT_1, course.id());

        CourseDetailPage detail = new CourseDetailPage(pageAs(TestUser.STUDENT_2)).open(course.id());
        detail.subscribe();

        assertThat(detail.subscribeError()).isVisible();
        assertThat(detail.subscribeSuccess()).hasCount(0);
        MySubscriptionsPage mine = new MySubscriptionsPage(detail.page()).open();
        assertThat(mine.items().filter(new Locator.FilterOptions()
                .setHasText(course.title()))).hasCount(0);
    }

    @Test
    @DisplayName("HARD_CAP grenswaarde: de laatste vrije plaats (2 van 2) lukt nog")
    void lastSeatIsStillAvailable() {
        TestCourse course = aCourse().titled("Last seat").free().hardCap().maxSubscribers(2).create();
        api.subscribe(TestUser.STUDENT_1, course.id());

        CourseDetailPage detail = new CourseDetailPage(pageAs(TestUser.STUDENT_2)).open(course.id());
        detail.subscribe();

        assertThat(detail.subscribeSuccess()).hasText("Subscribed - status: CONFIRMED.");
    }

    @Test
    @DisplayName("HARD_CAP: onbetaalde (PENDING_PAYMENT) inschrijvingen tellen niet mee voor de capaciteit")
    void pendingPaymentsDoNotTakeSeats() {
        TestCourse course = aCourse().titled("Pending seats").price("30").hardCap().maxSubscribers(1).create();
        api.subscribe(TestUser.STUDENT_1, course.id());

        CourseDetailPage detail = new CourseDetailPage(pageAs(TestUser.STUDENT_2)).open(course.id());
        detail.subscribe();

        assertThat(detail.subscribeSuccess()).hasText("Subscribed - status: PENDING_PAYMENT.");
    }

    @Test
    @DisplayName("WAITLIST met 1 plaats: de tweede student komt op de wachtlijst (WAITLISTED)")
    void waitlistQueuesWhenFull() {
        TestCourse course = aCourse().titled("Waitlist").free().waitlist().maxSubscribers(1).create();
        api.subscribe(TestUser.STUDENT_1, course.id());

        CourseDetailPage detail = new CourseDetailPage(pageAs(TestUser.STUDENT_2)).open(course.id());
        detail.subscribe();

        assertThat(detail.subscribeSuccess()).hasText("Subscribed - status: WAITLISTED.");
        MySubscriptionsPage mine = new MySubscriptionsPage(detail.page()).open();
        assertThat(mine.status(course.title())).hasText("WAITLISTED");
    }
}

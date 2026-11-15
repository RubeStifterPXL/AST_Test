package be.pxl.qacademy.tests.student;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.factories.TestCourse;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.CourseDetailPage;
import be.pxl.qacademy.pages.MySubscriptionsPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-10 (tweede aanpak): Als student kan ik me niet inschrijven voor een volle cursus (HARD_CAP)
 * of kom ik op de wachtlijst (WAITLIST).
 *
 * <p>Andere invalshoek dan {@link CapacityTest}: alles via de UI (geen API-setup voor de
 * inschrijvingen) en de capaciteit in een volgorde getest: plaats 1, plaats 2 (= max), plaats 3
 * (= max + 1). Daarnaast: een vrijgekomen plaats bij HARD_CAP is opnieuw beschikbaar.</p>
 */
@Tag("student")
@Tag("US-10")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-10 Capaciteit in volgorde: max-1, max, max+1, en vrijgekomen plaats")
class CapacityBoundaryTest extends BaseUiTest {

    private CourseDetailPage subscribeViaUi(TestUser student, TestCourse course) {
        CourseDetailPage detail = new CourseDetailPage(pageAs(student)).open(course.id());
        detail.subscribe();
        return detail;
    }

    @Test
    @DisplayName("HARD_CAP 2: student 1 en 2 CONFIRMED, student 3 geweigerd")
    void hardCapSequence() {
        TestCourse course = aCourse().titled("Seq hard").free().hardCap().maxSubscribers(2).create();

        assertThat(subscribeViaUi(TestUser.STUDENT_1, course).subscribeSuccess()).containsText("CONFIRMED");
        assertThat(subscribeViaUi(TestUser.STUDENT_2, course).subscribeSuccess()).containsText("CONFIRMED");
        CourseDetailPage third = subscribeViaUi(TestUser.STUDENT_3, course);

        assertThat(third.subscribeError()).isVisible();
        assertThat(third.subscribeSuccess()).hasCount(0);
    }

    @Test
    @DisplayName("WAITLIST 2: student 1 en 2 CONFIRMED, student 3 WAITLISTED")
    void waitlistSequence() {
        TestCourse course = aCourse().titled("Seq wait").free().waitlist().maxSubscribers(2).create();

        assertThat(subscribeViaUi(TestUser.STUDENT_1, course).subscribeSuccess()).containsText("CONFIRMED");
        assertThat(subscribeViaUi(TestUser.STUDENT_2, course).subscribeSuccess()).containsText("CONFIRMED");
        CourseDetailPage third = subscribeViaUi(TestUser.STUDENT_3, course);

        assertThat(third.subscribeSuccess()).hasText("Subscribed - status: WAITLISTED.");
    }

    @Test
    @DisplayName("HARD_CAP 1: na annuleren door de enige deelnemer kan een andere student wel inschrijven")
    void freedHardCapSeatIsAvailableAgain() {
        TestCourse course = aCourse().titled("Seq freed").free().hardCap().maxSubscribers(1).create();
        assertThat(subscribeViaUi(TestUser.STUDENT_1, course).subscribeSuccess()).containsText("CONFIRMED");
        assertThat(subscribeViaUi(TestUser.STUDENT_2, course).subscribeError()).isVisible();

        MySubscriptionsPage first = new MySubscriptionsPage(pageAs(TestUser.STUDENT_1)).open();
        first.cancel(course.title());
        assertThat(first.status(course.title())).hasText("CANCELLED");

        assertThat(subscribeViaUi(TestUser.STUDENT_2, course).subscribeSuccess()).containsText("CONFIRMED");
    }

    @Test
    @DisplayName("HARD_CAP 1 op een betaalde cursus: pas na betaling is de plaats bezet")
    void paidSeatIsTakenOnlyAfterPayment() {
        TestCourse course = aCourse().titled("Seq paid").price("25").hardCap().maxSubscribers(1).create();
        assertThat(subscribeViaUi(TestUser.STUDENT_1, course).subscribeSuccess()).containsText("PENDING_PAYMENT");
        MySubscriptionsPage first = new MySubscriptionsPage(pageAs(TestUser.STUDENT_1)).open();
        first.payWithCard(course.title());
        assertThat(first.status(course.title())).hasText("CONFIRMED");

        CourseDetailPage second = subscribeViaUi(TestUser.STUDENT_2, course);

        assertThat(second.subscribeError()).isVisible();
    }
}

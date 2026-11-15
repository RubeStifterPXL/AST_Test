package be.pxl.qacademy.tests.student;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.factories.TestCourse;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.MySubscriptionsPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-11: Als student kan ik een inschrijving annuleren; een vrijgekomen plaats gaat naar de
 * wachtlijst (bij WAITLIST).
 */
@Tag("student")
@Tag("US-11")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-11 Inschrijving annuleren en doorschuiven van de wachtlijst")
class CancelSubscriptionTest extends BaseUiTest {

    @Test
    @DisplayName("Annuleren zet de status op CANCELLED en de annuleerknop verdwijnt")
    void cancelConfirmedSubscription() {
        TestCourse course = aCourse().titled("Cancel me").free().create();
        api.subscribe(TestUser.STUDENT_1, course.id());

        MySubscriptionsPage mine = new MySubscriptionsPage(pageAs(TestUser.STUDENT_1)).open();
        mine.cancel(course.title());

        assertThat(mine.message(course.title())).hasText("Subscription cancelled.");
        assertThat(mine.status(course.title())).hasText("CANCELLED");
        mine.open();
        assertThat(mine.status(course.title())).hasText("CANCELLED");
        assertThat(mine.cancelButton(course.title())).hasCount(0);
    }

    @Test
    @DisplayName("Een onbetaalde inschrijving kan ook geannuleerd worden")
    void cancelPendingPayment() {
        TestCourse course = aCourse().titled("Cancel pending").price("20").create();
        api.subscribe(TestUser.STUDENT_1, course.id());

        MySubscriptionsPage mine = new MySubscriptionsPage(pageAs(TestUser.STUDENT_1)).open();
        mine.cancel(course.title());

        assertThat(mine.status(course.title())).hasText("CANCELLED");
        assertThat(mine.payWithCardButton(course.title())).hasCount(0);
    }

    @Test
    @DisplayName("Na annuleren kan de student zich opnieuw inschrijven")
    void canResubscribeAfterCancel() {
        TestCourse course = aCourse().titled("Resubscribe").free().create();
        api.subscribe(TestUser.STUDENT_1, course.id());
        MySubscriptionsPage mine = new MySubscriptionsPage(pageAs(TestUser.STUDENT_1)).open();
        mine.cancel(course.title());
        assertThat(mine.status(course.title())).hasText("CANCELLED");

        mine.page().navigate(baseUrl() + "/courses/" + course.id());
        mine.page().getByTestId("subscribe-button").click();

        assertThat(mine.page().getByTestId("subscribe-success-alert")).hasText("Subscribed - status: CONFIRMED.");
    }

    @Test
    @DisplayName("WAITLIST: een vrijgekomen plaats gaat naar de oudste wachtende (gratis -> CONFIRMED)")
    void freedSeatGoesToOldestWaitlistedStudent() throws InterruptedException {
        TestCourse course = aCourse().titled("Promote").free().waitlist().maxSubscribers(1).create();
        api.subscribe(TestUser.STUDENT_1, course.id());
        api.subscribe(TestUser.STUDENT_2, course.id());   // oudste op de wachtlijst
        Thread.sleep(1100);                               // subscribedAt duidelijk verschillend
        api.subscribe(TestUser.STUDENT_3, course.id());   // nieuwste op de wachtlijst

        MySubscriptionsPage first = new MySubscriptionsPage(pageAs(TestUser.STUDENT_1)).open();
        first.cancel(course.title());
        assertThat(first.status(course.title())).hasText("CANCELLED");

        MySubscriptionsPage oldest = new MySubscriptionsPage(pageAs(TestUser.STUDENT_2)).open();
        assertThat(oldest.status(course.title())).hasText("CONFIRMED");
        MySubscriptionsPage newest = new MySubscriptionsPage(pageAs(TestUser.STUDENT_3)).open();
        assertThat(newest.status(course.title())).hasText("WAITLISTED");
    }

    @Test
    @DisplayName("WAITLIST op een betaalde cursus: de doorgeschoven student moet nog betalen (PENDING_PAYMENT)")
    void promotedStudentOnPaidCourseMustPay() {
        TestCourse course = aCourse().titled("Promote paid").price("60").waitlist().maxSubscribers(1).create();
        String sub = api.subscribe(TestUser.STUDENT_1, course.id()).path("id").asText();
        api.payExternal(TestUser.STUDENT_1, sub);
        api.subscribe(TestUser.STUDENT_2, course.id());

        MySubscriptionsPage first = new MySubscriptionsPage(pageAs(TestUser.STUDENT_1)).open();
        first.cancel(course.title());

        MySubscriptionsPage promoted = new MySubscriptionsPage(pageAs(TestUser.STUDENT_2)).open();
        assertThat(promoted.status(course.title())).hasText("PENDING_PAYMENT");
        assertThat(promoted.payWithCardButton(course.title())).isVisible();
    }
}

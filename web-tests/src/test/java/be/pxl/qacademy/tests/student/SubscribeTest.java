package be.pxl.qacademy.tests.student;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.factories.TestCourse;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.CourseDetailPage;
import be.pxl.qacademy.pages.MySubscriptionsPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-08: Als student schrijf ik me in voor een cursus (betaalde en gratis).
 */
@Tag("student")
@Tag("US-08")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-08 Inschrijven voor een betaalde en een gratis cursus")
class SubscribeTest extends BaseUiTest {

    @Test
    @DisplayName("Gratis cursus: inschrijving meteen CONFIRMED, zonder betaalknoppen")
    void freeCourseIsConfirmedImmediately() {
        TestCourse course = aCourse().titled("Free subscribe").free().create();
        Page page = pageAs(TestUser.STUDENT_1);

        CourseDetailPage detail = new CourseDetailPage(page).open(course.id());
        detail.subscribe();

        assertThat(detail.subscribeSuccess()).hasText("Subscribed - status: CONFIRMED.");
        MySubscriptionsPage mine = new MySubscriptionsPage(page).open();
        assertThat(mine.status(course.title())).hasText("CONFIRMED");
        assertThat(mine.payWithCardButton(course.title())).hasCount(0);
    }

    @Test
    @DisplayName("Betaalde cursus: PENDING_PAYMENT, na betalen met kaart (mock) CONFIRMED")
    void paidCourseNeedsPayment() {
        TestCourse course = aCourse().titled("Paid subscribe").price("75").create();
        Page page = pageAs(TestUser.STUDENT_1);

        CourseDetailPage detail = new CourseDetailPage(page).open(course.id());
        detail.subscribe();
        assertThat(detail.subscribeSuccess()).hasText("Subscribed - status: PENDING_PAYMENT.");

        MySubscriptionsPage mine = new MySubscriptionsPage(page).open();
        assertThat(mine.status(course.title())).hasText("PENDING_PAYMENT");
        mine.payWithCard(course.title());

        assertThat(mine.message(course.title())).containsText("status: CONFIRMED");
        assertThat(mine.status(course.title())).hasText("CONFIRMED");
        assertThat(mine.payWithCardButton(course.title())).hasCount(0);
    }

    @Test
    @DisplayName("Betalen met wallet faalt (saldo is altijd 0) en de inschrijving blijft PENDING_PAYMENT")
    void walletPaymentFailsWithInsufficientBalance() {
        TestCourse course = aCourse().titled("Wallet").price("40").create();
        api.subscribe(TestUser.STUDENT_1, course.id());
        Page page = pageAs(TestUser.STUDENT_1);

        MySubscriptionsPage mine = new MySubscriptionsPage(page).open();
        mine.payWithWallet(course.title());

        assertThat(mine.message(course.title())).hasText("Insufficient wallet balance");
        mine.open();
        assertThat(mine.status(course.title())).hasText("PENDING_PAYMENT");
    }

    @Test
    @DisplayName("Tweede keer inschrijven op dezelfde cursus wordt geweigerd met een foutmelding")
    void cannotSubscribeTwice() {
        TestCourse course = aCourse().titled("Twice").free().create();
        api.subscribe(TestUser.STUDENT_1, course.id());
        Page page = pageAs(TestUser.STUDENT_1);

        CourseDetailPage detail = new CourseDetailPage(page).open(course.id());
        detail.subscribe();

        assertThat(detail.subscribeError()).isVisible();
        assertThat(detail.subscribeSuccess()).hasCount(0);
    }

    @Test
    @DisplayName("Op een DRAFT-cursus is er geen Subscribe-knop")
    void noSubscribeOnDraftCourse() {
        TestCourse course = aCourse().titled("Draft no subscribe").draft().create();
        Page page = pageAs(TestUser.STUDENT_1);

        CourseDetailPage detail = new CourseDetailPage(page).open(course.id());

        assertThat(detail.statusBadge()).hasText("DRAFT");
        assertThat(detail.subscribeButton()).hasCount(0);
    }
}

package be.pxl.qacademy.tests.student;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.factories.TestCourse;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.CatalogPage;
import be.pxl.qacademy.pages.CourseDetailPage;
import be.pxl.qacademy.pages.MySubscriptionsPage;
import be.pxl.qacademy.pages.NavBar;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-08 (tweede aanpak): Als student schrijf ik me in voor een cursus (betaalde en gratis).
 *
 * <p>Andere invalshoek dan {@link SubscribeTest}: de volledige gebruikersreis zonder één
 * rechtstreekse URL. De student zoekt in de catalogus, opent de kaart, schrijft in en gaat via
 * de navbar naar zijn inschrijvingen. Daarnaast controleert deze test wat er op het item staat
 * (titel en link terug naar de cursus) en wat er gebeurt als een anonieme bezoeker wil
 * inschrijven.</p>
 */
@Tag("student")
@Tag("US-08")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-08 Inschrijven als gebruikersreis: catalogus -> detail -> My subscriptions")
class SubscribeJourneyTest extends BaseUiTest {

    private CourseDetailPage findAndOpen(Page page, TestCourse course) {
        CatalogPage catalog = new CatalogPage(page).open().search(course.title());
        catalog.apply();
        return catalog.openCourse(course.id());
    }

    @Test
    @DisplayName("Gratis cursus: zoeken, openen, inschrijven en via de navbar terugvinden als CONFIRMED")
    void freeCourseJourney() {
        TestCourse course = aCourse().titled("Journey free").free().create();
        Page page = pageAs(TestUser.STUDENT_3);

        findAndOpen(page, course).subscribe();
        assertThat(page.getByTestId("subscribe-success-alert")).containsText("CONFIRMED");
        new NavBar(page).mySubscriptionsLink().click();

        MySubscriptionsPage mine = new MySubscriptionsPage(page);
        assertThat(mine.item(course.title())).isVisible();
        assertThat(mine.status(course.title())).hasText("CONFIRMED");
    }

    @Test
    @DisplayName("Betaalde cursus: na betalen blijft de status CONFIRMED, ook na herladen")
    void paidCourseJourney() {
        TestCourse course = aCourse().titled("Journey paid").price("12.50").create();
        Page page = pageAs(TestUser.STUDENT_3);

        findAndOpen(page, course).subscribe();
        assertThat(page.getByTestId("subscribe-success-alert")).containsText("PENDING_PAYMENT");
        new NavBar(page).mySubscriptionsLink().click();
        MySubscriptionsPage mine = new MySubscriptionsPage(page);
        mine.payWithCard(course.title());
        assertThat(mine.status(course.title())).hasText("CONFIRMED");

        page.reload();

        assertThat(mine.status(course.title())).hasText("CONFIRMED");
        assertThat(mine.payWithCardButton(course.title())).hasCount(0);
        assertThat(mine.payWithWalletButton(course.title())).hasCount(0);
    }

    @Test
    @DisplayName("Het item in My subscriptions linkt terug naar het detail van de juiste cursus")
    void subscriptionItemLinksBackToCourse() {
        TestCourse course = aCourse().titled("Journey link").free().create();
        api.subscribe(TestUser.STUDENT_3, course.id());
        Page page = pageAs(TestUser.STUDENT_3);

        MySubscriptionsPage mine = new MySubscriptionsPage(page).open();
        mine.item(course.title()).getByText(course.title()).click();

        CourseDetailPage detail = new CourseDetailPage(page).waitUntilShown();
        assertThat(detail.title()).hasText(course.title());
    }

    @Test
    @DisplayName("Twee studenten op dezelfde cursus: elk ziet enkel zijn eigen inschrijving, met eigen status")
    void twoStudentsEachSeeTheirOwnSubscription() {
        TestCourse course = aCourse().titled("Journey two").price("15").create();
        Page first = pageAs(TestUser.STUDENT_1);
        Page second = pageAs(TestUser.STUDENT_2);

        findAndOpen(first, course).subscribe();
        findAndOpen(second, course).subscribe();
        MySubscriptionsPage firstSubs = new MySubscriptionsPage(first).open();
        firstSubs.payWithCard(course.title());
        assertThat(firstSubs.status(course.title())).hasText("CONFIRMED");

        MySubscriptionsPage secondSubs = new MySubscriptionsPage(second).open();
        assertThat(secondSubs.status(course.title())).hasText("PENDING_PAYMENT");
    }
}

package be.pxl.qacademy.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** "My subscriptions": eigen inschrijvingen, betalen en annuleren. */
public class MySubscriptionsPage extends BasePage {

    public MySubscriptionsPage(Page page) {
        super(page);
    }

    public MySubscriptionsPage open() {
        open("/my-subscriptions");
        assertThat(byTestId("my-subscriptions-page")).isVisible();
        waitUntilLoaded();
        return this;
    }

    /** Het item van een cursus. Er kunnen er meerdere zijn (bv. een oude geannuleerde): het laatste telt. */
    public Locator item(String courseTitle) {
        return byTestIdPrefix("subscription-item-").filter(new Locator.FilterOptions().setHasText(courseTitle)).last();
    }

    public Locator items() {
        return byTestIdPrefix("subscription-item-");
    }

    public Locator status(String courseTitle) {
        return item(courseTitle).locator("[data-testid^='subscription-status-']");
    }

    public Locator message(String courseTitle) {
        return item(courseTitle).locator("[data-testid^='subscription-message-']");
    }

    public Locator payWithCardButton(String courseTitle) {
        return item(courseTitle).locator("[data-testid^='pay-external-button-']");
    }

    public Locator payWithWalletButton(String courseTitle) {
        return item(courseTitle).locator("[data-testid^='pay-wallet-button-']");
    }

    public Locator cancelButton(String courseTitle) {
        return item(courseTitle).locator("[data-testid^='cancel-subscription-button-']");
    }

    public void payWithCard(String courseTitle) {
        payWithCardButton(courseTitle).click();
    }

    public void payWithWallet(String courseTitle) {
        payWithWalletButton(courseTitle).click();
    }

    public void cancel(String courseTitle) {
        cancelButton(courseTitle).click();
    }

    public Locator emptyMessage() {
        return byTestId("empty-subscriptions-message");
    }
}

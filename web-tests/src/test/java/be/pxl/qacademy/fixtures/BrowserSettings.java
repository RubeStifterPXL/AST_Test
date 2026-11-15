package be.pxl.qacademy.fixtures;

import be.pxl.qacademy.config.Config;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

/** Kiest en configureert de browser (BROWSER, HEADLESS, SLOW_MO_MS). */
final class BrowserSettings {

    /** Breed genoeg zodat de Bootstrap-navbar niet inklapt (inklappen gebeurt onder 992 px). */
    static final int WIDTH = 1440;
    static final int HEIGHT = 900;

    private BrowserSettings() {
    }

    static Browser launch(Playwright playwright) {
        BrowserType type = switch (Config.browser()) {
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            case "chromium" -> playwright.chromium();
            default -> throw new IllegalArgumentException("Onbekende BROWSER: " + Config.browser());
        };
        return type.launch(new BrowserType.LaunchOptions()
                .setHeadless(Config.headless())
                .setSlowMo(Config.slowMoMs()));
    }

    static Browser.NewContextOptions contextOptions() {
        return new Browser.NewContextOptions()
                .setViewportSize(WIDTH, HEIGHT)
                .setAcceptDownloads(true)
                .setLocale("en-GB")
                .setTimezoneId("Europe/Brussels");
    }
}

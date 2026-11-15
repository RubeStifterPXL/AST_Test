package be.pxl.qacademy.fixtures;

import be.pxl.qacademy.api.QAcademyApi;
import be.pxl.qacademy.config.Config;
import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.factories.CourseBuilder;
import be.pxl.qacademy.factories.TestDataTracker;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Basis voor elke browsertest.
 * <ul>
 *   <li>Eén browser per testklasse, een nieuwe context (schone cookies) per gebruiker per test.</li>
 *   <li>{@link #pageAs(TestUser)} geeft een pagina die al ingelogd is als die rol (fixture).</li>
 *   <li>{@link #anonymousPage()} geeft een pagina zonder login.</li>
 *   <li>{@link #data} ruimt na elke test zijn eigen testdata op, {@link #classData} na de klasse.</li>
 *   <li>Bij falen: trace (.zip) en screenshot per context in target/artifacts (zie {@link FailureArtifacts}).</li>
 * </ul>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(FailureArtifacts.class)
public abstract class BaseUiTest {

    protected final QAcademyApi api = new QAcademyApi();
    protected final TestDataTracker data = new TestDataTracker();
    protected final TestDataTracker classData = new TestDataTracker();

    private Playwright playwright;
    private Browser browser;
    private final Map<String, BrowserContext> contexts = new LinkedHashMap<>();
    private String testName;

    static {
        PlaywrightAssertions.setDefaultAssertionTimeout(Config.timeoutMs());
    }

    @BeforeAll
    void launchBrowser() {
        playwright = Playwright.create();
        browser = BrowserSettings.launch(playwright);
    }

    @BeforeEach
    void rememberTestName(TestInfo info) {
        testName = info.getTestClass().map(Class::getSimpleName).orElse("test") + "-"
                + info.getTestMethod().map(m -> m.getName()).orElse("method")
                + info.getDisplayName().replaceAll("[^A-Za-z0-9]+", "_");
        if (testName.length() > 120) {
            testName = testName.substring(0, 120);
        }
    }

    /** Nieuwe, ingelogde pagina voor deze gebruiker. */
    protected Page pageAs(TestUser user) {
        Path state = AuthState.storageStateFor(browser, user);
        return newPage(user.username(), BrowserSettings.contextOptions().setStorageStatePath(state));
    }

    /** Nieuwe pagina zonder ingelogde gebruiker. */
    protected Page anonymousPage() {
        return newPage("anonymous-" + contexts.size(), BrowserSettings.contextOptions());
    }

    private Page newPage(String label, Browser.NewContextOptions options) {
        BrowserContext context = browser.newContext(options);
        context.setDefaultTimeout(Config.timeoutMs());
        context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true).setSources(true));
        contexts.put(label + "-" + contexts.size(), context);
        return context.newPage();
    }

    protected String baseUrl() {
        return Config.baseUrl();
    }

    protected CourseBuilder aCourse() {
        return CourseBuilder.aCourse(data);
    }

    /** Voor testdata die een hele testklasse deelt (enkel voor tests die ze alleen lezen). */
    protected CourseBuilder aSharedCourse() {
        return CourseBuilder.aCourse(classData);
    }

    /** Wordt door {@link FailureArtifacts} aangeroepen vóór @AfterEach. */
    void saveArtifacts(boolean failed) {
        Path dir = Config.artifactsDir();
        List<String> labels = new ArrayList<>(contexts.keySet());
        for (String label : labels) {
            BrowserContext context = contexts.get(label);
            try {
                if (failed) {
                    for (Page page : context.pages()) {
                        page.screenshot(new Page.ScreenshotOptions().setFullPage(true)
                                .setPath(dir.resolve("screenshots").resolve(testName + "-" + label + ".png")));
                    }
                }
                if (failed || Config.traceAlways()) {
                    context.tracing().stop(new Tracing.StopOptions()
                            .setPath(dir.resolve("traces").resolve(testName + "-" + label + ".zip")));
                } else {
                    context.tracing().stop();
                }
            } catch (RuntimeException e) {
                System.err.println("[artifacts] " + label + ": " + e.getMessage());
            }
        }
    }

    @AfterEach
    void closeContextsAndCleanUp() {
        contexts.values().forEach(context -> {
            try {
                context.close();
            } catch (RuntimeException ignored) {
                // al gesloten
            }
        });
        contexts.clear();
        data.cleanUp();
    }

    @AfterAll
    void closeBrowser() {
        classData.cleanUp();
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}

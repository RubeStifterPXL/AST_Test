package be.pxl.qacademy.fixtures;

import be.pxl.qacademy.config.Config;
import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.pages.KeycloakLoginPage;
import be.pxl.qacademy.pages.NavBar;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

/**
 * Ingelogde sessie per rol: één keer per run echt inloggen via de Keycloak-loginpagina, daarna
 * de cookies (storage state) hergebruiken. Zo test elke test via de echte login, zonder dat elke
 * test opnieuw door Keycloak moet. Bestanden staan in target/.auth (gitignored, bevatten tokens).
 */
public final class AuthState {

    private static final Duration MAX_AGE = Duration.ofMinutes(20);
    private static final Path DIR = Path.of("target", ".auth");
    private static final Map<TestUser, Path> CACHE = new EnumMap<>(TestUser.class);

    private AuthState() {
    }

    public static synchronized Path storageStateFor(Browser browser, TestUser user) {
        Path file = DIR.resolve(Config.browser() + "-" + user.username() + ".json");
        if (CACHE.containsKey(user) && isFresh(file)) {
            return file;
        }
        BrowserContext context = browser.newContext(BrowserSettings.contextOptions());
        try {
            Page page = context.newPage();
            page.setDefaultTimeout(Config.timeoutMs());
            page.navigate(Config.baseUrl() + "/");
            new NavBar(page).clickLogin();
            new KeycloakLoginPage(page).loginAs(user.username(), user.password());
            new NavBar(page).waitUntilLoggedInAs(user.username());
            Files.createDirectories(DIR);
            context.storageState(new BrowserContext.StorageStateOptions().setPath(file));
            CACHE.put(user, file);
            return file;
        } catch (IOException e) {
            throw new IllegalStateException("Kan sessie niet opslaan voor " + user.username(), e);
        } finally {
            context.close();
        }
    }

    private static boolean isFresh(Path file) {
        try {
            FileTime modified = Files.getLastModifiedTime(file);
            return Instant.now().minus(MAX_AGE).isBefore(modified.toInstant());
        } catch (IOException e) {
            return false;
        }
    }
}

package be.pxl.qacademy.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centrale configuratie. Elke waarde komt uit (in deze volgorde):
 * <ol>
 *   <li>een omgevingsvariabele (CI: GitHub secrets/variables),</li>
 *   <li>een lokaal bestand {@code web-tests/.env} (staat in .gitignore),</li>
 *   <li>een veilige standaardwaarde (nooit voor geheimen).</li>
 * </ol>
 * Zo draait exact dezelfde code lokaal en in de pipeline.
 */
public final class Config {

    private static final Map<String, String> DOT_ENV = loadDotEnv();

    private Config() {
    }

    public static String baseUrl() {
        return stripTrailingSlash(get("BASE_URL", "https://qacademyb.duckdns.org"));
    }

    public static String keycloakUrl() {
        return stripTrailingSlash(get("KEYCLOAK_URL", "https://qacademy-auth.duckdns.org"));
    }

    public static String realm() {
        return get("KEYCLOAK_REALM", "qacademy");
    }

    /** Client met "direct access grants", enkel gebruikt voor setup via de API. */
    public static String apiClientId() {
        return get("KEYCLOAK_API_CLIENT_ID", "qacademy-test-tools");
    }

    /** Wachtwoord van de testgebruikers: verplicht, nooit in de repository. */
    public static String password() {
        String value = get("QA_PASSWORD", null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "QA_PASSWORD is niet gezet. Zet de omgevingsvariabele of maak web-tests/.env aan (zie .env.example).");
        }
        return value;
    }

    /** chromium, firefox of webkit. */
    public static String browser() {
        return get("BROWSER", "chromium").toLowerCase();
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("HEADLESS", "true"));
    }

    public static double slowMoMs() {
        return Double.parseDouble(get("SLOW_MO_MS", "0"));
    }

    /** Standaard timeout voor acties en asserties in de browser. */
    public static double timeoutMs() {
        return Double.parseDouble(get("TIMEOUT_MS", "15000"));
    }

    /** Altijd een trace bijhouden (ook bij geslaagde tests). Standaard enkel bij falen. */
    public static boolean traceAlways() {
        return Boolean.parseBoolean(get("TRACE_ALWAYS", "false"));
    }

    public static Path artifactsDir() {
        return Path.of(get("ARTIFACTS_DIR", "target/artifacts"));
    }

    static String get(String key, String defaultValue) {
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        String prop = System.getProperty(key);
        if (prop != null && !prop.isBlank()) {
            return prop.trim();
        }
        String fromFile = DOT_ENV.get(key);
        if (fromFile != null && !fromFile.isBlank()) {
            return fromFile.trim();
        }
        return defaultValue;
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static Map<String, String> loadDotEnv() {
        Map<String, String> values = new HashMap<>();
        for (Path candidate : List.of(Path.of(".env"), Path.of("web-tests", ".env"))) {
            if (!Files.isRegularFile(candidate)) {
                continue;
            }
            try {
                for (String line : Files.readAllLines(candidate)) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                        continue;
                    }
                    int eq = trimmed.indexOf('=');
                    String value = trimmed.substring(eq + 1).trim();
                    if (value.length() >= 2 && (value.startsWith("\"") && value.endsWith("\"")
                            || value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    values.put(trimmed.substring(0, eq).trim(), value);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Kan " + candidate + " niet lezen", e);
            }
            break;
        }
        return values;
    }
}

package be.pxl.qacademy.api;

import be.pxl.qacademy.config.Config;
import be.pxl.qacademy.config.TestUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

/**
 * Dunne HTTP-client voor testsetup en -opruiming via de REST API (toegestaan door de opdracht).
 * Haalt per testgebruiker een token op bij Keycloak (password grant) en houdt het bij tot net
 * voor het verloopt. Asserties over het gedrag van de toepassing gebeuren in de browser, niet hier.
 */
public final class ApiClient {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static ApiClient instance;

    private final Playwright playwright;
    private final APIRequestContext http;
    private final Map<TestUser, Token> tokens = new EnumMap<>(TestUser.class);

    private record Token(String value, Instant expiresAt) {
    }

    private ApiClient() {
        playwright = Playwright.create();
        http = playwright.request().newContext();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            http.dispose();
            playwright.close();
        }));
    }

    public static synchronized ApiClient get() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public synchronized String token(TestUser user) {
        Token cached = tokens.get(user);
        if (cached != null && Instant.now().isBefore(cached.expiresAt().minusSeconds(30))) {
            return cached.value();
        }
        String url = Config.keycloakUrl() + "/realms/" + Config.realm() + "/protocol/openid-connect/token";
        APIResponse response = http.post(url, RequestOptions.create().setForm(FormData.create()
                .set("grant_type", "password")
                .set("client_id", Config.apiClientId())
                .set("username", user.username())
                .set("password", user.password())));
        if (response.status() != 200) {
            throw new IllegalStateException("Token ophalen mislukt voor " + user.username()
                    + ": HTTP " + response.status() + " " + response.text());
        }
        JsonNode body = parse(response.text());
        Token token = new Token(body.get("access_token").asText(),
                Instant.now().plusSeconds(body.path("expires_in").asLong(300)));
        tokens.put(user, token);
        return token.value();
    }

    public Response get(TestUser user, String path) {
        return send("GET", user, path, null);
    }

    public Response post(TestUser user, String path, Object body) {
        return send("POST", user, path, body);
    }

    public Response put(TestUser user, String path, Object body) {
        return send("PUT", user, path, body);
    }

    public Response delete(TestUser user, String path) {
        return send("DELETE", user, path, null);
    }

    /** Zonder token (publieke endpoints). */
    public Response getAnonymous(String path) {
        return send("GET", null, path, null);
    }

    private Response send(String method, TestUser user, String path, Object body) {
        RequestOptions options = RequestOptions.create().setMethod(method).setTimeout(30_000);
        if (user != null) {
            options.setHeader("Authorization", "Bearer " + token(user));
        }
        if (body != null) {
            try {
                options.setHeader("Content-Type", "application/json");
                options.setData(JSON.writeValueAsString(body));
            } catch (Exception e) {
                throw new IllegalArgumentException("Kan body niet serialiseren", e);
            }
        }
        APIResponse response = http.fetch(Config.baseUrl() + path, options);
        return new Response(method + " " + path, response.status(), response.text(), response.body());
    }

    static JsonNode parse(String text) {
        try {
            return text == null || text.isBlank() ? JSON.nullNode() : JSON.readTree(text);
        } catch (Exception e) {
            throw new IllegalStateException("Geen geldige JSON: " + text, e);
        }
    }

    /** Antwoord van de API, met hulp om een verwachte status af te dwingen. */
    public record Response(String request, int status, String text, byte[] bytes) {

        public JsonNode json() {
            return parse(text);
        }

        public boolean ok() {
            return status >= 200 && status < 300;
        }

        /** Voor setup: faalt luid met de echte fout, zodat een setupfout nooit als SUT-bug oogt. */
        public Response expectOk() {
            if (!ok()) {
                throw new IllegalStateException("Setup-request mislukt: " + request + " -> HTTP " + status + " " + text);
            }
            return this;
        }
    }
}

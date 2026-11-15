package be.pxl.qacademy.api;

import be.pxl.qacademy.config.TestUser;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Domeinoperaties op de REST API die tests gebruiken om hun eigen testdata op te zetten en op
 * te ruimen. Geen asserties op SUT-gedrag: die horen in de browsertests.
 */
public final class QAcademyApi {

    private static final Map<TestUser, String> USER_IDS = new ConcurrentHashMap<>();
    private static final Map<TestUser, String> USER_NAMES = new ConcurrentHashMap<>();

    private final ApiClient api = ApiClient.get();

    // ---------- cursussen ----------

    public JsonNode createCourse(Map<String, Object> body) {
        return api.post(TestUser.ADMIN, "/api/admin/courses", body).expectOk().json();
    }

    public JsonNode updateCourse(String courseId, Map<String, Object> body) {
        return api.put(TestUser.ADMIN, "/api/admin/courses/" + courseId, body).expectOk().json();
    }

    public JsonNode getCourse(String courseId) {
        return api.get(TestUser.ADMIN, "/api/admin/courses/" + courseId).expectOk().json();
    }

    public void publishCourse(String courseId) {
        api.post(TestUser.ADMIN, "/api/admin/courses/" + courseId + "/publish", Map.of()).expectOk();
    }

    public ApiClient.Response cancelCourse(String courseId) {
        return api.post(TestUser.ADMIN, "/api/admin/courses/" + courseId + "/cancel", Map.of());
    }

    public ApiClient.Response deleteCourse(String courseId) {
        return api.delete(TestUser.ADMIN, "/api/admin/courses/" + courseId);
    }

    public JsonNode addModule(String courseId, String title, String description, int sequenceIndex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", title);
        body.put("description", description);
        body.put("sequenceIndex", sequenceIndex);
        return api.post(TestUser.ADMIN, "/api/admin/courses/" + courseId + "/modules", body).expectOk().json();
    }

    /** Zoekt een cursus op slug in de admin-lijst (alle statussen). */
    public JsonNode findCourseBySlug(String slug) {
        JsonNode list = api.get(TestUser.ADMIN, "/api/admin/courses").expectOk().json();
        JsonNode items = list.isArray() ? list : list.path("content");
        for (JsonNode course : items) {
            if (slug.equals(course.path("slug").asText())) {
                return course;
            }
        }
        return null;
    }

    // ---------- locaties ----------

    public JsonNode createLocation(String name, String city, Integer roomCapacity) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("address", "Elfde-Liniestraat 24");
        body.put("city", city);
        body.put("country", "Belgium");
        body.put("roomCapacity", roomCapacity);
        return api.post(TestUser.ADMIN, "/api/admin/locations", body).expectOk().json();
    }

    public ApiClient.Response deleteLocation(String locationId) {
        return api.delete(TestUser.ADMIN, "/api/admin/locations/" + locationId);
    }

    public JsonNode findLocationByName(String name) {
        for (JsonNode location : api.get(TestUser.ADMIN, "/api/admin/locations").expectOk().json()) {
            if (name.equals(location.path("name").asText())) {
                return location;
            }
        }
        return null;
    }

    // ---------- gebruikers ----------

    /**
     * ID van een testgebruiker in de applicatie. Een account bestaat pas in de app-database na
     * een eerste geauthenticeerde call (vastgesteld in PE1), daarom eerst "activeren".
     */
    public String userId(TestUser user) {
        return USER_IDS.computeIfAbsent(user, u -> {
            activate(u);
            JsonNode match = findUser(u.username());
            if (match == null) {
                throw new IllegalStateException("Gebruiker " + u.username() + " niet gevonden in GET /api/admin/users");
            }
            USER_NAMES.put(u, (match.path("firstName").asText() + " " + match.path("lastName").asText()).trim());
            return match.path("id").asText();
        });
    }

    /** Voornaam + achternaam zoals de UI ze toont. */
    public String displayName(TestUser user) {
        userId(user);
        return USER_NAMES.get(user);
    }

    private void activate(TestUser user) {
        String path = switch (user.role()) {
            case STUDENT -> "/api/subscriptions/me";
            case INSTRUCTOR -> "/api/instructor/courses";
            case ADMIN -> "/api/admin/users";
        };
        api.get(user, path).expectOk();
    }

    private JsonNode findUser(String username) {
        for (JsonNode candidate : listUsers()) {
            String email = candidate.path("email").asText("").toLowerCase();
            if (email.equals(username.toLowerCase()) || email.startsWith(username.toLowerCase() + "@")) {
                return candidate;
            }
        }
        return null;
    }

    public JsonNode listUsers() {
        JsonNode users = api.get(TestUser.ADMIN, "/api/admin/users").expectOk().json();
        return users.isArray() ? users : users.path("content");
    }

    public JsonNode findUserByEmail(String email) {
        for (JsonNode candidate : listUsers()) {
            if (email.equalsIgnoreCase(candidate.path("email").asText())) {
                return candidate;
            }
        }
        return null;
    }

    public JsonNode createUser(String username, String email, String firstName, String lastName,
                               String password, String role) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", username);
        body.put("email", email);
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("password", password);
        body.put("role", role);
        return api.post(TestUser.ADMIN, "/api/admin/users", body).expectOk().json();
    }

    public void disableUser(String userId) {
        api.post(TestUser.ADMIN, "/api/admin/users/" + userId + "/disable", Map.of());
    }

    // ---------- inschrijvingen, aanwezigheden, resultaten ----------

    public JsonNode subscribe(TestUser student, String courseId) {
        return api.post(student, "/api/subscriptions", Map.of("courseId", courseId)).expectOk().json();
    }

    public ApiClient.Response trySubscribe(TestUser student, String courseId) {
        return api.post(student, "/api/subscriptions", Map.of("courseId", courseId));
    }

    public JsonNode mySubscriptionFor(TestUser student, String courseId) {
        for (JsonNode sub : api.get(student, "/api/subscriptions/me").expectOk().json()) {
            if (courseId.equals(sub.path("courseId").asText()) && !"CANCELLED".equals(sub.path("status").asText())) {
                return sub;
            }
        }
        return null;
    }

    public void payExternal(TestUser student, String subscriptionId) {
        api.post(student, "/api/payments/" + subscriptionId + "/checkout", Map.of("method", "EXTERNAL")).expectOk();
    }

    public void markAttendance(TestUser marker, String courseId, Map<String, String> statusByUserId) {
        List<Map<String, String>> entries = statusByUserId.entrySet().stream()
                .map(e -> Map.of("userId", e.getKey(), "status", e.getValue()))
                .toList();
        api.post(marker, "/api/admin/courses/" + courseId + "/attendance", Map.of("entries", entries)).expectOk();
    }

    public void recordExamResult(TestUser marker, String courseId, String userId, int score, boolean passed) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userId", userId);
        body.put("courseId", courseId);
        body.put("score", score);
        body.put("passed", passed);
        api.post(marker, "/api/admin/exam-results", body).expectOk();
    }

    public ApiClient.Response downloadCertificate(TestUser student, String courseId) {
        return api.get(student, "/api/certificates/me/" + courseId + "/pdf");
    }
}

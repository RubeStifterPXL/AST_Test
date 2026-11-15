package be.pxl.qacademy.factories;

import be.pxl.qacademy.api.QAcademyApi;
import be.pxl.qacademy.config.TestUser;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder voor testcursussen. Standaard: een gratis, gepubliceerde HARD_CAP-cursus met 10
 * plaatsen, over 30 dagen, op de locatie van deze run, met instructor.test als docent en
 * zonder modules. Titel en slug bevatten altijd de run-id.
 *
 * <pre>{@code
 * TestCourse course = CourseBuilder.aCourse(tracker)
 *         .titled("Waitlist")
 *         .waitlist().maxSubscribers(1)
 *         .withModule("Intro", "Kennismaking")
 *         .create();
 * }</pre>
 */
public final class CourseBuilder {

    private static final QAcademyApi API = new QAcademyApi();
    private static String runLocationId;
    private static String runLocationName;

    private final TestDataTracker tracker;
    private final String uniqueId = RunId.next();
    private String label = "Course";
    private String titlePrefix = "QA " + RunId.value();
    private String description = "Automatisch aangemaakt door de web-tests (run " + RunId.value() + ").";
    private BigDecimal price;
    private String capacityPolicy = "HARD_CAP";
    private Integer minAge;
    private Integer maxSubscribers = 10;
    private Instant start = Instant.now().plus(Duration.ofDays(30)).truncatedTo(ChronoUnit.HOURS);
    private Duration length = Duration.ofHours(2);
    private boolean withInstructor = true;
    private boolean withLocation = true;
    private boolean publish = true;
    private final List<String[]> modules = new ArrayList<>();

    private CourseBuilder(TestDataTracker tracker) {
        this.tracker = tracker;
    }

    public static CourseBuilder aCourse(TestDataTracker tracker) {
        return new CourseBuilder(tracker);
    }

    /** Leesbaar deel van de titel; run-id en een unieke suffix worden altijd toegevoegd. */
    public CourseBuilder titled(String label) {
        this.label = label;
        return this;
    }

    /** Gemeenschappelijk voorvoegsel, bv. om een groep cursussen samen terug te vinden. */
    public CourseBuilder titlePrefix(String prefix) {
        this.titlePrefix = prefix;
        return this;
    }

    public CourseBuilder description(String description) {
        this.description = description;
        return this;
    }

    public CourseBuilder price(String euros) {
        this.price = euros == null ? null : new BigDecimal(euros);
        return this;
    }

    public CourseBuilder free() {
        this.price = null;
        return this;
    }

    public CourseBuilder minAge(Integer minAge) {
        this.minAge = minAge;
        return this;
    }

    public CourseBuilder hardCap() {
        this.capacityPolicy = "HARD_CAP";
        return this;
    }

    public CourseBuilder waitlist() {
        this.capacityPolicy = "WAITLIST";
        return this;
    }

    public CourseBuilder maxSubscribers(int max) {
        this.maxSubscribers = max;
        return this;
    }

    public CourseBuilder startsIn(Duration fromNow) {
        this.start = Instant.now().plus(fromNow).truncatedTo(ChronoUnit.MINUTES);
        return this;
    }

    public CourseBuilder withoutInstructor() {
        this.withInstructor = false;
        return this;
    }

    public CourseBuilder withModule(String title, String moduleDescription) {
        modules.add(new String[]{title, moduleDescription});
        return this;
    }

    /** Blijft DRAFT: niet publiceren. */
    public CourseBuilder draft() {
        this.publish = false;
        return this;
    }

    /** Enkel aanmaken (DRAFT), zonder locatie, datums, plaatsen of docent. */
    public CourseBuilder bare() {
        this.publish = false;
        this.withLocation = false;
        this.withInstructor = false;
        this.maxSubscribers = null;
        return this;
    }

    public String title() {
        return titlePrefix + " " + label + " " + uniqueId;
    }

    public String slug() {
        String readable = label.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return ("qa-" + RunId.value() + "-" + readable + "-" + uniqueId).replaceAll("-+", "-");
    }

    public TestCourse create() {
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("title", title());
        createBody.put("slug", slug());
        createBody.put("description", description);
        createBody.put("price", price);
        createBody.put("capacityPolicy", capacityPolicy);
        createBody.put("minAge", minAge);
        JsonNode created = API.createCourse(createBody);
        String id = created.path("id").asText();
        tracker.trackCourse(id);

        List<String> moduleTitles = new ArrayList<>();
        List<String> moduleDescriptions = new ArrayList<>();
        int sequence = 1;
        for (String[] module : modules) {
            API.addModule(id, module[0], module[1], sequence++);
            moduleTitles.add(module[0]);
            moduleDescriptions.add(module[1]);
        }

        String locationName = null;
        if (withLocation || withInstructor || maxSubscribers != null) {
            Map<String, Object> update = new HashMap<>();
            update.put("title", title());
            update.put("description", description);
            update.put("price", price);
            update.put("capacityPolicy", capacityPolicy);
            update.put("minAge", minAge);
            if (withLocation) {
                update.put("locationId", runLocationId());
                locationName = runLocationName;
            }
            update.put("startDateTime", iso(start));
            update.put("endDateTime", iso(start.plus(length)));
            update.put("maxSubscribers", maxSubscribers);
            update.put("instructorIds", withInstructor ? List.of(API.userId(TestUser.INSTRUCTOR)) : List.of());
            API.updateCourse(id, update);
        }

        String status = "DRAFT";
        if (publish) {
            API.publishCourse(id);
            status = "PUBLISHED";
        }
        return new TestCourse(id, title(), slug(), description, price, capacityPolicy, maxSubscribers,
                locationName, List.copyOf(moduleTitles), List.copyOf(moduleDescriptions), status);
    }

    /** Formaat dat de API verwacht, bv. 2026-12-15T10:00:00Z. */
    public static String iso(Instant instant) {
        return instant.truncatedTo(ChronoUnit.SECONDS).toString();
    }

    /**
     * Eén locatie per run, gedeeld door alle cursussen van die run. Een locatie met cursussen
     * kan niet verwijderd worden (US-25), dus per cursus een locatie zou er honderden achterlaten.
     */
    private static synchronized String runLocationId() {
        if (runLocationId == null) {
            runLocationName = "QA Campus " + RunId.value();
            JsonNode existing = API.findLocationByName(runLocationName);
            JsonNode location = existing != null ? existing : API.createLocation(runLocationName, "Hasselt", 40);
            runLocationId = location.path("id").asText();
        }
        return runLocationId;
    }

    public static synchronized String runLocationName() {
        runLocationId();
        return runLocationName;
    }

    public static synchronized String sharedLocationId() {
        return runLocationId();
    }
}

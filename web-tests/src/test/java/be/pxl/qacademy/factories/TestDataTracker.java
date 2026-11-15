package be.pxl.qacademy.factories;

import be.pxl.qacademy.api.ApiClient;
import be.pxl.qacademy.api.QAcademyApi;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Houdt bij welke testdata een test (of testklasse) aanmaakte en ruimt die achteraf op, in
 * omgekeerde volgorde. Opruimen faalt nooit de test zelf: een fout wordt gelogd.
 */
public final class TestDataTracker {

    private final QAcademyApi api = new QAcademyApi();
    private final Deque<Runnable> cleanups = new ArrayDeque<>();

    /**
     * Een cursus zonder inschrijvingen wordt verwijderd. Heeft ze ooit een inschrijving gehad,
     * dan blokkeert de API verwijderen (409, US-16) en wordt ze geannuleerd.
     */
    public void trackCourse(String courseId) {
        cleanups.push(() -> {
            ApiClient.Response deleted = api.deleteCourse(courseId);
            if (!deleted.ok() && deleted.status() != 404) {
                api.cancelCourse(courseId);
            }
        });
    }

    public void trackLocation(String locationId) {
        cleanups.push(() -> api.deleteLocation(locationId));
    }

    public void trackUser(String userId) {
        cleanups.push(() -> api.disableUser(userId));
    }

    public void track(Runnable cleanup) {
        cleanups.push(cleanup);
    }

    public void cleanUp() {
        while (!cleanups.isEmpty()) {
            try {
                cleanups.pop().run();
            } catch (RuntimeException e) {
                System.err.println("[cleanup] " + e.getMessage());
            }
        }
    }
}

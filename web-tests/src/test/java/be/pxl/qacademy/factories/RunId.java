package be.pxl.qacademy.factories;

import java.security.SecureRandom;

/**
 * Unieke id per JVM-run (lokaal of per CI-job). Elke testcursus, slug, locatie en gebruiker
 * draagt deze id, zodat teamleden en parallelle pipelines elkaars data nooit raken en een
 * zoekterm in de catalogus enkel de data van deze run teruggeeft.
 */
public final class RunId {

    private static final String VALUE = create();
    private static final SecureRandom RANDOM = new SecureRandom();

    private RunId() {
    }

    /** Bv. "m3k9x2ab": lowercase letters en cijfers, geldig in een slug. */
    public static String value() {
        return VALUE;
    }

    /** Korte, unieke waarde binnen deze run (bv. voor één testcursus). */
    public static String next() {
        return Long.toString(System.nanoTime() % 1_000_000_000L, 36) + RANDOM.nextInt(36 * 36);
    }

    private static String create() {
        String base = Long.toString(System.currentTimeMillis(), 36);
        int suffix = new SecureRandom().nextInt(36 * 36);
        return (base + Integer.toString(suffix, 36)).toLowerCase();
    }
}

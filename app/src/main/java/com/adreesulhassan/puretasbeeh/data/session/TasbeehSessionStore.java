package com.adreesulhassan.puretasbeeh.data.session;

/**
 * In-RAM session state for tasbeeh counters (clears when the process dies).
 */
public final class TasbeehSessionStore {

    private TasbeehSessionStore() {
    }

    // --- Custom Tasbeeh ---
    public static int customCount;
    public static int customTarget = 33;
    public static boolean customTargetReachedNotified;
    public static String customZikrArabic;
    public static String customZikrTranslit;

    // --- Tasbeeh e Zehra ---
    public static int zehraPhaseOrdinal = 0;
    public static int zehraRemaining = 34;
    public static boolean zehraCompleted;
    public static boolean zehraHasProgress;

    public static void resetCustomCount() {
        customCount = 0;
        customTargetReachedNotified = false;
    }

    public static void resetZehra() {
        zehraPhaseOrdinal = 0;
        zehraRemaining = 34;
        zehraCompleted = false;
        zehraHasProgress = false;
    }

    /** Clear a finished Zehra session when the user leaves the screen. */
    public static void clearZehraIfCompleted() {
        if (zehraCompleted) {
            resetZehra();
        }
    }
}

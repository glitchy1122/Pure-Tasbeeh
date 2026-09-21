package com.adreesulhassan.puretasbeeh.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Hadith of the Day: one fixed hadith per Islamic date, plus a non-repeat pool
 * across days until every eligible hadith has been shown once.
 */
public final class HadithDayPreferences {

    private static final String PREFS = "pure_tasbeeh_prefs";
    private static final String KEY_SHOWN = "hadith_day_shown_ids";
    private static final String KEY_DAY = "hadith_day_islamic_key";
    private static final String KEY_ID = "hadith_day_selected_id";

    private final SharedPreferences prefs;

    public HadithDayPreferences(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    @NonNull
    public Set<Long> getShownIds() {
        Set<String> raw = prefs.getStringSet(KEY_SHOWN, null);
        Set<Long> out = new HashSet<>();
        if (raw == null) {
            return out;
        }
        for (String s : raw) {
            try {
                out.add(Long.parseLong(s));
            } catch (NumberFormatException ignored) {
            }
        }
        return out;
    }

    public void markShown(long id) {
        Set<Long> shown = getShownIds();
        shown.add(id);
        persist(shown);
    }

    public void reset() {
        prefs.edit().remove(KEY_SHOWN).apply();
    }

    @Nullable
    public String getLockedIslamicDay() {
        return prefs.getString(KEY_DAY, null);
    }

    public long getLockedHadithId() {
        return prefs.getLong(KEY_ID, -1L);
    }

    public void lockForDay(@NonNull String islamicDayKey, long hadithId) {
        prefs.edit()
                .putString(KEY_DAY, islamicDayKey)
                .putLong(KEY_ID, hadithId)
                .apply();
    }

    private void persist(@NonNull Set<Long> ids) {
        Set<String> raw = new HashSet<>();
        for (Long id : ids) {
            raw.add(String.valueOf(id));
        }
        prefs.edit().putStringSet(KEY_SHOWN, raw).apply();
    }
}

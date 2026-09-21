package com.adreesulhassan.puretasbeeh.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adreesulhassan.puretasbeeh.data.entity.SectTag;

/**
 * Offline prefs — Fiqh selection and first-launch flag.
 * Values: JAFRIYA | HANFIYA | BOTH
 */
public final class FiqhPreferences {

    public static final String FIQH_JAFRIYA = "JAFRIYA";
    public static final String FIQH_HANFIYA = "HANFIYA";
    public static final String FIQH_BOTH = "BOTH";

    private static final String PREFS = "pure_tasbeeh_prefs";
    private static final String KEY_FIQH = "fiqh_preference";
    private static final String KEY_ONBOARDED = "fiqh_onboarded";

    private final SharedPreferences prefs;

    public FiqhPreferences(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isOnboarded() {
        return prefs.getBoolean(KEY_ONBOARDED, false);
    }

    public void setOnboarded(boolean value) {
        prefs.edit().putBoolean(KEY_ONBOARDED, value).apply();
    }

    @Nullable
    public String getFiqh() {
        return prefs.getString(KEY_FIQH, null);
    }

    @NonNull
    public String getFiqhOrDefault() {
        String v = getFiqh();
        return v != null ? v : FIQH_BOTH;
    }

    public void setFiqh(@NonNull String fiqh) {
        prefs.edit()
                .putString(KEY_FIQH, fiqh)
                .putBoolean(KEY_ONBOARDED, true)
                .apply();
    }

    /**
     * Maps UI Fiqh preference to Room sect_tag filter.
     * For BOTH, callers should branch UI instead of using a single tag.
     */
    @NonNull
    public String toSectTag() {
        switch (getFiqhOrDefault()) {
            case FIQH_JAFRIYA:
                return SectTag.SHIA;
            case FIQH_HANFIYA:
                return SectTag.SUNNI;
            default:
                return SectTag.BOTH;
        }
    }

    public boolean isSingleFiqh() {
        String f = getFiqhOrDefault();
        return FIQH_JAFRIYA.equals(f) || FIQH_HANFIYA.equals(f);
    }
}

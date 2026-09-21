package com.adreesulhassan.puretasbeeh.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

/**
 * App UI + content translation language.
 * Codes: en | ur | ar | fa
 */
public final class LanguagePreferences {

    public static final String LANG_EN = "en";
    public static final String LANG_UR = "ur";
    public static final String LANG_AR = "ar";
    public static final String LANG_FA = "fa";

    private static final String PREFS = "pure_tasbeeh_prefs";
    private static final String KEY_LANG = "app_language";
    private static final String KEY_LANG_SET = "app_language_set";

    private final SharedPreferences prefs;

    public LanguagePreferences(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isLanguageSet() {
        return prefs.getBoolean(KEY_LANG_SET, false);
    }

    @NonNull
    public String getLanguage() {
        String v = prefs.getString(KEY_LANG, LANG_EN);
        return v != null ? v : LANG_EN;
    }

    public void setLanguage(@NonNull String lang) {
        prefs.edit()
                .putString(KEY_LANG, lang)
                .putBoolean(KEY_LANG_SET, true)
                .apply();
        applyAppLocales(lang);
    }

    public boolean isRtl() {
        String lang = getLanguage();
        return LANG_UR.equals(lang) || LANG_AR.equals(lang) || LANG_FA.equals(lang);
    }

    /** Apply persisted locale at process start. */
    public void applySavedLocale() {
        if (isLanguageSet()) {
            applyAppLocales(getLanguage());
        }
    }

    public static void applyAppLocales(@NonNull String lang) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang));
    }

    @Nullable
    public static String displayName(@NonNull String code) {
        switch (code) {
            case LANG_EN:
                return "English";
            case LANG_UR:
                return "اردو";
            case LANG_AR:
                return "العربية";
            case LANG_FA:
                return "فارسی";
            default:
                return code;
        }
    }
}

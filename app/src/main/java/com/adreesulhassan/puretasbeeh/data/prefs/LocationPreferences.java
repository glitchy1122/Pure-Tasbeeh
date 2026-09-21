package com.adreesulhassan.puretasbeeh.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * City coordinates, per-prayer offsets, and dashboard namaz method preference.
 */
public final class LocationPreferences {

    public static final String NAMAZ_JAFARI = "JAFARI";
    public static final String NAMAZ_HANAFI = "HANAFI";

    private static final String PREFS = "pure_tasbeeh_prefs";
    private static final String KEY_CITY = "location_city";
    private static final String KEY_COUNTRY = "location_country";
    private static final String KEY_LAT = "location_lat";
    private static final String KEY_LNG = "location_lng";
    private static final String KEY_TZ = "location_tz";
    private static final String KEY_OFFSET = "prayer_offset_minutes"; // legacy global
    private static final String KEY_OFF_FAJR = "offset_fajr";
    private static final String KEY_OFF_DHUHR = "offset_dhuhr";
    private static final String KEY_OFF_ASR = "offset_asr";
    private static final String KEY_OFF_MAGHRIB = "offset_maghrib";
    private static final String KEY_OFF_ISHA = "offset_isha";
    private static final String KEY_NAMAZ_METHOD = "dashboard_namaz_method";

    public enum PrayerOffset {
        FAJR, DHUHR, ASR, MAGHRIB, ISHA
    }

    private final SharedPreferences prefs;

    public LocationPreferences(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean hasCity() {
        return prefs.contains(KEY_LAT) && prefs.contains(KEY_LNG);
    }

    @NonNull
    public String getCityName() {
        return prefs.getString(KEY_CITY, "Makkah");
    }

    @NonNull
    public String getCountry() {
        return prefs.getString(KEY_COUNTRY, "Saudi Arabia");
    }

    public double getLatitude() {
        return Double.longBitsToDouble(
                prefs.getLong(KEY_LAT, Double.doubleToRawLongBits(21.4225)));
    }

    public double getLongitude() {
        return Double.longBitsToDouble(
                prefs.getLong(KEY_LNG, Double.doubleToRawLongBits(39.8262)));
    }

    @NonNull
    public String getTimeZoneId() {
        return prefs.getString(KEY_TZ, "Asia/Riyadh");
    }

    public void setCity(@NonNull String name,
                        @NonNull String country,
                        double lat,
                        double lng,
                        @NonNull String timeZoneId) {
        prefs.edit()
                .putString(KEY_CITY, name)
                .putString(KEY_COUNTRY, country)
                .putLong(KEY_LAT, Double.doubleToRawLongBits(lat))
                .putLong(KEY_LNG, Double.doubleToRawLongBits(lng))
                .putString(KEY_TZ, timeZoneId)
                .apply();
    }

    /** Legacy global offset — kept for migration into per-prayer defaults. */
    public int getOffsetMinutes() {
        return prefs.getInt(KEY_OFFSET, 0);
    }

    public void setOffsetMinutes(int minutes) {
        int clamped = clamp(minutes);
        prefs.edit().putInt(KEY_OFFSET, clamped).apply();
    }

    public int getPrayerOffset(@NonNull PrayerOffset prayer) {
        int legacy = getOffsetMinutes();
        String key = keyFor(prayer);
        if (!prefs.contains(key)) {
            return legacy;
        }
        return prefs.getInt(key, legacy);
    }

    public void setPrayerOffset(@NonNull PrayerOffset prayer, int minutes) {
        prefs.edit().putInt(keyFor(prayer), clamp(minutes)).apply();
    }

    @NonNull
    private static String keyFor(@NonNull PrayerOffset prayer) {
        switch (prayer) {
            case FAJR:
                return KEY_OFF_FAJR;
            case DHUHR:
                return KEY_OFF_DHUHR;
            case ASR:
                return KEY_OFF_ASR;
            case MAGHRIB:
                return KEY_OFF_MAGHRIB;
            case ISHA:
                return KEY_OFF_ISHA;
            default: {
                PrayerOffset ignored = prayer;
                throw new IllegalStateException("Unhandled prayer offset: " + ignored);
            }
        }
    }

    private static int clamp(int minutes) {
        return Math.max(-30, Math.min(30, minutes));
    }

    @Nullable
    public String getNamazMethod() {
        return prefs.getString(KEY_NAMAZ_METHOD, null);
    }

    public void setNamazMethod(@NonNull String method) {
        prefs.edit().putString(KEY_NAMAZ_METHOD, method).apply();
    }

    public void clearNamazMethod() {
        prefs.edit().remove(KEY_NAMAZ_METHOD).apply();
    }
}

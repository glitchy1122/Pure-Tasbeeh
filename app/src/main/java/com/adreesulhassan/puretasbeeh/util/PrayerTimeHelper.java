package com.adreesulhassan.puretasbeeh.util;

import android.content.Context;
import android.icu.util.IslamicCalendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.data.prefs.FiqhPreferences;
import com.adreesulhassan.puretasbeeh.data.prefs.LocationPreferences;
import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Madhab;
import com.batoulapps.adhan.Prayer;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.data.DateComponents;
import com.batoulapps.adhan.data.TimeComponents;
import com.batoulapps.adhan.internal.SolarTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Offline namaz times via adhan-java, respecting Fiqh + location + offset.
 * <ul>
 *   <li>Hanafi: Karachi method + Madhab.HANAFI (shadow length ×2)</li>
 *   <li>Jafari: Leva Research Institute Qom — Fajr 16°, Maghrib 4°, Isha 14°</li>
 * </ul>
 */
public final class PrayerTimeHelper {

    public static final double DEFAULT_LAT = 21.4225;
    public static final double DEFAULT_LNG = 39.8262;

    private static final int[] HIJRI_MONTH_IDS = {
            R.string.hijri_month_1, R.string.hijri_month_2, R.string.hijri_month_3,
            R.string.hijri_month_4, R.string.hijri_month_5, R.string.hijri_month_6,
            R.string.hijri_month_7, R.string.hijri_month_8, R.string.hijri_month_9,
            R.string.hijri_month_10, R.string.hijri_month_11, R.string.hijri_month_12
    };

    public static final class Snapshot {
        public final Date fajr;
        public final Date sunrise;
        public final Date dhuhr;
        public final Date asr;
        public final Date maghrib;
        public final Date isha;
        public final Prayer current;
        public final Prayer next;
        public final String methodLabel;

        public Snapshot(Date fajr, Date sunrise, Date dhuhr, Date asr,
                        Date maghrib, Date isha, Prayer current, Prayer next,
                        String methodLabel) {
            this.fajr = fajr;
            this.sunrise = sunrise;
            this.dhuhr = dhuhr;
            this.asr = asr;
            this.maghrib = maghrib;
            this.isha = isha;
            this.current = current;
            this.next = next;
            this.methodLabel = methodLabel;
        }
    }

    private PrayerTimeHelper() {
    }

    @NonNull
    public static Snapshot computeFor(@NonNull Context context) {
        LocationPreferences loc = new LocationPreferences(context);
        FiqhPreferences fiqhPrefs = new FiqhPreferences(context);
        String method = resolveNamazMethod(fiqhPrefs, loc);
        return compute(loc.getLatitude(), loc.getLongitude(), new Date(), method, loc);
    }

    /**
     * @return {@link LocationPreferences#NAMAZ_JAFARI} or {@link LocationPreferences#NAMAZ_HANAFI},
     *         or {@code null} when BOTH is selected and the user has not chosen yet.
     */
    @Nullable
    public static String resolveNamazMethod(@NonNull FiqhPreferences fiqhPrefs,
                                            @NonNull LocationPreferences loc) {
        switch (fiqhPrefs.getFiqhOrDefault()) {
            case FiqhPreferences.FIQH_JAFRIYA:
                return LocationPreferences.NAMAZ_JAFARI;
            case FiqhPreferences.FIQH_HANFIYA:
                return LocationPreferences.NAMAZ_HANAFI;
            case FiqhPreferences.FIQH_BOTH:
                return loc.getNamazMethod();
            default:
                return loc.getNamazMethod();
        }
    }

    @NonNull
    public static Snapshot compute(double lat, double lng, Date when,
                                   @Nullable String namazMethod,
                                   @NonNull LocationPreferences offsets) {
        String method = namazMethod != null ? namazMethod : LocationPreferences.NAMAZ_HANAFI;
        Coordinates coordinates = new Coordinates(lat, lng);
        DateComponents date = DateComponents.from(when);
        CalculationParameters params = buildParameters(method);
        PrayerTimes times = new PrayerTimes(coordinates, date, params);

        Date maghrib = times.maghrib;
        if (LocationPreferences.NAMAZ_JAFARI.equals(method)) {
            Date angled = maghribAtAngle(coordinates, date, 4.0);
            if (angled != null) {
                maghrib = angled;
            }
        }

        Date fajr = applyOffset(times.fajr, offsets.getPrayerOffset(LocationPreferences.PrayerOffset.FAJR));
        Date sunrise = applyOffset(times.sunrise, offsets.getOffsetMinutes());
        Date dhuhr = applyOffset(times.dhuhr, offsets.getPrayerOffset(LocationPreferences.PrayerOffset.DHUHR));
        Date asr = applyOffset(times.asr, offsets.getPrayerOffset(LocationPreferences.PrayerOffset.ASR));
        maghrib = applyOffset(maghrib, offsets.getPrayerOffset(LocationPreferences.PrayerOffset.MAGHRIB));
        Date isha = applyOffset(times.isha, offsets.getPrayerOffset(LocationPreferences.PrayerOffset.ISHA));

        Prayer current = currentPrayer(when, fajr, sunrise, dhuhr, asr, maghrib, isha);
        Prayer next = nextPrayer(when, fajr, sunrise, dhuhr, asr, maghrib, isha);

        String label = LocationPreferences.NAMAZ_JAFARI.equals(method)
                ? "Jafari"
                : "Hanafi";

        return new Snapshot(fajr, sunrise, dhuhr, asr, maghrib, isha, current, next, label);
    }

    @NonNull
    private static CalculationParameters buildParameters(@NonNull String method) {
        if (LocationPreferences.NAMAZ_JAFARI.equals(method)) {
            // Leva Research Institute, Qom — Fajr 16°, Isha 14° (Maghrib via 4° angle separately)
            CalculationParameters params = new CalculationParameters(16.0, 14.0, CalculationMethod.OTHER);
            params.madhab = Madhab.SHAFI;
            return params;
        }
        // Sunni Hanafi: Karachi + Hanafi Asr (shadow length twice the object)
        CalculationParameters params = CalculationMethod.KARACHI.getParameters();
        params.madhab = Madhab.HANAFI;
        return params;
    }

    @Nullable
    private static Date maghribAtAngle(@NonNull Coordinates coordinates,
                                       @NonNull DateComponents date,
                                       double degreesBelowHorizon) {
        try {
            SolarTime solarTime = new SolarTime(date, coordinates);
            TimeComponents components = TimeComponents.fromDouble(
                    solarTime.hourAngle(-degreesBelowHorizon, true));
            if (components == null) {
                return null;
            }
            return components.dateComponents(date);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static Date applyOffset(@Nullable Date date, int offsetMinutes) {
        if (date == null || offsetMinutes == 0) {
            return date;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, offsetMinutes);
        return cal.getTime();
    }

    @NonNull
    private static Prayer currentPrayer(Date when, Date fajr, Date sunrise, Date dhuhr,
                                        Date asr, Date maghrib, Date isha) {
        long t = when.getTime();
        if (isha != null && isha.getTime() - t <= 0) {
            return Prayer.ISHA;
        } else if (maghrib != null && maghrib.getTime() - t <= 0) {
            return Prayer.MAGHRIB;
        } else if (asr != null && asr.getTime() - t <= 0) {
            return Prayer.ASR;
        } else if (dhuhr != null && dhuhr.getTime() - t <= 0) {
            return Prayer.DHUHR;
        } else if (sunrise != null && sunrise.getTime() - t <= 0) {
            return Prayer.SUNRISE;
        } else if (fajr != null && fajr.getTime() - t <= 0) {
            return Prayer.FAJR;
        }
        return Prayer.NONE;
    }

    @NonNull
    private static Prayer nextPrayer(Date when, Date fajr, Date sunrise, Date dhuhr,
                                     Date asr, Date maghrib, Date isha) {
        long t = when.getTime();
        if (isha != null && isha.getTime() - t <= 0) {
            return Prayer.NONE;
        } else if (maghrib != null && maghrib.getTime() - t <= 0) {
            return Prayer.ISHA;
        } else if (asr != null && asr.getTime() - t <= 0) {
            return Prayer.MAGHRIB;
        } else if (dhuhr != null && dhuhr.getTime() - t <= 0) {
            return Prayer.ASR;
        } else if (sunrise != null && sunrise.getTime() - t <= 0) {
            return Prayer.DHUHR;
        } else if (fajr != null && fajr.getTime() - t <= 0) {
            return Prayer.SUNRISE;
        }
        return Prayer.FAJR;
    }

    public static String formatTime(Date date) {
        if (date == null) {
            return "--:--";
        }
        SimpleDateFormat fmt = new SimpleDateFormat("h:mm a", Locale.getDefault());
        fmt.setTimeZone(TimeZone.getDefault());
        return fmt.format(date);
    }

    public static String formatIslamicGregorian(@NonNull Context context) {
        Date now = new Date();
        String gregorian = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(now);

        IslamicCalendar islamic = new IslamicCalendar();
        islamic.setTime(now);
        int day = islamic.get(IslamicCalendar.DAY_OF_MONTH);
        int month = islamic.get(IslamicCalendar.MONTH);
        int year = islamic.get(IslamicCalendar.YEAR);
        int monthRes = (month >= 0 && month < HIJRI_MONTH_IDS.length)
                ? HIJRI_MONTH_IDS[month]
                : R.string.hijri_month_1;
        String monthName = context.getString(monthRes);

        return context.getString(R.string.date_hijri_gregorian, day, monthName, year, gregorian);
    }

    /** Stable Islamic calendar day key, e.g. {@code 1448-01-14}. */
    @NonNull
    public static String islamicDayKey() {
        IslamicCalendar islamic = new IslamicCalendar();
        islamic.setTime(new Date());
        int day = islamic.get(IslamicCalendar.DAY_OF_MONTH);
        int month = islamic.get(IslamicCalendar.MONTH) + 1;
        int year = islamic.get(IslamicCalendar.YEAR);
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
    }

    public static boolean isCurrent(Prayer prayer, Prayer current) {
        return prayer == current;
    }

    /**
     * Rough day-progress of the sun between sunrise and sunset (0..1), or night progress for moon.
     */
    public static float sunProgress(@NonNull Snapshot snap, @NonNull Date now) {
        if (snap.sunrise == null || snap.maghrib == null) {
            return 0.5f;
        }
        long start = snap.sunrise.getTime();
        long end = snap.maghrib.getTime();
        if (end <= start) {
            return 0.5f;
        }
        long t = now.getTime();
        if (t <= start) {
            return 0f;
        }
        if (t >= end) {
            return 1f;
        }
        return (float) (t - start) / (float) (end - start);
    }

    /** Approximate moon presence opposite the solar day (simple UI estimate). */
    public static float moonProgress(@NonNull Snapshot snap, @NonNull Date now) {
        float sun = sunProgress(snap, now);
        if (snap.sunrise == null || snap.maghrib == null) {
            return 0.5f;
        }
        long sunrise = snap.sunrise.getTime();
        long sunset = snap.maghrib.getTime();
        long t = now.getTime();
        boolean isNight = t < sunrise || t > sunset;
        if (!isNight) {
            // Daytime: moon low / near horizon estimate on opposite side
            return Math.max(0f, Math.min(1f, 1f - sun));
        }
        // Night: progress from sunset→midnight→sunrise as 0→1
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        if (t >= sunset) {
            Calendar nextSunrise = Calendar.getInstance();
            nextSunrise.setTime(snap.sunrise);
            nextSunrise.add(Calendar.DAY_OF_YEAR, 1);
            long nightEnd = nextSunrise.getTimeInMillis();
            long span = nightEnd - sunset;
            if (span <= 0) {
                return 0.5f;
            }
            return Math.max(0f, Math.min(1f, (float) (t - sunset) / (float) span));
        }
        // Before sunrise (early morning)
        Calendar prevSunset = Calendar.getInstance();
        prevSunset.setTime(snap.maghrib);
        prevSunset.add(Calendar.DAY_OF_YEAR, -1);
        long nightStart = prevSunset.getTimeInMillis();
        long span = sunrise - nightStart;
        if (span <= 0) {
            return 0.5f;
        }
        return Math.max(0f, Math.min(1f, (float) (t - nightStart) / (float) span));
    }
}

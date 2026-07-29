package com.adreesulhassan.puretasbeeh.util;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Prayer;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.data.DateComponents;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Offline namaz times via adhan-java.
 * Default coordinates: Makkah (frictionless; no location permission required).
 */
public final class PrayerTimeHelper {

    // Makkah
    public static final double DEFAULT_LAT = 21.4225;
    public static final double DEFAULT_LNG = 39.8262;

    public static final class Snapshot {
        public final Date fajr;
        public final Date sunrise;
        public final Date dhuhr;
        public final Date asr;
        public final Date maghrib;
        public final Date isha;
        public final Prayer current;
        public final Prayer next;

        public Snapshot(Date fajr, Date sunrise, Date dhuhr, Date asr,
                        Date maghrib, Date isha, Prayer current, Prayer next) {
            this.fajr = fajr;
            this.sunrise = sunrise;
            this.dhuhr = dhuhr;
            this.asr = asr;
            this.maghrib = maghrib;
            this.isha = isha;
            this.current = current;
            this.next = next;
        }
    }

    private PrayerTimeHelper() {
    }

    public static Snapshot computeNow() {
        return compute(DEFAULT_LAT, DEFAULT_LNG, new Date());
    }

    public static Snapshot compute(double lat, double lng, Date when) {
        Coordinates coordinates = new Coordinates(lat, lng);
        DateComponents date = DateComponents.from(when);
        CalculationParameters params = CalculationMethod.MUSLIM_WORLD_LEAGUE.getParameters();
        PrayerTimes times = new PrayerTimes(coordinates, date, params);

        Prayer current = times.currentPrayer();
        Prayer next = times.nextPrayer();

        return new Snapshot(
                times.fajr,
                times.sunrise,
                times.dhuhr,
                times.asr,
                times.maghrib,
                times.isha,
                current,
                next
        );
    }

    public static String formatTime(Date date) {
        if (date == null) {
            return "--:--";
        }
        SimpleDateFormat fmt = new SimpleDateFormat("h:mm a", Locale.getDefault());
        fmt.setTimeZone(TimeZone.getDefault());
        return fmt.format(date);
    }

    public static String formatIslamicGregorian() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat gregorian = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault());
        // Simple Hijri approx display via android ICU if available — fallback label
        String g = gregorian.format(cal.getTime());
        try {
            android.icu.util.IslamicCalendar islamic = new android.icu.util.IslamicCalendar();
            islamic.setTime(cal.getTime());
            int day = islamic.get(android.icu.util.Calendar.DAY_OF_MONTH);
            int month = islamic.get(android.icu.util.Calendar.MONTH) + 1;
            int year = islamic.get(android.icu.util.Calendar.YEAR);
            String[] months = {
                    "Muharram", "Safar", "Rabiʿ I", "Rabiʿ II", "Jumada I", "Jumada II",
                    "Rajab", "Shaʿban", "Ramadan", "Shawwal", "Dhul Qaʿdah", "Dhul Hijjah"
            };
            String mName = months[Math.max(0, Math.min(months.length - 1, month - 1))];
            return day + " " + mName + " " + year + " AH  ·  " + g;
        } catch (Throwable t) {
            return g;
        }
    }

    public static boolean isCurrent(Prayer prayer, Prayer current) {
        return prayer != null && prayer == current;
    }
}

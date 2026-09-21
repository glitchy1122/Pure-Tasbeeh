package com.adreesulhassan.puretasbeeh.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.data.db.AppDatabase;
import com.adreesulhassan.puretasbeeh.data.entity.HadithEntity;
import com.adreesulhassan.puretasbeeh.data.entity.SectTag;
import com.adreesulhassan.puretasbeeh.data.prefs.FiqhPreferences;
import com.adreesulhassan.puretasbeeh.data.prefs.LocationPreferences;
import com.adreesulhassan.puretasbeeh.data.prefs.HadithDayPreferences;
import com.adreesulhassan.puretasbeeh.data.prefs.LanguagePreferences;
import com.adreesulhassan.puretasbeeh.ui.ahadith.AhadithActivity;
import com.adreesulhassan.puretasbeeh.ui.books.BooksActivity;
import com.adreesulhassan.puretasbeeh.ui.duas.DuasLibraryActivity;
import com.adreesulhassan.puretasbeeh.ui.faal.FaalActivity;
import com.adreesulhassan.puretasbeeh.ui.quran.QuranActivity;
import com.adreesulhassan.puretasbeeh.ui.settings.LocationSettingsActivity;
import com.adreesulhassan.puretasbeeh.ui.sky.AnimatedSkyView;
import com.adreesulhassan.puretasbeeh.ui.tasbeeh.CustomTasbeehActivity;
import com.adreesulhassan.puretasbeeh.ui.tasbeeh.TasbeehZehraActivity;
import com.adreesulhassan.puretasbeeh.util.HapticHelper;
import com.adreesulhassan.puretasbeeh.util.PermissionHelper;
import com.adreesulhassan.puretasbeeh.util.PrayerTimeHelper;
import com.batoulapps.adhan.Prayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private FiqhPreferences prefs;
    private LocationPreferences locationPrefs;
    private LanguagePreferences languagePrefs;
    private HadithDayPreferences hadithDayPrefs;
    private boolean namazDialogShowing;
    private long lastHadithId = -1L;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        prefs = new FiqhPreferences(this);
        locationPrefs = new LocationPreferences(this);
        languagePrefs = new LanguagePreferences(this);
        hadithDayPrefs = new HadithDayPreferences(this);

        TextView tvDate = findViewById(R.id.tvDate);
        tvDate.setText(PrayerTimeHelper.formatIslamicGregorian(this));

        maybePromptNamazMethod();
        bindPrayerTimes();
        bindSkyPanel();
        loadHadithOfDay();
        bindMenu();
        maybePromptRuntimePermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.handleRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void maybePromptRuntimePermissions() {
        if (PermissionHelper.wasPermissionOnboardingShown(this)) {
            return;
        }
        // Defer slightly so Fiqh/Namaz dialogs are not stacked awkwardly.
        getWindow().getDecorView().postDelayed(() -> {
            if (isFinishing() || PermissionHelper.wasPermissionOnboardingShown(this)) {
                return;
            }
            if (namazDialogShowing) {
                getWindow().getDecorView().postDelayed(this::maybePromptRuntimePermissions, 600);
                return;
            }
            PermissionHelper.showPermissionOnboarding(this, null);
        }, 400);
    }

    @Override
    protected void onResume() {
        super.onResume();
        maybePromptNamazMethod();
        loadHadithOfDay();
        bindPrayerTimes();
        bindSkyPanel();
    }

    private void maybePromptNamazMethod() {
        if (!FiqhPreferences.FIQH_BOTH.equals(prefs.getFiqhOrDefault())) {
            return;
        }
        if (locationPrefs.getNamazMethod() != null) {
            return;
        }
        if (namazDialogShowing) {
            return;
        }
        namazDialogShowing = true;
        new AlertDialog.Builder(this)
                .setMessage(R.string.namaz_method_title)
                .setCancelable(false)
                .setPositiveButton(R.string.namaz_method_shia, (d, w) -> {
                    locationPrefs.setNamazMethod(LocationPreferences.NAMAZ_JAFARI);
                    namazDialogShowing = false;
                    bindPrayerTimes();
                    bindSkyPanel();
                })
                .setNegativeButton(R.string.namaz_method_sunni, (d, w) -> {
                    locationPrefs.setNamazMethod(LocationPreferences.NAMAZ_HANAFI);
                    namazDialogShowing = false;
                    bindPrayerTimes();
                    bindSkyPanel();
                })
                .setOnDismissListener(d -> namazDialogShowing = false)
                .show();
    }

    private void bindPrayerTimes() {
        String method = PrayerTimeHelper.resolveNamazMethod(prefs, locationPrefs);
        if (method == null) {
            return;
        }
        PrayerTimeHelper.Snapshot snap = PrayerTimeHelper.computeFor(this);
        bindChip(R.id.prayerFajr, getString(R.string.prayer_fajr), snap.fajr, Prayer.FAJR, snap.current);
        bindChip(R.id.prayerDhuhr, getString(R.string.prayer_dhuhr), snap.dhuhr, Prayer.DHUHR, snap.current);
        bindChip(R.id.prayerAsr, getString(R.string.prayer_asr), snap.asr, Prayer.ASR, snap.current);
        bindChip(R.id.prayerMaghrib, getString(R.string.prayer_maghrib), snap.maghrib, Prayer.MAGHRIB, snap.current);
        bindChip(R.id.prayerIsha, getString(R.string.prayer_isha), snap.isha, Prayer.ISHA, snap.current);
    }

    private void bindSkyPanel() {
        String method = PrayerTimeHelper.resolveNamazMethod(prefs, locationPrefs);
        TextView tvSkyLabel = findViewById(R.id.tvSkyLabel);
        TextView tvSun = findViewById(R.id.tvSunEstimate);
        TextView tvMoon = findViewById(R.id.tvMoonEstimate);
        AnimatedSkyView sky = findViewById(R.id.animatedSky);

        tvSkyLabel.setText(getString(R.string.sky_panel_label, locationPrefs.getCityName()));

        if (method == null) {
            tvSun.setText(getString(R.string.sun_position) + ": —");
            tvMoon.setText(getString(R.string.moon_position) + ": —");
            sky.setSkyState(0.35f, 0.7f, true);
            return;
        }

        PrayerTimeHelper.Snapshot snap = PrayerTimeHelper.computeFor(this);
        Date now = new Date();
        float sunP = PrayerTimeHelper.sunProgress(snap, now);
        float moonP = PrayerTimeHelper.moonProgress(snap, now);

        boolean daytime = snap.sunrise != null && snap.maghrib != null
                && now.after(snap.sunrise) && now.before(snap.maghrib);
        sky.setSkyState(sunP, moonP, daytime);

        tvSun.setText(String.format(Locale.getDefault(),
                "%s · %s", getString(R.string.sun_position),
                daytime ? percentLabel(sunP) : getString(R.string.sky_below_horizon)));
        tvMoon.setText(String.format(Locale.getDefault(),
                "%s · %s", getString(R.string.moon_position),
                daytime ? getString(R.string.sky_day_estimate) : percentLabel(moonP)));
    }

    private String percentLabel(float progress) {
        int pct = Math.round(progress * 100f);
        if (pct <= 15) {
            return getString(R.string.sky_rising);
        }
        if (pct >= 85) {
            return getString(R.string.sky_setting);
        }
        return getString(R.string.sky_overhead_pct, pct);
    }

    private void bindChip(int includeId, String name, Date time, Prayer prayer, Prayer current) {
        View chip = findViewById(includeId);
        TextView tvName = chip.findViewById(R.id.tvPrayerName);
        TextView tvTime = chip.findViewById(R.id.tvPrayerTime);
        tvName.setText(name);
        tvTime.setText(PrayerTimeHelper.formatTime(time));
        if (PrayerTimeHelper.isCurrent(prayer, current)) {
            chip.setBackgroundResource(R.drawable.bg_clay_prayer_active);
        } else {
            chip.setBackground(null);
        }
    }

    private void loadHadithOfDay() {
        AppDatabase.io().execute(() -> {
            String dayKey = PrayerTimeHelper.islamicDayKey();
            String lockedDay = hadithDayPrefs.getLockedIslamicDay();
            long lockedId = hadithDayPrefs.getLockedHadithId();

            // Same Islamic day → keep the already chosen hadith.
            if (dayKey.equals(lockedDay) && lockedId > 0L) {
                HadithEntity locked = AppDatabase.getInstance(this).hadithDao().getById(lockedId);
                if (locked != null) {
                    runOnUiThread(() -> bindHadithCard(locked));
                    return;
                }
            }

            boolean both = SectTag.BOTH.equals(prefs.toSectTag());
            String sect = both ? SectTag.BOTH : prefs.toSectTag();
            Set<Long> shown = hadithDayPrefs.getShownIds();
            HadithEntity hadith;
            if (shown.isEmpty()) {
                hadith = both
                        ? AppDatabase.getInstance(this).hadithDao().getRandomAny()
                        : AppDatabase.getInstance(this).hadithDao().getRandomForSect(sect);
            } else {
                ArrayList<Long> exclude = new ArrayList<>(shown);
                if (both) {
                    hadith = AppDatabase.getInstance(this).hadithDao().getRandomExcluding(exclude);
                    if (hadith == null) {
                        hadithDayPrefs.reset();
                        hadith = AppDatabase.getInstance(this).hadithDao().getRandomAny();
                    }
                } else {
                    hadith = AppDatabase.getInstance(this).hadithDao()
                            .getRandomForSectExcluding(sect, exclude);
                    if (hadith == null) {
                        hadithDayPrefs.reset();
                        hadith = AppDatabase.getInstance(this).hadithDao().getRandomForSect(sect);
                    }
                }
            }

            HadithEntity shownEntity = hadith;
            if (shownEntity != null) {
                hadithDayPrefs.markShown(shownEntity.id);
                hadithDayPrefs.lockForDay(dayKey, shownEntity.id);
            }
            runOnUiThread(() -> bindHadithCard(shownEntity));
        });
    }

    private void bindHadithCard(@Nullable HadithEntity shownEntity) {
        TextView translation = findViewById(R.id.tvHadithTranslation);
        TextView source = findViewById(R.id.tvHadithSource);
        if (shownEntity == null) {
            translation.setText(getString(R.string.no_content));
            source.setText("");
            return;
        }
        lastHadithId = shownEntity.id;
        translation.setText(shownEntity.translationFor(languagePrefs.getLanguage()));
        source.setText(shownEntity.source);
    }

    private void bindMenu() {
        // Hadith of the Day is fixed for the Islamic date — no tap-to-change.
        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            startActivity(new Intent(this, LocationSettingsActivity.class));
        });
        findViewById(R.id.tileQuran).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            startActivity(new Intent(this, QuranActivity.class));
        });
        findViewById(R.id.tileDuas).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            startActivity(new Intent(this, DuasLibraryActivity.class));
        });
        findViewById(R.id.tileZehra).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            startActivity(new Intent(this, TasbeehZehraActivity.class));
        });
        findViewById(R.id.tileCustom).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            startActivity(new Intent(this, CustomTasbeehActivity.class));
        });
        findViewById(R.id.tileAhadith).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            startActivity(new Intent(this, AhadithActivity.class));
        });
        findViewById(R.id.tileBooks).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            startActivity(new Intent(this, BooksActivity.class));
        });
        findViewById(R.id.tileFaal).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            startActivity(new Intent(this, FaalActivity.class));
        });
    }
}

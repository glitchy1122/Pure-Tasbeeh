package com.adreesulhassan.puretasbeeh.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.data.db.AppDatabase;
import com.adreesulhassan.puretasbeeh.data.entity.HadithEntity;
import com.adreesulhassan.puretasbeeh.data.entity.SectTag;
import com.adreesulhassan.puretasbeeh.data.prefs.FiqhPreferences;
import com.adreesulhassan.puretasbeeh.ui.books.BooksActivity;
import com.adreesulhassan.puretasbeeh.ui.duas.DuasLibraryActivity;
import com.adreesulhassan.puretasbeeh.ui.settings.FiqhSettingsActivity;
import com.adreesulhassan.puretasbeeh.ui.tasbeeh.CustomTasbeehActivity;
import com.adreesulhassan.puretasbeeh.ui.tasbeeh.TasbeehZehraActivity;
import com.adreesulhassan.puretasbeeh.util.HapticHelper;
import com.adreesulhassan.puretasbeeh.util.PrayerTimeHelper;
import com.batoulapps.adhan.Prayer;

public class HomeActivity extends AppCompatActivity {

    private FiqhPreferences prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        prefs = new FiqhPreferences(this);

        TextView tvDate = findViewById(R.id.tvDate);
        tvDate.setText(PrayerTimeHelper.formatIslamicGregorian());

        bindPrayerTimes();
        loadHadithOfDay();
        bindMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHadithOfDay();
        bindPrayerTimes();
    }

    private void bindPrayerTimes() {
        PrayerTimeHelper.Snapshot snap = PrayerTimeHelper.computeNow();
        bindChip(R.id.prayerFajr, getString(R.string.prayer_fajr), snap.fajr, Prayer.FAJR, snap.current);
        bindChip(R.id.prayerDhuhr, getString(R.string.prayer_dhuhr), snap.dhuhr, Prayer.DHUHR, snap.current);
        bindChip(R.id.prayerAsr, getString(R.string.prayer_asr), snap.asr, Prayer.ASR, snap.current);
        bindChip(R.id.prayerMaghrib, getString(R.string.prayer_maghrib), snap.maghrib, Prayer.MAGHRIB, snap.current);
        bindChip(R.id.prayerIsha, getString(R.string.prayer_isha), snap.isha, Prayer.ISHA, snap.current);
    }

    private void bindChip(int includeId, String name, java.util.Date time, Prayer prayer, Prayer current) {
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
            String sect = prefs.toSectTag();
            if (FiqhPreferences.FIQH_BOTH.equals(prefs.getFiqhOrDefault())
                    || SectTag.BOTH.equals(sect)) {
                // Universal: random from any tagged BOTH preferentially via BOTH query using SHIA|SUNNI random
                // Use BOTH as filter so rows with BOTH + we also allow either by picking SUNNI/SHIA randomly
                sect = Math.random() < 0.5 ? SectTag.SHIA : SectTag.SUNNI;
            }
            HadithEntity hadith = AppDatabase.getInstance(this).hadithDao().getRandomForSect(sect);
            runOnUiThread(() -> {
                TextView arabic = findViewById(R.id.tvHadithArabic);
                TextView translation = findViewById(R.id.tvHadithTranslation);
                TextView source = findViewById(R.id.tvHadithSource);
                if (hadith == null) {
                    arabic.setText("…");
                    translation.setText(getString(R.string.no_content));
                    source.setText("");
                    return;
                }
                arabic.setText(hadith.arabicText);
                translation.setText(hadith.translation);
                source.setText(hadith.source);
            });
        });
    }

    private void bindMenu() {
        findViewById(R.id.tileQuran).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            Toast.makeText(this, R.string.quran_coming_soon, Toast.LENGTH_SHORT).show();
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
        findViewById(R.id.tileBooks).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            startActivity(new Intent(this, BooksActivity.class));
        });
        findViewById(R.id.btnFiqhSettings).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            startActivity(new Intent(this, FiqhSettingsActivity.class));
        });
    }
}

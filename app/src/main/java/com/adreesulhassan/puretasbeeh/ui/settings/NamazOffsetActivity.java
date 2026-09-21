package com.adreesulhassan.puretasbeeh.ui.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.data.prefs.LocationPreferences;
import com.adreesulhassan.puretasbeeh.util.HapticHelper;

import java.util.Locale;

public class NamazOffsetActivity extends AppCompatActivity {

    private LocationPreferences prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_namaz_offset);
        prefs = new LocationPreferences(this);

        bindRow(R.id.rowFajr, R.string.prayer_fajr, LocationPreferences.PrayerOffset.FAJR);
        bindRow(R.id.rowDhuhr, R.string.prayer_dhuhr, LocationPreferences.PrayerOffset.DHUHR);
        bindRow(R.id.rowAsr, R.string.prayer_asr, LocationPreferences.PrayerOffset.ASR);
        bindRow(R.id.rowMaghrib, R.string.prayer_maghrib, LocationPreferences.PrayerOffset.MAGHRIB);
        bindRow(R.id.rowIsha, R.string.prayer_isha, LocationPreferences.PrayerOffset.ISHA);

        findViewById(R.id.btnResetOffsets).setOnClickListener(v -> {
            HapticHelper.milestone(v);
            for (LocationPreferences.PrayerOffset p : LocationPreferences.PrayerOffset.values()) {
                prefs.setPrayerOffset(p, 0);
            }
            prefs.setOffsetMinutes(0);
            recreate();
        });
    }

    private void bindRow(int includeId, int labelRes, LocationPreferences.PrayerOffset prayer) {
        View row = findViewById(includeId);
        TextView label = row.findViewById(R.id.tvPrayerLabel);
        TextView value = row.findViewById(R.id.tvOffset);
        label.setText(labelRes);
        refresh(value, prayer);

        row.findViewById(R.id.btnMinus).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            prefs.setPrayerOffset(prayer, prefs.getPrayerOffset(prayer) - 1);
            refresh(value, prayer);
        });
        row.findViewById(R.id.btnPlus).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            prefs.setPrayerOffset(prayer, prefs.getPrayerOffset(prayer) + 1);
            refresh(value, prayer);
        });
    }

    private void refresh(TextView value, LocationPreferences.PrayerOffset prayer) {
        int o = prefs.getPrayerOffset(prayer);
        String sign = o > 0 ? "+" : "";
        value.setText(String.format(Locale.getDefault(), "%s%d", sign, o));
    }
}

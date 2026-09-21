package com.adreesulhassan.puretasbeeh.ui.faal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.data.db.DatabaseHelper;
import com.adreesulhassan.puretasbeeh.data.prefs.LanguagePreferences;
import com.adreesulhassan.puretasbeeh.util.HapticHelper;

import java.util.List;
import java.util.Locale;

/**
 * Fa'al — three random Quran verses (may repeat across draws).
 */
public class FaalActivity extends AppCompatActivity {

    private LinearLayout container;
    private String lang = LanguagePreferences.LANG_EN;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faal);
        lang = new LanguagePreferences(this).getLanguage();
        container = findViewById(R.id.faalContainer);
        findViewById(R.id.btnRefreshFaal).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            drawFaal();
        });
        drawFaal();
    }

    private void drawFaal() {
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        db.ensureCopiedAsync((ok, err) -> new Thread(() -> {
            List<DatabaseHelper.Ayah> ayahs = db.getRandomAyahs(3);
            runOnUiThread(() -> bindAyahs(ayahs));
        }).start());
    }

    private void bindAyahs(List<DatabaseHelper.Ayah> ayahs) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        if (ayahs == null || ayahs.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.quran_empty);
            empty.setTextAppearance(this, R.style.Clay_Body);
            container.addView(empty);
            return;
        }
        for (DatabaseHelper.Ayah a : ayahs) {
            View row = inflater.inflate(R.layout.item_ayah, container, false);
            TextView number = row.findViewById(R.id.tvAyahNumber);
            TextView text = row.findViewById(R.id.tvAyahText);
            TextView translation = row.findViewById(R.id.tvAyahTranslation);
            number.setText(String.format(Locale.getDefault(), "%d:%d", a.surahNumber, a.ayahNumber));
            text.setText(a.textAr);
            String tr = a.translationFor(lang);
            if (tr.isEmpty() || LanguagePreferences.LANG_AR.equals(lang)) {
                translation.setVisibility(View.GONE);
            } else {
                translation.setVisibility(View.VISIBLE);
                translation.setText(tr);
            }
            container.addView(row);
        }
    }
}

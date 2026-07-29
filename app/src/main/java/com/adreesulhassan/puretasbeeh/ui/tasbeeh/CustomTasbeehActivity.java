package com.adreesulhassan.puretasbeeh.ui.tasbeeh;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.data.db.AppDatabase;
import com.adreesulhassan.puretasbeeh.service.GeminiZikrService;
import com.adreesulhassan.puretasbeeh.util.HapticHelper;

/**
 * Custom tasbeeh with massive clay tap button + Gemini feeling → zikr.
 */
public class CustomTasbeehActivity extends AppCompatActivity {

    private final GeminiZikrService gemini = new GeminiZikrService();

    private EditText etFeeling;
    private TextView tvZikrArabic;
    private TextView tvZikrTranslit;
    private TextView tvCount;
    private ProgressBar progress;
    private FrameLayout btnTap;

    private int count;
    private int milestoneEvery = 33;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_tasbeeh);

        etFeeling = findViewById(R.id.etFeeling);
        tvZikrArabic = findViewById(R.id.tvZikrArabic);
        tvZikrTranslit = findViewById(R.id.tvZikrTranslit);
        tvCount = findViewById(R.id.tvCount);
        progress = findViewById(R.id.progress);
        btnTap = findViewById(R.id.btnTap);

        findViewById(R.id.btnGetZikr).setOnClickListener(v -> requestZikr());
        findViewById(R.id.btnReset).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            resetCounter();
        });
        btnTap.setOnClickListener(v -> onTap());
        resetCounter();
    }

    private void onTap() {
        HapticHelper.tap(btnTap);
        count++;
        tvCount.setText(String.valueOf(count));
        if (milestoneEvery > 0 && count % milestoneEvery == 0) {
            HapticHelper.milestone(btnTap);
        }
    }

    private void resetCounter() {
        count = 0;
        tvCount.setText("0");
    }

    private void requestZikr() {
        String feeling = etFeeling.getText() != null
                ? etFeeling.getText().toString().trim()
                : "";
        if (TextUtils.isEmpty(feeling)) {
            Toast.makeText(this, R.string.feeling_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!gemini.hasApiKey()) {
            Toast.makeText(this, R.string.gemini_key_missing, Toast.LENGTH_LONG).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);
        findViewById(R.id.btnGetZikr).setEnabled(false);
        Toast.makeText(this, R.string.gemini_loading, Toast.LENGTH_SHORT).show();

        AppDatabase.io().execute(() -> {
            try {
                GeminiZikrService.ZikrSuggestion suggestion = gemini.suggestZikr(feeling);
                runOnUiThread(() -> {
                    tvZikrArabic.setText(suggestion.arabic);
                    tvZikrTranslit.setText(suggestion.transliteration);
                    milestoneEvery = suggestion.recommendedCount;
                    resetCounter();
                    HapticHelper.milestone(btnTap);
                    progress.setVisibility(View.GONE);
                    findViewById(R.id.btnGetZikr).setEnabled(true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    findViewById(R.id.btnGetZikr).setEnabled(true);
                    Toast.makeText(this, R.string.gemini_error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}

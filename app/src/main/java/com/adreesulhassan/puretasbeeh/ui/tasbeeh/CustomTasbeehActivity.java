package com.adreesulhassan.puretasbeeh.ui.tasbeeh;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.data.session.TasbeehSessionStore;
import com.adreesulhassan.puretasbeeh.service.FeelingZikrMatcher;
import com.adreesulhassan.puretasbeeh.util.HapticHelper;

/**
 * Custom tasbeeh: offline feeling → zikr / ayah / short dua, plus tap counter.
 */
public class CustomTasbeehActivity extends AppCompatActivity {

    private final FeelingZikrMatcher matcher = new FeelingZikrMatcher();

    private EditText etFeeling;
    private EditText etTarget;
    private TextView tvZikrArabic;
    private TextView tvZikrTranslit;
    private TextView tvCount;
    private TextView btnGetZikr;
    private ProgressBar progress;
    private FrameLayout btnTap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_tasbeeh);

        etFeeling = findViewById(R.id.etFeeling);
        etTarget = findViewById(R.id.etTarget);
        tvZikrArabic = findViewById(R.id.tvZikrArabic);
        tvZikrTranslit = findViewById(R.id.tvZikrTranslit);
        tvCount = findViewById(R.id.tvCount);
        progress = findViewById(R.id.progress);
        btnTap = findViewById(R.id.btnTap);
        btnGetZikr = findViewById(R.id.btnGetZikr);

        progress.setVisibility(View.GONE);
        restoreFromSession();

        etTarget.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyTargetFromField();
            }
        });

        btnGetZikr.setOnClickListener(v -> suggestForFeeling());
        findViewById(R.id.btnReset).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            TasbeehSessionStore.resetCustomCount();
            tvCount.setText("0");
        });
        btnTap.setOnClickListener(v -> onTap());
    }

    private void restoreFromSession() {
        if (TasbeehSessionStore.customTarget <= 0) {
            TasbeehSessionStore.customTarget = 33;
        }
        etTarget.setText(String.valueOf(TasbeehSessionStore.customTarget));
        tvCount.setText(String.valueOf(TasbeehSessionStore.customCount));
        if (!TextUtils.isEmpty(TasbeehSessionStore.customZikrArabic)) {
            tvZikrArabic.setText(TasbeehSessionStore.customZikrArabic);
        }
        if (!TextUtils.isEmpty(TasbeehSessionStore.customZikrTranslit)) {
            tvZikrTranslit.setText(TasbeehSessionStore.customZikrTranslit);
        }
    }

    private void applyTargetFromField() {
        String raw = etTarget.getText() != null ? etTarget.getText().toString().trim() : "";
        if (TextUtils.isEmpty(raw)) {
            return;
        }
        try {
            int value = Integer.parseInt(raw);
            if (value > 0 && value != TasbeehSessionStore.customTarget) {
                TasbeehSessionStore.customTarget = value;
                TasbeehSessionStore.customTargetReachedNotified =
                        TasbeehSessionStore.customCount >= value;
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void onTap() {
        HapticHelper.tap(btnTap);
        TasbeehSessionStore.customCount++;
        tvCount.setText(String.valueOf(TasbeehSessionStore.customCount));

        int target = TasbeehSessionStore.customTarget;
        if (target > 0
                && TasbeehSessionStore.customCount == target
                && !TasbeehSessionStore.customTargetReachedNotified) {
            TasbeehSessionStore.customTargetReachedNotified = true;
            HapticHelper.milestone(btnTap);
            Toast.makeText(this, getString(R.string.target_reached, target), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void suggestForFeeling() {
        String feeling = etFeeling.getText() != null
                ? etFeeling.getText().toString().trim()
                : "";
        if (TextUtils.isEmpty(feeling)) {
            Toast.makeText(this, R.string.feeling_hint, Toast.LENGTH_SHORT).show();
            return;
        }

        FeelingZikrMatcher.Suggestion suggestion = matcher.suggest(feeling);
        tvZikrArabic.setText(suggestion.arabic);
        tvZikrTranslit.setText(suggestion.meaning);
        TasbeehSessionStore.customZikrArabic = suggestion.arabic;
        TasbeehSessionStore.customZikrTranslit = suggestion.meaning;
        TasbeehSessionStore.customTarget = suggestion.recommendedCount;
        etTarget.setText(String.valueOf(suggestion.recommendedCount));
        TasbeehSessionStore.resetCustomCount();
        tvCount.setText("0");
        HapticHelper.milestone(btnTap);
        Toast.makeText(this, R.string.feeling_suggestion_ready, Toast.LENGTH_SHORT).show();
    }
}

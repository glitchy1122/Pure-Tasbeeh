package com.adreesulhassan.puretasbeeh.ui.tasbeeh;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.data.session.TasbeehSessionStore;
import com.adreesulhassan.puretasbeeh.util.HapticHelper;

/**
 * Guided Tasbeeh e Zehra (s.a): 34 Allahu Akbar → 33 Alhamdulillah → 33 Subhanallah.
 * Incomplete progress is kept in RAM; a finished round locks until Reset,
 * and leaving while finished auto-resets for the next visit.
 */
public class TasbeehZehraActivity extends AppCompatActivity {

    private enum Phase {
        ALLAHU_AKBAR(34),
        ALHAMDULILLAH(33),
        SUBHANALLAH(33);

        final int target;

        Phase(int target) {
            this.target = target;
        }
    }

    private Phase phase = Phase.ALLAHU_AKBAR;
    private int remaining;
    private boolean completed;
    private TextView tvPhaseLabel;
    private TextView tvZikrArabic;
    private TextView tvRemaining;
    private TextView tvTapHint;
    private FrameLayout btnTap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasbeeh_zehra);

        tvPhaseLabel = findViewById(R.id.tvPhaseLabel);
        tvZikrArabic = findViewById(R.id.tvZikrArabic);
        tvRemaining = findViewById(R.id.tvRemaining);
        tvTapHint = findViewById(R.id.tvTapHint);
        btnTap = findViewById(R.id.btnTap);

        findViewById(R.id.btnReset).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            resetAll();
            persistSession();
        });

        btnTap.setOnClickListener(v -> onTap());
        restoreFromSession();
    }

    @Override
    protected void onStop() {
        // Leaving a finished round clears RAM so the next visit starts fresh.
        TasbeehSessionStore.clearZehraIfCompleted();
        super.onStop();
    }

    private void restoreFromSession() {
        if (TasbeehSessionStore.zehraCompleted) {
            // Returning after a completed round (or mid-complete before leave) → fresh.
            resetAll();
            persistSession();
            return;
        }
        if (TasbeehSessionStore.zehraHasProgress) {
            Phase[] values = Phase.values();
            int ord = TasbeehSessionStore.zehraPhaseOrdinal;
            if (ord < 0 || ord >= values.length) {
                ord = 0;
            }
            phase = values[ord];
            remaining = Math.max(1, TasbeehSessionStore.zehraRemaining);
            completed = false;
            renderPhase();
            setTapEnabled(true);
            return;
        }
        resetAll();
        persistSession();
    }

    private void resetAll() {
        phase = Phase.ALLAHU_AKBAR;
        remaining = phase.target;
        completed = false;
        setTapEnabled(true);
        if (tvTapHint != null) {
            tvTapHint.setText(R.string.tap_to_count);
            tvTapHint.setTextSize(16f);
        }
        renderPhase();
    }

    private void onTap() {
        if (completed || !btnTap.isEnabled()) {
            return;
        }
        HapticHelper.tap(btnTap);
        remaining--;
        TasbeehSessionStore.zehraHasProgress = true;
        if (remaining <= 0) {
            HapticHelper.milestone(btnTap);
            advancePhase();
        } else {
            tvRemaining.setText(String.valueOf(remaining));
            persistSession();
        }
    }

    private void advancePhase() {
        switch (phase) {
            case ALLAHU_AKBAR:
                phase = Phase.ALHAMDULILLAH;
                remaining = phase.target;
                renderPhase();
                persistSession();
                break;
            case ALHAMDULILLAH:
                phase = Phase.SUBHANALLAH;
                remaining = phase.target;
                renderPhase();
                persistSession();
                break;
            case SUBHANALLAH:
                markCompleted();
                break;
            default:
                throw new IllegalStateException("Unexpected phase: " + phase);
        }
    }

    private void markCompleted() {
        completed = true;
        remaining = 0;
        tvRemaining.setText("✓");
        tvPhaseLabel.setText(R.string.tasbeeh_complete_short);
        tvZikrArabic.setText("");
        if (tvTapHint != null) {
            tvTapHint.setText(R.string.may_allah_accept);
            tvTapHint.setTextSize(15f);
        }
        setTapEnabled(false);
        persistSession();
    }

    private void setTapEnabled(boolean enabled) {
        btnTap.setEnabled(enabled);
        btnTap.setClickable(enabled);
        btnTap.setAlpha(enabled ? 1f : 0.55f);
    }

    private void persistSession() {
        TasbeehSessionStore.zehraPhaseOrdinal = phase.ordinal();
        TasbeehSessionStore.zehraRemaining = remaining;
        TasbeehSessionStore.zehraCompleted = completed;
        TasbeehSessionStore.zehraHasProgress = completed
                || phase != Phase.ALLAHU_AKBAR
                || remaining != Phase.ALLAHU_AKBAR.target;
        if (!completed && !TasbeehSessionStore.zehraHasProgress) {
            // Fresh start — nothing to keep.
            TasbeehSessionStore.resetZehra();
        }
    }

    private void renderPhase() {
        switch (phase) {
            case ALLAHU_AKBAR:
                tvPhaseLabel.setText(R.string.zikr_allahu_akbar_label);
                tvZikrArabic.setText(R.string.zikr_allahu_akbar);
                break;
            case ALHAMDULILLAH:
                tvPhaseLabel.setText(R.string.zikr_alhamdulillah_label);
                tvZikrArabic.setText(R.string.zikr_alhamdulillah);
                break;
            case SUBHANALLAH:
                tvPhaseLabel.setText(R.string.zikr_subhanallah_label);
                tvZikrArabic.setText(R.string.zikr_subhanallah);
                break;
            default:
                throw new IllegalStateException("Unexpected phase: " + phase);
        }
        tvRemaining.setText(String.valueOf(remaining));
    }
}

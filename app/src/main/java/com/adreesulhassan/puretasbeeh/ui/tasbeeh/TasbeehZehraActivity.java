package com.adreesulhassan.puretasbeeh.ui.tasbeeh;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.util.HapticHelper;

/**
 * Guided Tasbeeh e Zehra (s.a): 34 Allahu Akbar → 33 Alhamdulillah → 33 Subhanallah.
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
    private TextView tvPhaseLabel;
    private TextView tvZikrArabic;
    private TextView tvRemaining;
    private FrameLayout btnTap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasbeeh_zehra);

        tvPhaseLabel = findViewById(R.id.tvPhaseLabel);
        tvZikrArabic = findViewById(R.id.tvZikrArabic);
        tvRemaining = findViewById(R.id.tvRemaining);
        btnTap = findViewById(R.id.btnTap);

        findViewById(R.id.btnReset).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            resetAll();
        });

        btnTap.setOnClickListener(v -> onTap());
        resetAll();
    }

    private void resetAll() {
        phase = Phase.ALLAHU_AKBAR;
        remaining = phase.target;
        renderPhase();
    }

    private void onTap() {
        HapticHelper.tap(btnTap);
        remaining--;
        if (remaining <= 0) {
            HapticHelper.milestone(btnTap);
            advancePhase();
        } else {
            tvRemaining.setText(String.valueOf(remaining));
        }
    }

    private void advancePhase() {
        switch (phase) {
            case ALLAHU_AKBAR:
                phase = Phase.ALHAMDULILLAH;
                remaining = phase.target;
                renderPhase();
                break;
            case ALHAMDULILLAH:
                phase = Phase.SUBHANALLAH;
                remaining = phase.target;
                renderPhase();
                break;
            case SUBHANALLAH:
                Toast.makeText(this, R.string.tasbeeh_complete, Toast.LENGTH_LONG).show();
                resetAll();
                break;
            default:
                throw new IllegalStateException("Unexpected phase: " + phase);
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

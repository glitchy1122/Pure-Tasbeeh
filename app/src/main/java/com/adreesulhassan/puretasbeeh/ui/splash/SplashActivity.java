package com.adreesulhassan.puretasbeeh.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.data.prefs.FiqhPreferences;
import com.adreesulhassan.puretasbeeh.data.prefs.LanguagePreferences;
import com.adreesulhassan.puretasbeeh.ui.home.HomeActivity;
import com.adreesulhassan.puretasbeeh.util.HapticHelper;

/**
 * 1.5s Bismillah splash; first-launch language then Fiqh picker.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_MS = 1500L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private FiqhPreferences prefs;
    private LanguagePreferences languagePrefs;
    private boolean navigated;
    private AlertDialog fiqhDialog;
    private AlertDialog languageDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        prefs = new FiqhPreferences(this);
        languagePrefs = new LanguagePreferences(this);

        handler.postDelayed(this::afterSplash, SPLASH_MS);
    }

    private void afterSplash() {
        if (isFinishing() || navigated) {
            return;
        }
        if (!languagePrefs.isLanguageSet()) {
            showLanguageDialog();
        } else if (!prefs.isOnboarded()) {
            showFiqhDialog();
        } else {
            goHome();
        }
    }

    private void showLanguageDialog() {
        final String[] codes = {
                LanguagePreferences.LANG_EN,
                LanguagePreferences.LANG_UR,
                LanguagePreferences.LANG_AR,
                LanguagePreferences.LANG_FA
        };
        final String[] labels = {
                LanguagePreferences.displayName(LanguagePreferences.LANG_EN),
                LanguagePreferences.displayName(LanguagePreferences.LANG_UR),
                LanguagePreferences.displayName(LanguagePreferences.LANG_AR),
                LanguagePreferences.displayName(LanguagePreferences.LANG_FA)
        };
        languageDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.select_language_title)
                .setItems(labels, (d, which) -> {
                    languagePrefs.setLanguage(codes[which]);
                    if (!prefs.isOnboarded()) {
                        showFiqhDialog();
                    } else {
                        goHome();
                    }
                })
                .setCancelable(false)
                .create();
        languageDialog.show();
    }

    private void showFiqhDialog() {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_fiqh_select, null, false);

        fiqhDialog = new AlertDialog.Builder(this)
                .setView(content)
                .setCancelable(false)
                .create();

        if (fiqhDialog.getWindow() != null) {
            fiqhDialog.getWindow().setBackgroundDrawableResource(R.color.clay_bg);
        }

        content.findViewById(R.id.btnFiqhJafriya).setOnClickListener(v ->
                onFiqhChosen(v, FiqhPreferences.FIQH_JAFRIYA));
        content.findViewById(R.id.btnFiqhHanfiya).setOnClickListener(v ->
                onFiqhChosen(v, FiqhPreferences.FIQH_HANFIYA));
        content.findViewById(R.id.btnFiqhBoth).setOnClickListener(v ->
                onFiqhChosen(v, FiqhPreferences.FIQH_BOTH));

        fiqhDialog.show();
    }

    private void onFiqhChosen(View v, String fiqh) {
        HapticHelper.milestone(v);
        prefs.setFiqh(fiqh);
        if (fiqhDialog != null && fiqhDialog.isShowing()) {
            fiqhDialog.dismiss();
        }
        goHome();
    }

    private void goHome() {
        if (navigated) {
            return;
        }
        navigated = true;
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (fiqhDialog != null && fiqhDialog.isShowing()) {
            fiqhDialog.dismiss();
        }
        if (languageDialog != null && languageDialog.isShowing()) {
            languageDialog.dismiss();
        }
        super.onDestroy();
    }
}

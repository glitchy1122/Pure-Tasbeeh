package com.adreesulhassan.puretasbeeh;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adreesulhassan.puretasbeeh.data.db.AppDatabase;
import com.adreesulhassan.puretasbeeh.data.db.DatabaseHelper;
import com.adreesulhassan.puretasbeeh.data.prefs.LanguagePreferences;
import com.adreesulhassan.puretasbeeh.util.SafeAreaHelper;

/**
 * Pure Tasbeeh — offline-first Islamic app.
 */
public class PureTasbeehApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new LanguagePreferences(this).applySavedLocale();
        AppDatabase.getInstance(this);
        DatabaseHelper.getInstance(this).ensureCopiedAsync(null);
        registerSafeAreaHandling();
    }

    /** Keeps every screen clear of status/navigation bars and display cutouts. */
    private void registerSafeAreaHandling() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle state) {
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                SafeAreaHelper.apply(activity);
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity,
                                                    @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });
    }
}

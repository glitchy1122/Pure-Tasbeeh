package com.adreesulhassan.puretasbeeh;

import android.app.Application;

import com.adreesulhassan.puretasbeeh.data.db.AppDatabase;

/**
 * Pure Tasbeeh — offline-first Islamic app.
 * Property of Adrees ul Hassan. Not to be shared without permission.
 */
public class PureTasbeehApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Warm Room + seed dummy duas/books/ahadith on first create
        AppDatabase.getInstance(this);
    }
}

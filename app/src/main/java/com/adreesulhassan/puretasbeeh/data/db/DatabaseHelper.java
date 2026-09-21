package com.adreesulhassan.puretasbeeh.data.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Copies {@code assets/databases/quran_and_duas.db} into the app databases
 * directory on first launch (background thread), then serves Surah queries.
 * Re-copies when translation columns are missing.
 */
public final class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    public static final String DB_NAME = "quran_and_duas.db";
    private static final String ASSET_PATH = "databases/" + DB_NAME;
    private static final int DB_VERSION = 2;

    private static volatile DatabaseHelper INSTANCE;
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();

    private final Context appContext;
    private final AtomicBoolean copyStarted = new AtomicBoolean(false);
    private final AtomicBoolean ready = new AtomicBoolean(false);
    private volatile String lastError;

    public static final class Surah {
        public final int number;
        @NonNull
        public final String nameEn;
        @NonNull
        public final String nameAr;

        public Surah(int number, @NonNull String nameEn, @NonNull String nameAr) {
            this.number = number;
            this.nameEn = nameEn;
            this.nameAr = nameAr;
        }
    }

    public static final class Ayah {
        public final int surahNumber;
        public final int ayahNumber;
        @NonNull
        public final String textAr;
        @Nullable
        public final String textEn;
        @Nullable
        public final String textUr;
        @Nullable
        public final String textFa;

        public Ayah(int surahNumber, int ayahNumber, @NonNull String textAr,
                    @Nullable String textEn, @Nullable String textUr, @Nullable String textFa) {
            this.surahNumber = surahNumber;
            this.ayahNumber = ayahNumber;
            this.textAr = textAr;
            this.textEn = textEn;
            this.textUr = textUr;
            this.textFa = textFa;
        }

        @NonNull
        public String translationFor(@NonNull String lang) {
            switch (lang) {
                case "ur":
                    return textUr != null && !textUr.isEmpty() ? textUr
                            : (textEn != null ? textEn : "");
                case "fa":
                    return textFa != null && !textFa.isEmpty() ? textFa
                            : (textEn != null ? textEn : "");
                case "ar":
                    return textAr;
                default:
                    return textEn != null ? textEn : "";
            }
        }
    }

    public interface ReadyCallback {
        void onReady(boolean success, @Nullable String error);
    }

    private DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
        this.appContext = context.getApplicationContext();
    }

    public static DatabaseHelper getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DatabaseHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DatabaseHelper(context);
                }
            }
        }
        return INSTANCE;
    }

    /** Kick off asset → local DB copy on a background thread (idempotent). */
    public void ensureCopiedAsync(@Nullable ReadyCallback callback) {
        if (isDatabasePresent() && hasSurahTable() && hasAyahTable() && hasTranslationColumns()) {
            ready.set(true);
            if (callback != null) {
                callback.onReady(true, null);
            }
            return;
        }
        if (!copyStarted.compareAndSet(false, true) && !ready.get()) {
            IO.execute(() -> waitAndNotify(callback));
            return;
        }
        IO.execute(() -> {
            try {
                copyDatabaseFromAssetsIfNeeded();
                ready.set(true);
                lastError = null;
                if (callback != null) {
                    callback.onReady(true, null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to copy Quran database", e);
                lastError = e.getMessage();
                ready.set(false);
                copyStarted.set(false);
                if (callback != null) {
                    callback.onReady(false, lastError);
                }
            }
        });
    }

    private void waitAndNotify(@Nullable ReadyCallback callback) {
        for (int i = 0; i < 80 && !ready.get() && lastError == null; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (callback != null) {
            callback.onReady(ready.get(), lastError);
        }
    }

    private boolean isDatabasePresent() {
        File dbFile = appContext.getDatabasePath(DB_NAME);
        return dbFile.exists() && dbFile.length() > 1024;
    }

    private boolean hasSurahTable() {
        return tableExists("surahs");
    }

    private boolean tableExists(@NonNull String table) {
        File dbFile = appContext.getDatabasePath(DB_NAME);
        if (!dbFile.exists()) {
            return false;
        }
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = SQLiteDatabase.openDatabase(
                    dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{table});
            return cursor.moveToFirst();
        } catch (Exception e) {
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean isReady() {
        return ready.get() || (isDatabasePresent() && hasSurahTable()
                && hasAyahTable() && hasTranslationColumns());
    }

    private boolean hasAyahTable() {
        File dbFile = appContext.getDatabasePath(DB_NAME);
        if (!dbFile.exists()) {
            return false;
        }
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = SQLiteDatabase.openDatabase(
                    dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='ayahs'",
                    null);
            if (!cursor.moveToFirst()) {
                return false;
            }
            cursor.close();
            cursor = db.rawQuery("SELECT COUNT(*) FROM ayahs", null);
            return cursor.moveToFirst() && cursor.getInt(0) > 0;
        } catch (Exception e) {
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    private boolean hasTranslationColumns() {
        File dbFile = appContext.getDatabasePath(DB_NAME);
        if (!dbFile.exists()) {
            return false;
        }
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = SQLiteDatabase.openDatabase(
                    dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            cursor = db.rawQuery("PRAGMA table_info(ayahs)", null);
            boolean en = false;
            boolean ur = false;
            boolean fa = false;
            while (cursor.moveToNext()) {
                String name = cursor.getString(1);
                if ("text_en".equals(name)) {
                    en = true;
                } else if ("text_ur".equals(name)) {
                    ur = true;
                } else if ("text_fa".equals(name)) {
                    fa = true;
                }
            }
            if (!en || !ur || !fa) {
                return false;
            }
            cursor.close();
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM ayahs WHERE length(COALESCE(text_en,'')) > 0", null);
            return cursor.moveToFirst() && cursor.getInt(0) > 1000;
        } catch (Exception e) {
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    private void copyDatabaseFromAssetsIfNeeded() throws IOException {
        File dbFile = appContext.getDatabasePath(DB_NAME);
        if (isDatabasePresent() && hasSurahTable() && hasAyahTable() && hasTranslationColumns()) {
            return;
        }
        // Remove stale stub / older asset without translations
        if (dbFile.exists() && !dbFile.delete()) {
            throw new IOException("Cannot replace existing database file");
        }
        File parent = dbFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Cannot create databases directory");
        }
        try (InputStream in = appContext.getAssets().open(ASSET_PATH);
             OutputStream out = new FileOutputStream(dbFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }
        if (!hasSurahTable() || !hasAyahTable() || !hasTranslationColumns()) {
            throw new IOException("Copied database is missing surahs/ayahs/translations");
        }
    }

    @NonNull
    public List<Surah> getAllSurahs() {
        List<Surah> list = new ArrayList<>();
        File dbFile = appContext.getDatabasePath(DB_NAME);
        if (!dbFile.exists()) {
            return list;
        }
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = SQLiteDatabase.openDatabase(
                    dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            cursor = db.rawQuery(
                    "SELECT number, name_en, name_ar FROM surahs ORDER BY number ASC",
                    null);
            while (cursor.moveToNext()) {
                list.add(new Surah(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2)
                ));
            }
        } catch (Exception e) {
            Log.e(TAG, "getAllSurahs failed", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return list;
    }

    @Nullable
    public Surah getSurah(int number) {
        File dbFile = appContext.getDatabasePath(DB_NAME);
        if (!dbFile.exists()) {
            return null;
        }
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = SQLiteDatabase.openDatabase(
                    dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            cursor = db.rawQuery(
                    "SELECT number, name_en, name_ar FROM surahs WHERE number = ? LIMIT 1",
                    new String[]{String.valueOf(number)});
            if (cursor.moveToFirst()) {
                return new Surah(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
            }
        } catch (Exception e) {
            Log.e(TAG, "getSurah failed", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return null;
    }

    @NonNull
    public List<Ayah> getAyahsForSurah(int surahNumber) {
        List<Ayah> list = new ArrayList<>();
        File dbFile = appContext.getDatabasePath(DB_NAME);
        if (!dbFile.exists()) {
            return list;
        }
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = SQLiteDatabase.openDatabase(
                    dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            cursor = db.rawQuery(
                    "SELECT surah_number, ayah_number, text_ar, text_en, text_ur, text_fa "
                            + "FROM ayahs WHERE surah_number = ? ORDER BY ayah_number ASC",
                    new String[]{String.valueOf(surahNumber)});
            while (cursor.moveToNext()) {
                list.add(new Ayah(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        nullToEmpty(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5)
                ));
            }
        } catch (Exception e) {
            Log.e(TAG, "getAyahsForSurah failed", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return list;
    }

    @NonNull
    private static String nullToEmpty(@Nullable String s) {
        return s != null ? s : "";
    }

    @NonNull
    public List<Ayah> getRandomAyahs(int count) {
        List<Ayah> list = new ArrayList<>();
        File dbFile = appContext.getDatabasePath(DB_NAME);
        if (!dbFile.exists() || count <= 0) {
            return list;
        }
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = SQLiteDatabase.openDatabase(
                    dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            cursor = db.rawQuery(
                    "SELECT surah_number, ayah_number, text_ar, text_en, text_ur, text_fa "
                            + "FROM ayahs ORDER BY RANDOM() LIMIT ?",
                    new String[]{String.valueOf(count)});
            while (cursor.moveToNext()) {
                list.add(new Ayah(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        nullToEmpty(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5)
                ));
            }
        } catch (Exception e) {
            Log.e(TAG, "getRandomAyahs failed", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return list;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Prebuilt asset DB — schema lives in assets/databases/quran_and_duas.db
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Asset re-copy is handled in ensureCopiedAsync when columns are missing.
    }
}

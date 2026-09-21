package com.adreesulhassan.puretasbeeh.data.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import com.adreesulhassan.puretasbeeh.data.entity.HadithEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads curated ahadith from {@code assets/databases/ahadith.db}
 * (500+ Sunni Bukhari + 500+ Shia Al-Kafi).
 */
public final class HadithAssetLoader {

    private static final String TAG = "HadithAssetLoader";
    private static final String ASSET = "databases/ahadith.db";
    private static final String CACHE_NAME = "ahadith_asset.db";

    private HadithAssetLoader() {
    }

    @NonNull
    public static List<HadithEntity> loadAll(@NonNull Context context) {
        List<HadithEntity> list = new ArrayList<>();
        File local = new File(context.getCacheDir(), CACHE_NAME);
        try {
            copyAsset(context, local);
            SQLiteDatabase db = SQLiteDatabase.openDatabase(
                    local.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            Cursor c = db.rawQuery(
                    "SELECT arabic_text, translation, translation_ur, translation_ar, "
                            + "translation_fa, source, sect_tag FROM ahadith ORDER BY id ASC",
                    null);
            while (c.moveToNext()) {
                list.add(new HadithEntity(
                        nullToEmpty(c.getString(0)),
                        nullToEmpty(c.getString(1)),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4),
                        nullToEmpty(c.getString(5)),
                        nullToEmpty(c.getString(6))
                ));
            }
            c.close();
            db.close();
        } catch (Exception e) {
            Log.e(TAG, "Failed to load ahadith asset", e);
        }
        return list;
    }

    private static void copyAsset(@NonNull Context context, @NonNull File dest) throws Exception {
        // Always refresh from APK so upgrades pick up larger corpora.
        try (InputStream in = context.getAssets().open(ASSET);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            out.flush();
        }
    }

    @NonNull
    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }
}

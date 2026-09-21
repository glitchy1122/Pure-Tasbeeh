package com.adreesulhassan.puretasbeeh.data.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.adreesulhassan.puretasbeeh.data.entity.ContentEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** Loads full-page duas from assets/databases/duas.db into Room. */
public final class ContentAssetLoader {

    private static final String TAG = "ContentAssetLoader";
    private static final String ASSET = "databases/duas.db";

    private ContentAssetLoader() {
    }

    @androidx.annotation.NonNull
    public static List<ContentEntity> loadAll(Context context) {
        List<ContentEntity> out = new ArrayList<>();
        try {
            File local = new File(context.getFilesDir(), "duas_asset.db");
            try (InputStream in = context.getAssets().open(ASSET);
                 FileOutputStream outFs = new FileOutputStream(local)) {
                byte[] buf = new byte[16 * 1024];
                int n;
                while ((n = in.read(buf)) > 0) {
                    outFs.write(buf, 0, n);
                }
            }
            SQLiteDatabase db = SQLiteDatabase.openDatabase(
                    local.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            Cursor c = db.rawQuery(
                    "SELECT title, arabic_text, translation, translation_ur, translation_ar, "
                            + "translation_fa, source, sect_tag, category FROM content ORDER BY id ASC",
                    null);
            while (c.moveToNext()) {
                out.add(new ContentEntity(
                        c.getString(0),
                        c.getString(1),
                        c.getString(2),
                        c.isNull(3) ? null : c.getString(3),
                        c.isNull(4) ? null : c.getString(4),
                        c.isNull(5) ? null : c.getString(5),
                        c.isNull(6) ? null : c.getString(6),
                        c.getString(7),
                        c.getString(8)));
            }
            c.close();
            db.close();
        } catch (Exception e) {
            Log.e(TAG, "Failed to load duas.db", e);
        }
        return out;
    }
}

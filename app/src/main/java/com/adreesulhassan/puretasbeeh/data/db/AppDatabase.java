package com.adreesulhassan.puretasbeeh.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.adreesulhassan.puretasbeeh.data.dao.BookDao;
import com.adreesulhassan.puretasbeeh.data.dao.ContentDao;
import com.adreesulhassan.puretasbeeh.data.dao.HadithDao;
import com.adreesulhassan.puretasbeeh.data.entity.BookEntity;
import com.adreesulhassan.puretasbeeh.data.entity.ContentEntity;
import com.adreesulhassan.puretasbeeh.data.entity.HadithEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {ContentEntity.class, BookEntity.class, HadithEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DB_NAME = "pure_tasbeeh.db";

    private static volatile AppDatabase INSTANCE;
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();

    public abstract ContentDao contentDao();

    public abstract BookDao bookDao();

    public abstract HadithDao hadithDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME)
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    IO.execute(() -> {
                                        if (INSTANCE != null) {
                                            DatabaseSeeder.seed(INSTANCE);
                                        }
                                    });
                                }
                            })
                            .fallbackToDestructiveMigration()
                            .build();
                    // Ensure seed also runs for fresh installs after first open
                    IO.execute(() -> DatabaseSeeder.seed(INSTANCE));
                }
            }
        }
        return INSTANCE;
    }

    public static ExecutorService io() {
        return IO;
    }
}

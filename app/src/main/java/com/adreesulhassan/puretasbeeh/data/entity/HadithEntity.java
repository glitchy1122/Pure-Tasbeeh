package com.adreesulhassan.puretasbeeh.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Hadith of the Day source rows.
 */
@Entity(tableName = "ahadith")
public class HadithEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "arabic_text")
    public String arabicText;

    @NonNull
    @ColumnInfo(name = "translation")
    public String translation;

    @NonNull
    @ColumnInfo(name = "source")
    public String source;

    @NonNull
    @ColumnInfo(name = "sect_tag")
    public String sectTag;

    public HadithEntity(@NonNull String arabicText,
                        @NonNull String translation,
                        @NonNull String source,
                        @NonNull String sectTag) {
        this.arabicText = arabicText;
        this.translation = translation;
        this.source = source;
        this.sectTag = sectTag;
    }
}

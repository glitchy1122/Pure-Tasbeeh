package com.adreesulhassan.puretasbeeh.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Dua / Munajat content entity.
 * sect_tag: SHIA | SUNNI | BOTH
 * category: NAMAZ | SPECIAL | MONTHS | DAILY
 */
@Entity(tableName = "content")
public class ContentEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "title")
    public String title;

    @NonNull
    @ColumnInfo(name = "arabic_text")
    public String arabicText;

    @NonNull
    @ColumnInfo(name = "translation")
    public String translation;

    @NonNull
    @ColumnInfo(name = "sect_tag")
    public String sectTag;

    @NonNull
    @ColumnInfo(name = "category")
    public String category;

    public ContentEntity(@NonNull String title,
                         @NonNull String arabicText,
                         @NonNull String translation,
                         @NonNull String sectTag,
                         @NonNull String category) {
        this.title = title;
        this.arabicText = arabicText;
        this.translation = translation;
        this.sectTag = sectTag;
        this.category = category;
    }
}

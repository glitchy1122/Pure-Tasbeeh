package com.adreesulhassan.puretasbeeh.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Dua / Munajat — Arabic fixed; translations per language.
 * category: NAMAZ | SPECIAL | MONTHS | DAILY
 * sect_tag: SHIA | SUNNI | BOTH
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

    @Nullable
    @ColumnInfo(name = "translation_ur")
    public String translationUr;

    @Nullable
    @ColumnInfo(name = "translation_ar")
    public String translationAr;

    @Nullable
    @ColumnInfo(name = "translation_fa")
    public String translationFa;

    @Nullable
    @ColumnInfo(name = "source")
    public String source;

    @NonNull
    @ColumnInfo(name = "sect_tag")
    public String sectTag;

    @NonNull
    @ColumnInfo(name = "category")
    public String category;

    public ContentEntity(@NonNull String title,
                         @NonNull String arabicText,
                         @NonNull String translation,
                         @Nullable String translationUr,
                         @Nullable String translationAr,
                         @Nullable String translationFa,
                         @Nullable String source,
                         @NonNull String sectTag,
                         @NonNull String category) {
        this.title = title;
        this.arabicText = arabicText;
        this.translation = translation;
        this.translationUr = translationUr;
        this.translationAr = translationAr;
        this.translationFa = translationFa;
        this.source = source;
        this.sectTag = sectTag;
        this.category = category;
    }

    @NonNull
    public String translationFor(@NonNull String lang) {
        switch (lang) {
            case "ur":
                return translationUr != null && !translationUr.isEmpty() ? translationUr : translation;
            case "ar":
                if (translationAr != null
                        && !translationAr.isEmpty()
                        && !translationAr.equals(arabicText)) {
                    return translationAr;
                }
                return translation;
            case "fa":
                return translationFa != null && !translationFa.isEmpty() ? translationFa : translation;
            default:
                return translation;
        }
    }
}

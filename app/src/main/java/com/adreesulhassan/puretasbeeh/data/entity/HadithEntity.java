package com.adreesulhassan.puretasbeeh.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Hadith of the Day / Ahadith library rows.
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

    @Nullable
    @ColumnInfo(name = "translation_ur")
    public String translationUr;

    @Nullable
    @ColumnInfo(name = "translation_ar")
    public String translationAr;

    @Nullable
    @ColumnInfo(name = "translation_fa")
    public String translationFa;

    @NonNull
    @ColumnInfo(name = "source")
    public String source;

    @NonNull
    @ColumnInfo(name = "sect_tag")
    public String sectTag;

    public HadithEntity(@NonNull String arabicText,
                        @NonNull String translation,
                        @Nullable String translationUr,
                        @Nullable String translationAr,
                        @Nullable String translationFa,
                        @NonNull String source,
                        @NonNull String sectTag) {
        this.arabicText = arabicText;
        this.translation = translation;
        this.translationUr = translationUr;
        this.translationAr = translationAr;
        this.translationFa = translationFa;
        this.source = source;
        this.sectTag = sectTag;
    }

    @NonNull
    public String translationFor(@NonNull String lang) {
        switch (lang) {
            case "ur":
                return translationUr != null && !translationUr.isEmpty() ? translationUr : translation;
            case "ar":
                // Avoid duplicating arabicText as the "translation" line.
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

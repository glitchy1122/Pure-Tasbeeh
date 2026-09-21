package com.adreesulhassan.puretasbeeh.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Islamic books — tagged by sect for Fiqh filtering.
 */
@Entity(tableName = "books")
public class BookEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "book_name")
    public String bookName;

    @NonNull
    @ColumnInfo(name = "author")
    public String author;

    @NonNull
    @ColumnInfo(name = "download_url")
    public String downloadUrl;

    @NonNull
    @ColumnInfo(name = "sect_tag")
    public String sectTag;

    public BookEntity(@NonNull String bookName,
                      @NonNull String author,
                      @NonNull String downloadUrl,
                      @NonNull String sectTag) {
        this.bookName = bookName;
        this.author = author;
        this.downloadUrl = downloadUrl;
        this.sectTag = sectTag;
    }
}

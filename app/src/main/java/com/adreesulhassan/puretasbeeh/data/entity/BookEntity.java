package com.adreesulhassan.puretasbeeh.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Universal Islamic books (no sect tag).
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

    public BookEntity(@NonNull String bookName,
                      @NonNull String author,
                      @NonNull String downloadUrl) {
        this.bookName = bookName;
        this.author = author;
        this.downloadUrl = downloadUrl;
    }
}

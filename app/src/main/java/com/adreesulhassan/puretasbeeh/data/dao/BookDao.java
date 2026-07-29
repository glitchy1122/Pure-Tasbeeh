package com.adreesulhassan.puretasbeeh.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.adreesulhassan.puretasbeeh.data.entity.BookEntity;

import java.util.List;

@Dao
public interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BookEntity> books);

    @Query("SELECT * FROM books ORDER BY book_name ASC")
    LiveData<List<BookEntity>> observeAll();

    @Query("SELECT * FROM books ORDER BY book_name ASC")
    List<BookEntity> getAll();

    @Query("SELECT COUNT(*) FROM books")
    int count();
}

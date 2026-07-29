package com.adreesulhassan.puretasbeeh.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.adreesulhassan.puretasbeeh.data.entity.ContentEntity;

import java.util.List;

@Dao
public interface ContentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ContentEntity> items);

    @Query("SELECT * FROM content WHERE id = :id LIMIT 1")
    ContentEntity getById(long id);

    @Query("SELECT * FROM content WHERE category = :category AND "
            + "(sect_tag = :sect OR sect_tag = 'BOTH') ORDER BY title ASC")
    LiveData<List<ContentEntity>> observeByCategoryAndSect(String category, String sect);

    @Query("SELECT * FROM content WHERE category = :category AND "
            + "(sect_tag = :sect OR sect_tag = 'BOTH') ORDER BY title ASC")
    List<ContentEntity> getByCategoryAndSect(String category, String sect);

    @Query("SELECT COUNT(*) FROM content")
    int count();
}

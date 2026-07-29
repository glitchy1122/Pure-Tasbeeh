package com.adreesulhassan.puretasbeeh.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.adreesulhassan.puretasbeeh.data.entity.HadithEntity;

import java.util.List;

@Dao
public interface HadithDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<HadithEntity> items);

    @Query("SELECT * FROM ahadith WHERE sect_tag = :sect OR sect_tag = 'BOTH' "
            + "ORDER BY RANDOM() LIMIT 1")
    HadithEntity getRandomForSect(String sect);

    @Query("SELECT COUNT(*) FROM ahadith")
    int count();
}

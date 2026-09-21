package com.adreesulhassan.puretasbeeh.data.dao;

import androidx.lifecycle.LiveData;
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

    @Query("DELETE FROM ahadith")
    void deleteAll();

    @Query("SELECT * FROM ahadith WHERE sect_tag = :sect OR sect_tag = 'BOTH' "
            + "ORDER BY RANDOM() LIMIT 1")
    HadithEntity getRandomForSect(String sect);

    @Query("SELECT * FROM ahadith WHERE (sect_tag = :sect OR sect_tag = 'BOTH') "
            + "AND id NOT IN (:excludeIds) ORDER BY RANDOM() LIMIT 1")
    HadithEntity getRandomForSectExcluding(String sect, List<Long> excludeIds);

    @Query("SELECT * FROM ahadith WHERE id NOT IN (:excludeIds) ORDER BY RANDOM() LIMIT 1")
    HadithEntity getRandomExcluding(List<Long> excludeIds);

    @Query("SELECT * FROM ahadith ORDER BY RANDOM() LIMIT 1")
    HadithEntity getRandomAny();

    @Query("SELECT COUNT(*) FROM ahadith WHERE sect_tag = :sect OR sect_tag = 'BOTH'")
    int countForSect(String sect);

    @Query("SELECT * FROM ahadith WHERE sect_tag = :sect OR sect_tag = 'BOTH' "
            + "ORDER BY id ASC")
    LiveData<List<HadithEntity>> observeForSect(String sect);

    @Query("SELECT * FROM ahadith ORDER BY id ASC")
    LiveData<List<HadithEntity>> observeAll();

    @Query("SELECT COUNT(*) FROM ahadith")
    int count();

    @Query("SELECT * FROM ahadith WHERE id = :id LIMIT 1")
    HadithEntity getById(long id);
}

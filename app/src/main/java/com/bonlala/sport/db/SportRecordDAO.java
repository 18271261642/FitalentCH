package com.bonlala.sport.db;



import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Create by sjh
 *
 * @Date 2024/4/11
 * @Desc
 */

@Dao
public interface SportRecordDAO {


    @Insert()
    void insertOneData(SportRecordDb sportRecordDb);

    @Insert
    void insertAllData(SportRecordDb...sportRecordDbs);


    @Query("SELECT * FROM SportRecordDb")
    List<SportRecordDb> findAllRecord();


    @Query("SELECT * FROM SportRecordDb WHERE day IN (:day)")
    List<SportRecordDb> findAllRecordByDay(String day);


    @Query("SELECT * FROM SportRecordDb WHERE day IN (:day) AND sportType IN (:type)")
    List<SportRecordDb> findDbByDayAndType(String day,int type);

    @Query("SELECT * FROM SportRecordDb WHERE  sportType IN (:type)")
    List<SportRecordDb> findDbByType(int type);

}

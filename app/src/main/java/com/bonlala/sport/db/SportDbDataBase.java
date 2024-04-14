package com.bonlala.sport.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


/**
 * Create by sjh
 *
 * @Date 2024/4/11
 * @Desc
 */
@Database(entities = {SportRecordDb.class},version = 1,exportSchema = false)
public abstract class SportDbDataBase extends RoomDatabase {


    private static SportDbDataBase sportDbDataBase;


    public static synchronized SportDbDataBase getSportDbDataBase(Context context){
        if(sportDbDataBase == null){
            sportDbDataBase = Room.databaseBuilder(context, SportDbDataBase.class,"fitalent_room_db")
                    .allowMainThreadQueries()
                    .build();
        }
        return sportDbDataBase;
    }


    public abstract SportRecordDAO sportRecordDAO();

}

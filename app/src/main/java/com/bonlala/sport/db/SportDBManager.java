package com.bonlala.sport.db;

import com.bonlala.fitalent.BaseApplication;
import com.bonlala.sport.model.SportTotalBean;
import com.google.gson.Gson;

import java.sql.PreparedStatement;
import java.util.List;

import timber.log.Timber;

/**
 * Create by sjh
 *
 * @Date 2024/4/11
 * @Desc
 */
public class SportDBManager {

    private volatile static SportDBManager sportDBManager;


    public static synchronized SportDBManager getSportDBManager(){
        if(sportDBManager == null){
            synchronized (SportDBManager.class){
                sportDBManager = new SportDBManager();
            }
        }
        return sportDBManager;
    }

    private SportDBManager() {
    }


    //保存一条运动数据
    public void  saveOneSportRecord(SportRecordDb sportRecordDb){
        BaseApplication.getInstance().getSportDbDataBase().sportRecordDAO().insertOneData(sportRecordDb);
    }


    public List<SportRecordDb> getRecordByDay(String day){
        List<SportRecordDb> list = BaseApplication.getInstance().getSportDbDataBase().sportRecordDAO().findAllRecordByDay(day);
        return list;
    }


    public SportRecordDb findDbByType(int type){
        List<SportRecordDb> list = BaseApplication.getInstance().getSportDbDataBase().sportRecordDAO().findDbByType(type);

        return list == null || list.isEmpty() ? null : list.get(list.size() - 1);
    }

    public SportTotalBean getTotalByType(int type){
        List<SportRecordDb> list = BaseApplication.getInstance().getSportDbDataBase().sportRecordDAO().findDbByType(type);
        if(list == null || list.isEmpty()){
            return new SportTotalBean();
        }else{
            int dis = 0;
            int time = 0;
            int kcal = 0;
            Timber.e("_-----------汇总的="+new Gson().toJson(list));
            for(SportRecordDb sportRecordDb : list){
                dis+=sportRecordDb.getTotalDistance();
                time +=sportRecordDb.getSportTotalTime();
                kcal +=sportRecordDb.getTotalKcal();
            }
            SportTotalBean sb = new SportTotalBean(dis,time,kcal);
            return sb;
        }
    }

}

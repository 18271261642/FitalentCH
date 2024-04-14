package com.bonlala.fitalent.db.model;

import org.litepal.crud.LitePalSupport;

/**
 * 用于心率带运动模式强制退出时保存运动数据的表，已转化成json，使用时再反序列化
 * 尽量只保存一条数据，多余的覆盖掉
 * Created by Admin
 * Date 2023/2/15
 * @author Admin
 */
public class HrBeltExitExerciseModel extends LitePalSupport {


    /**userid**/
    private String userId;

    /**mac**/
    private String deviceMac;

    /**
     * type 暂时无用，保留
     */
    private int type;


    /**
     * 数据源
     * com.bonlala.fitalent.db.model.ExerciseModel 序列化字符串保存
     */
    private String exerciseStr;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getExerciseStr() {
        return exerciseStr;
    }

    public void setExerciseStr(String exerciseStr) {
        this.exerciseStr = exerciseStr;
    }
}

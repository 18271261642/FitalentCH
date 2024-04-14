package com.bonlala.sport.db;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

/**
 * Create by sjh
 *
 * @Date 2024/4/11
 * @Desc
 */


@Entity
public class SportRecordDb {

    @PrimaryKey(autoGenerate = true)
    private int autoId;

    private int recordId;

    private int sportType;

    private String userId;

    private String day;


    private String deviceName;

    private String deviceMac;

    private long startTime;

    private long endTime;

    private int sportTotalTime;

    private int totalKcal;

    private int totalDistance;

    private int totalStep;

    private String heartList;

    private String stepFrequencyList;

    private String stepRangeList;

    private String paceList;


    private String pointList;



    public int getAutoId() {
        return autoId;
    }

    public void setAutoId(int autoId) {
        this.autoId = autoId;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getSportType() {
        return sportType;
    }

    public void setSportType(int sportType) {
        this.sportType = sportType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getSportTotalTime() {
        return sportTotalTime;
    }

    public void setSportTotalTime(int sportTotalTime) {
        this.sportTotalTime = sportTotalTime;
    }

    public int getTotalKcal() {
        return totalKcal;
    }

    public void setTotalKcal(int totalKcal) {
        this.totalKcal = totalKcal;
    }

    public int getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(int totalDistance) {
        this.totalDistance = totalDistance;
    }

    public int getTotalStep() {
        return totalStep;
    }

    public void setTotalStep(int totalStep) {
        this.totalStep = totalStep;
    }

    public String getHeartList() {
        return heartList;
    }

    public void setHeartList(String heartList) {
        this.heartList = heartList;
    }

    public String getStepFrequencyList() {
        return stepFrequencyList;
    }

    public void setStepFrequencyList(String stepFrequencyList) {
        this.stepFrequencyList = stepFrequencyList;
    }

    public String getStepRangeList() {
        return stepRangeList;
    }

    public void setStepRangeList(String stepRangeList) {
        this.stepRangeList = stepRangeList;
    }

    public String getPaceList() {
        return paceList;
    }

    public void setPaceList(String paceList) {
        this.paceList = paceList;
    }

    public String getPointList() {
        return pointList;
    }

    public void setPointList(String pointList) {
        this.pointList = pointList;
    }
}

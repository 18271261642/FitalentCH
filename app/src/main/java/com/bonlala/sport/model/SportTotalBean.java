package com.bonlala.sport.model;

public class SportTotalBean {

    private int totalDistance;

    private int totalTime;

    private int totalConsume;

    public SportTotalBean() {
    }

    public SportTotalBean(int totalDistance, int totalTime, int totalConsume) {
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
        this.totalConsume = totalConsume;
    }

    public int getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(int totalDistance) {
        this.totalDistance = totalDistance;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public int getTotalConsume() {
        return totalConsume;
    }

    public void setTotalConsume(int totalConsume) {
        this.totalConsume = totalConsume;
    }
}

package com.bonlala.sport.model;

/**
 * Create by sjh
 *
 * @Date 2024/4/11
 * @Desc
 */
public class SportingBean {

    /**精度**/
    private double lat;

    /**纬度**/
    private double lng;

    /**GPS速度**/
    private float speed;

    /**精准度**/
    private float accuracy;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }
}

package com.bonlala.sport;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.bonlala.fitalent.db.DBManager;
import com.bonlala.fitalent.utils.BikeUtils;
import com.bonlala.sport.amap.AmapLocationService;
import com.bonlala.sport.db.SportDBManager;
import com.bonlala.sport.db.SportRecordDb;
import com.bonlala.sport.model.SportingBean;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import timber.log.Timber;

/**
 * Create by sjh
 *
 * @Date 2024/4/11
 * @Desc
 */
public class SportAmapService extends Service {


    private OnSportingBackLister onSportingBackLister;

    private TempSportBean tempSportBean;

    private int sportType = 0;
    private long startSportTime ;

    private int initSensorStep = 0;
    private boolean isUseSensorStep = false;
    private int totalSensorStep = 0;




    public void setOnSportingBackLister(OnSportingBackLister onSportingBackListers) {
        this.onSportingBackLister = onSportingBackListers;
    }

    private AmapLocationService amapLocationService;

    private SensorManager sensorManager;

    private final IBinder binder = new  SportBinder();

    //是否开启了运动
    private boolean isStartSport = false;
    //是否已经暂停了
    private boolean isPauseSport = false;


    //开启运动后的经纬度，有效的经纬度
    private final List<LatLng> sportPointMap =new ArrayList<>();

    //运动的总距离
    private float sportTotalDistance = 0.0F;
    //运动的时间
    private int sportTotalTime = 0;



    //最近一次的定位经纬度
    private LatLng recentLatLng;


    private final Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0x00){
                if(!isPauseSport){
                    sportTotalTime++;
                }
                startTimer();
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        amapLocationService = new AmapLocationService(this);
        amapLocationService.setOnLocationListener(onLocationListener);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class SportBinder extends Binder{
        public SportAmapService getService(){
            return SportAmapService.this;
        }
    }



    //开始运动
    public void startToSport(int type){
        this.sportType = type;
        startSportTime = System.currentTimeMillis()/1000;
        isStartSport = true;
        isUseSensorStep = false;
        isPauseSport = false;
        totalSensorStep = 0;
        sportTotalTime = 0;
        sportTotalDistance = 0;
        sportPointMap.clear();
        amapLocationService.startLocation();
        startTimer();

        sensorManager.unregisterListener(sensorEvent,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(sensorEvent,sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER));


        sensorManager.registerListener(sensorEvent,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEvent,sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),SensorManager.SENSOR_DELAY_NORMAL);

    }

    //暂停
    public void setPauseOrContinueSport(boolean isPause){
        this.isPauseSport = isPause;
    }

    //结束运动
    public void stopToSport(){

        amapLocationService.stopLocation();
        amapLocationService.destroyLocation();
        handler.removeMessages(0x00);
        sensorManager.unregisterListener(sensorEvent);

        saveSportData();

    }

    public void clearListener(){
        if(onSportingBackLister != null){
            onSportingBackLister = null;
        }
    }


    private void startTimer(){
        handler.postDelayed(runnable,1000);
        if(onSportingBackLister != null){
            if(isPauseSport){
                return;
            }
            onSportingBackLister.backTimerTime(sportTotalTime);
        }
    }

    private final Runnable runnable= new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(0x00);
        }
    };


    public LatLng getRecentLatLng(){
        return recentLatLng == null ? new LatLng(39.918092,116.396798) : recentLatLng;
    }


    private final SensorEventListener sensorEvent = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                float[] values = sensorEvent.values;
               // Timber.e("--------计步变化Sensor.TYPE_ACCELEROMETER="+new Gson().toJson(values));
            }

            if(sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
                float[] values = sensorEvent.values;
                Timber.e("--------计步变化="+new Gson().toJson(values));
                int step = (int) values[0];
                if(isStartSport && !isPauseSport){
                    if(!isUseSensorStep){
                        isUseSensorStep = true;
                        initSensorStep = step;
                    }
                   int realStep = step-initSensorStep;
                    totalSensorStep = realStep;
                    Timber.e("---------sensor的计步="+totalSensorStep);
                }
                if(isStartSport && isPauseSport){
                    initSensorStep = step;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            Timber.e("--------计步变化onAccuracyChanged="+i);
        }
    };





    private SportingBean sportingBean;

    private LatLng lastLatLng;

    private final AmapLocationService.OnLocationListener onLocationListener = new AmapLocationService.OnLocationListener() {
        @Override
        public void backLocalLatLon(AMapLocation searLocalBean) {
            recentLatLng = new LatLng(searLocalBean.getLatitude(),searLocalBean.getLongitude());
            Timber.e("--------服务定位回调="+searLocalBean.getLongitude()+" "+(onSportingBackLister == null)+" "+searLocalBean.getLatitude()+" "+searLocalBean.getLongitude());
            if(isStartSport && !isPauseSport){
                if(lastLatLng != null){
                    float dis = AMapUtils.calculateLineDistance(lastLatLng,new LatLng(searLocalBean.getLatitude(),searLocalBean.getLongitude()));
                    sportTotalDistance = dis+sportTotalDistance;
                }

                sportingBean = new SportingBean();
                sportPointMap.add(new LatLng(searLocalBean.getLatitude(),searLocalBean.getLongitude()));
                sportingBean.setLat(searLocalBean.getLatitude());
                sportingBean.setLng(searLocalBean.getLongitude());
                sportingBean.setSpeed(searLocalBean.getSpeed());
                sportingBean.setAccuracy(searLocalBean.getAccuracy());

                lastLatLng = new LatLng(searLocalBean.getLatitude(),searLocalBean.getLongitude());
                tempSportBean = new TempSportBean(sportTotalTime,sportTotalDistance, (int) searLocalBean.getSpeed());

                if(onSportingBackLister != null){
                    if(isPauseSport){
                        return;
                    }
                    onSportingBackLister.backSportData(sportingBean,sportPointMap,sportTotalDistance);
                }
            }
        }
    };

    public List<LatLng> getSportPointMap(){
        return sportPointMap;
    }

    public TempSportBean getTempSportBean() {
        return tempSportBean;
    }

    public boolean isPauseSport() {
        return isPauseSport;
    }

    public static class TempSportBean{
        private int totalTime;

        private float totalDis;

        private int pace;

        public TempSportBean() {
        }

        public TempSportBean(int totalTime, float totalDis, int pace) {
            this.totalTime = totalTime;
            this.totalDis = totalDis;
            this.pace = pace;
        }

        public int getTotalTime() {
            return totalTime;
        }

        public void setTotalTime(int totalTime) {
            this.totalTime = totalTime;
        }

        public float getTotalDis() {
            return totalDis;
        }

        public void setTotalDis(float totalDis) {
            this.totalDis = totalDis;
        }

        public int getPace() {
            return pace;
        }

        public void setPace(int pace) {
            this.pace = pace;
        }
    }


    //保存运动的数据
    public void saveSportData(){
        if(sportTotalDistance>0){
            List<SportRecordDb> list = SportDBManager.getSportDBManager().getRecordByDay(BikeUtils.getCurrDate());
            int index = list == null || list.isEmpty() ? 1 : list.size()+1;

            SportRecordDb sportRecordDb = new SportRecordDb();
            sportRecordDb.setAutoId(index);
            sportRecordDb.setDay(BikeUtils.getCurrDate());
            sportRecordDb.setSportType(sportType);
            sportRecordDb.setSportTotalTime(sportTotalTime);
            sportRecordDb.setTotalDistance(sportTotalDistance == 0 ? 0 : (int) (sportTotalDistance));
            sportRecordDb.setTotalStep(totalSensorStep);
            sportRecordDb.setEndTime(System.currentTimeMillis()/1000);
            sportRecordDb.setStartTime(startSportTime);

            List<double[]> list1 = new ArrayList<>();
            for(LatLng latLng : sportPointMap){
                double[] a = new double[]{latLng.latitude,latLng.longitude};
                list1.add(a);
            }

            sportRecordDb.setPointList(new Gson().toJson(list1));

            SportDBManager.getSportDBManager().saveOneSportRecord(sportRecordDb);
        }


        isStartSport = false;
        isUseSensorStep = false;
        isPauseSport = false;
    }
}

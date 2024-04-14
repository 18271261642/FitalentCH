package com.bonlala.fitalent.ble;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.blala.blalable.BleConstant;
import com.blala.blalable.BleOperateManager;
import com.blala.blalable.Utils;
import com.blala.blalable.listener.OnCommBackDataListener;
import com.blala.blalable.listener.WriteBack24HourDataListener;
import com.bonlala.fitalent.db.DBManager;
import com.bonlala.fitalent.emu.DbType;
import com.bonlala.fitalent.utils.BikeUtils;
import com.bonlala.fitalent.utils.MmkvUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import timber.log.Timber;

/**
 * 操作W575数据的单例类，主要用不心率数据
 * Created by Admin
 * Date 2023/2/22
 * @author Admin
 */
public class W575OperateManager {


    private static   W575OperateManager w575OperateManager;

    private static Context context;


    public static W575OperateManager getInstance(Context context1){
        context = context1;
        synchronized (W575OperateManager.class){
            if(w575OperateManager == null){
                w575OperateManager = new W575OperateManager();
            }
        }
        return w575OperateManager;
    }

    private W575OperateManager(){

    }


    /**
     * 获取历史的心率数据
     * @param day 0当天，1昨天
     */
    public void getHistoryHrData(int day){
        stringBuffer.delete(0,stringBuffer.length());
        BleOperateManager.getInstance().getDay24HourForData(day, new WriteBack24HourDataListener() {
            @Override
            public void onWriteBack(byte[] data) {
                Timber.e("----------连续心率="+"序号="+(data[0] & 0xff) +"||"+ Utils.formatBtArrayToString(data));
                analysisData(data);
            }
        });
    }


    /**
     * 处理数据，分包处理
     */

    private final StringBuffer stringBuffer = new StringBuffer();
    String dayStr;
    private void analysisData(byte[] data){
        //第一包 01 48454152545f534156455f44415441 00 fe f7 63

        //序号
        int index = data[0] & 0xff;


        if(data.length == 20 && index == 1 && data[1] == 72){
            //时间戳
            long timeLong = Utils.getIntFromBytes(data[19],data[18],data[17],data[16]);



            //转换成yyyy-MM-dd格式
            dayStr = BikeUtils.getFormatDate(timeLong*1000L ,"yyyy-MM-dd");
            Timber.e("-----timeLong=%s%s", timeLong,dayStr);
        }else{
            //判断是否是最后一包，最后一包序号为0x81
            if(index == 129){
                //813c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3cffffff
                //判断下是否有255，没有255就全部保存
                int groupValue = -1;
                for(int i = 1;i<data.length;i++){
                    //判断是否到FF了，到FF后可以截断
                    int tempV = data[i] & 0xff;
                    if(tempV == 255){
                        groupValue = tempV;
                    }
                    if(tempV != 255){
                        stringBuffer.append(String.format("%02x",data[i]));
                    }else{
                        //结束了
                        byte[] source = Utils.hexStringToByte(stringBuffer.toString());
                        saveHrBelt(dayStr,source);
                        break;
                    }
                }


                if(groupValue != 255){
                    //结束了
                    byte[] source = Utils.hexStringToByte(stringBuffer.toString());
                    saveHrBelt(dayStr,source);
                }

            }


            //每个包最多19个byte
            byte[] tempByte = new byte[19];
            System.arraycopy(data,1,tempByte,0,tempByte.length);
            String str = Utils.getHexString(tempByte);
            stringBuffer.append(str);
        }

    }

    List<Integer> heartList = new ArrayList<>();
    //保存心率数据
    private void saveHrBelt(String day,byte[] htArray){
        heartList.clear();
        for(byte b : htArray){
            int v = b & 0xff;

            heartList.add(v == 255 ? 0 : v);
        }

        String name = MmkvUtils.getConnDeviceName();
        String mac = MmkvUtils.getConnDeviceMac();
        //保存心率

        DBManager.getInstance().saveDateTypeDay("user_1001",mac,day, DbType.DB_TYPE_DETAIL_HR);

        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                DBManager.getInstance().saveOnDayHeartData("user_1001",name,mac,day,heartList);
            }
        },100);

        //心率保存完了发送有个表示
        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                sendBroadCastReceiver(null,null);
            }
        },1500);
    }


    /***
     * 读取设备的版本信息
     */
    public void readDeviceVersionInfo(){
        BleOperateManager.getInstance().readDeviceInfoMsg(new OnCommBackDataListener() {
            @Override
            public void onIntDataBack(int[] value) {

            }

            @Override
            public void onStrDataBack(String... value) {
                String version = value[0];
                sendBroadCastReceiver(BleConstant.BLE_SEND_DUF_VERSION_ACTION,version);
            }
        });
    }


    /**
     * 发送广播
     *
     */
    private void sendBroadCastReceiver(String action,String value){
        Intent intent = new Intent();
        intent.setAction(BleConstant.BLE_24HOUR_SYNC_COMPLETE_ACTION);
        if(action != null){
            intent.setAction(action);
        }
        if(value != null){
            intent.putExtra("comm_key",value);
        }

        context.sendBroadcast(intent);
    }
}

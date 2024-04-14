package com.bonlala.fitalent.emu;

import java.util.HashMap;

/**
 * 设备类型的常量
 * Created by Admin
 * Date 2022/12/13
 * @author Admin
 */
public class DeviceType {


    public static final String DEVICE_W560B_NAME = "w560b";
    public static final String DEVICE_W561_NAME = "w561b";
    public static final String DEVICE_W575_NAME = "w575";


    /**W560B**/
    public static final int DEVICE_W560B = 56011;

    /**心率带 561**/
    public static final int DEVICE_561 = 561;

    /**臂带575**/
    public static final int DEVICE_W575 = 575;




    public static HashMap<String,Integer> deviceGuideMap = new HashMap<>();


    public static int getDeviceGuideTypeId(int deviceId){
        String name = null;
        if(deviceId == DEVICE_W560B){
            name = DEVICE_W560B_NAME;
        }
        if(deviceId == DEVICE_561){
            name = DEVICE_W561_NAME;
        }
        if(deviceId == DEVICE_W575){
            name = DEVICE_W575_NAME;
        }
        return deviceGuideMap.get(name) == null ? deviceId : deviceGuideMap.get(name);
    }
}

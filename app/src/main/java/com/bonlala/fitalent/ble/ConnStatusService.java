package com.bonlala.fitalent.ble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.se.omapi.SEService;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import com.blala.blalable.BleConstant;
import com.blala.blalable.BleOperateManager;
import com.blala.blalable.blebean.CommBleSetBean;
import com.blala.blalable.listener.BleConnStatusListener;
import com.blala.blalable.listener.ConnStatusListener;
import com.blala.blalable.listener.WriteBackDataListener;
import com.bonlala.fitalent.BaseApplication;
import com.bonlala.fitalent.HomeActivity;
import com.bonlala.fitalent.R;
import com.bonlala.fitalent.db.DBManager;
import com.bonlala.fitalent.db.model.DeviceSetModel;
import com.bonlala.fitalent.emu.ConnStatus;
import com.bonlala.fitalent.emu.DeviceType;
import com.bonlala.fitalent.utils.BikeUtils;
import com.bonlala.fitalent.utils.MmkvUtils;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.tencent.mmkv.MMKV;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import timber.log.Timber;

/**
 * Created by Admin
 * Date 2022/8/15
 *
 * @author Admin
 */
public class ConnStatusService extends Service {

    private static final String TAG = "ConnStatusService";

    public IBinder iBinder = new ConnBinder();


    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x08) {
                BaseApplication.getInstance().setConnStatus(ConnStatus.CONNECTED);

                sendActionBroad(BleConstant.BLE_24HOUR_SYNC_COMPLETE_ACTION);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleConstant.BLE_CONNECTED_ACTION);
        intentFilter.addAction(BleConstant.BLE_DIS_CONNECT_ACTION);
        intentFilter.addAction(BleConstant.COMM_BROADCAST_ACTION);
        intentFilter.addAction(BleConstant.BLE_COMPLETE_EXERCISE_ACTION);

        intentFilter.addAction(BleConstant.BLE_SOURCE_DIS_CONNECTION_ACTION);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(broadcastReceiver, intentFilter, RECEIVER_NOT_EXPORTED);
        }else{
            registerReceiver(broadcastReceiver, intentFilter);
        }

        regeditBackService();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }


    public class ConnBinder extends Binder {
        public ConnStatusService getService() {
            return ConnStatusService.this;
        }
    }


    //启动前台服务
    private void regeditBackService() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelID = "com.bonlala.fitalent";
                String channelName = "fitalent";
                NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.createNotificationChannel(channel);
                Notification.Builder builder = new Notification.Builder(this);

                //Notification builder = new Notification();

//            builder.setSmallIcon(R.drawable.ic_noti_s);
                builder.setSmallIcon(R.drawable.ic_notify_small);
                builder.setContentText(getResources().getString(R.string.string_notify_title_content));
                builder.setContentTitle(getResources().getString(R.string.string_notify_title_title));
                //创建通知时指定channelID
                builder.setChannelId(channelID);
                Notification notification = builder.build();

                // 通知行为（点击后能进入应用界面）
                Intent intent = new Intent(this, HomeActivity.class);
//                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent pendingIntent ;//PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                pendingIntent = PendingIntent.getBroadcast(this, 0, intent, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S? PendingIntent.FLAG_MUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);


                builder.setContentIntent(pendingIntent);


                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(11, notification);
                startForeground(11, notification);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                    //Notification.Builder builder = new Notification.Builder(this,11);
                    builder.setSmallIcon(R.drawable.ic_notify_small);
                    builder.setContentText(getResources().getString(R.string.string_notify_title_content));
                    builder.setContentTitle(getResources().getString(R.string.string_notify_title_title));
                    // 设置通知的点击行为：自动取消/跳转等
                    builder.setAutoCancel(false);
                    startForeground(11, builder.build());
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //是否扫描到了
    private boolean isScanDevice = false;

    /**
     * 重新连接设备，扫描连接
     **/
    public void autoConnDevice(String mac, boolean isScanClass) {
        Timber.e("-----auto连接=" + mac);

        BleOperateManager.getInstance().scanBleDevice(new SearchResponse() {
            @Override
            public void onSearchStarted() {
                Timber.e("----onSearchStarted--");
                isScanDevice = false;
                BaseApplication.getInstance().setConnStatus(ConnStatus.CONNECTING);
                sendActionBroad(BleConstant.BLE_CONNECTED_ACTION, "");
            }

            @Override
            public void onDeviceFounded(SearchResult searchResult) {
                String bleName = searchResult.getName();
                if (TextUtils.isEmpty(bleName) || bleName.equals("NULL"))
                    return;
                if (searchResult.getAddress().equals(mac)) {
                    BleOperateManager.getInstance().stopScanDevice();
                    Timber.e("-------扫描到了，开始连接=" + mac);
                    isScanDevice = true;
                    connDevice(bleName, mac);
                    return;
                }
            }

            @Override
            public void onSearchStopped() {
                Timber.e("----onSearchStopped--");
                if (!isScanDevice) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendActionBroad(BleConstant.BLE_SCAN_COMPLETE_ACTION, "0");
                        }
                    }, 3 * 1000);

                }
            }

            @Override
            public void onSearchCanceled() {
                Timber.e("----onSearchCanceled--");


            }
        }, isScanClass, 20 * 1000, 1);
    }


    private void setConnListener() {
        BleOperateManager.getInstance().setBleConnStatusListener(new BleConnStatusListener() {
            @Override
            public void onConnectStatusChanged(String mac, int status) {
                Timber.e("------连接状态=" + mac + " status=" + status + " " + Constants.STATUS_DISCONNECTED);
                // Constants.STATUS_DISCONNECTED
                //连接失败
                if (status == Constants.STATUS_DISCONNECTED) {
                    handler.removeMessages(0x08);
                    BaseApplication.getInstance().setConnStatus(ConnStatus.NOT_CONNECTED);
                    sendActionBroad(BleConstant.BLE_CONNECTED_ACTION, "");
//                    BleOperateManager.getInstance().disConnYakDevice();
                    new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            autoConnDevice(mac,true);
                        }
                    }, 2 * 1000);

                }

            }
        });
    }

    public void connDeviceBack(String bleName, String mac, BleConnStatusListener bleConnStatusListener) {
        setConnListener();
        BleOperateManager.getInstance().connYakDevice(bleName, mac, new ConnStatusListener() {
            @Override
            public void connStatus(int status) {

            }

            @Override
            public void setNoticeStatus(int code) {

                Timber.e("-------连接成功=" + code);
                //连接成功
                BaseApplication.getInstance().setConnStatus(ConnStatus.CONNECTED);
                BaseApplication.getInstance().setConnBleName(bleName);
                MmkvUtils.saveConnDeviceMac(mac);
                MmkvUtils.saveConnDeviceName(bleName);
                if (bleConnStatusListener != null)
                    bleConnStatusListener.onConnectStatusChanged(mac, code);
                //判断是否是心率带，心率带直接连接成功
                int deviceType = BaseApplication.getInstance().getUserDeviceType(bleName);
                if (deviceType == DeviceType.DEVICE_561) {
                    sendActionBroad(BleConstant.BLE_CONNECTED_ACTION, "");
                    BleOperateManager.getInstance().setClearListener();
                    return;
                }
                //W575臂带，臂带只有心率
                if (deviceType == DeviceType.DEVICE_W575) {
                    BleOperateManager.getInstance().syncDeviceTime(new WriteBackDataListener() {
                        @Override
                        public void backWriteData(byte[] data) {

                        }
                    });
                    sendActionBroad(BleConstant.BLE_CONNECTED_ACTION, "");
                    //读取电量
                    W575OperateManager.getInstance(ConnStatusService.this).readDeviceVersionInfo();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            W575OperateManager.getInstance(ConnStatusService.this).getHistoryHrData(0);
                        }
                    }, 1000);
                    return;
                }

                //同步时间
                BleOperateManager.getInstance().syncDeviceTime(writeBackDataListener);

            }
        });
    }

    //连接
    public void connDevice(String name, String bleMac) {
        setConnListener();
        BleOperateManager.getInstance().connYakDevice(name, bleMac, new ConnStatusListener() {
            @Override
            public void connStatus(int status) {

            }

            @Override
            public void setNoticeStatus(int code) {

                Timber.e("-------连接成功=" + code);
                //连接成功
                BaseApplication.getInstance().setConnStatus(ConnStatus.CONNECTED);
                BaseApplication.getInstance().setConnBleName(name);
                MmkvUtils.saveConnDeviceMac(bleMac);
                MmkvUtils.saveConnDeviceName(name);

                //判断是否是心率带，心率带直接连接成功
                int deviceType = BaseApplication.getInstance().getUserDeviceType(name);
                Timber.e("---deviceType=" + deviceType);
                if (deviceType == DeviceType.DEVICE_561) {
                    sendActionBroad(BleConstant.BLE_CONNECTED_ACTION, "");
                    BleOperateManager.getInstance().setClearListener();
                    return;
                }
                //W575臂带，臂带只有心率
                if (deviceType == DeviceType.DEVICE_W575) {
                    BleOperateManager.getInstance().syncDeviceTime(new WriteBackDataListener() {
                        @Override
                        public void backWriteData(byte[] data) {

                        }
                    });

                    sendActionBroad(BleConstant.BLE_CONNECTED_ACTION, "");
                    //读取电量
                    W575OperateManager.getInstance(ConnStatusService.this).readDeviceVersionInfo();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            W575OperateManager.getInstance(ConnStatusService.this).getHistoryHrData(0);
                        }
                    }, 1000);

                    return;
                }


                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //同步时间
                        BleOperateManager.getInstance().syncDeviceTime(writeBackDataListener);
                    }
                }, 1000);

            }
        });
    }


    private final WriteBackDataListener writeBackDataListener = new WriteBackDataListener() {
        @Override
        public void backWriteData(byte[] data) {

            //设备回复: 02 FF 30 00
            //同步时间返回
            if (data.length == 4 && data[0] == 2 && (data[1] & 0xff) == 255 && (data[2] & 0xff) == 48) {


                handler.sendEmptyMessageDelayed(0x08, 15 * 1000);
                CommBleSetBean commBleSetBean = new CommBleSetBean();
                String mac = MmkvUtils.getConnDeviceMac();
                if (!TextUtils.isEmpty(mac)) {
                    DeviceSetModel deviceSetModel = DBManager.getInstance().getDeviceSetModel("user_1001", mac);
                    if (deviceSetModel != null) {
                        commBleSetBean.setIs24Heart(deviceSetModel.isIs24Heart() ? 0 : 1);
                        commBleSetBean.setMetric(deviceSetModel.getIsKmUnit());
                    }
                }


                commBleSetBean.setTimeType(DateFormat.is24HourFormat(ConnStatusService.this) ? 1 : 0);
                commBleSetBean.setLanguage(BaseApplication.getInstance().getIsChinese() ? 1 : 0);
                BleOperateManager.getInstance().setCommonSetting(commBleSetBean, writeBackDataListener);

                sendActionBroad(BleConstant.BLE_CONNECTED_ACTION, "");
                BleOperateManager.getInstance().setClearListener();

                DataOperateManager.getInstance(ConnStatusService.this).setMeasureDataSave(BleOperateManager.getInstance());
                DataOperateManager.getInstance(ConnStatusService.this).readAllDataSet(BleOperateManager.getInstance(), false);
            }
        }
    };


    private void sendActionBroad(String action, String... params) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("ble_key", params);
        sendBroadcast(intent);
    }


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;

            //蓝牙开关状态action
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_NAME,0);
                Log.e(TAG,"---------蓝牙状态="+state);
                if(state == BluetoothAdapter.STATE_ON){ //打开

                }
                if(state == BluetoothAdapter.STATE_OFF){    //关闭
                    BaseApplication.getInstance().setConnStatus(ConnStatus.NOT_CONNECTED);
                    sendActionBroad(BleConstant.BLE_CONNECTED_ACTION, "");
                }
            }
            
            
            
            
            
            if (action.equals(BleConstant.BLE_SOURCE_DIS_CONNECTION_ACTION)) {

                Timber.e("---------收到了断开连接");
                String saveMac = MmkvUtils.getConnDeviceMac();
                Timber.e("----------锻炼联了=");
                BleOperateManager.getInstance().disConnNotRemoveMac();
                if (BikeUtils.isEmpty(saveMac)) {
                    return;
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        autoConnDevice(saveMac, true);
                    }
                }, 2 * 1000);

            }

            //配对
            if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                // abortBroadcast();
            }


            //连接断开
            if (action.equals(BleConstant.BLE_DIS_CONNECT_ACTION)) {
                //判断是否主动断开，主动断开无Mac地址
                String saveMac = MmkvUtils.getConnDeviceMac();
                Timber.e("----------锻炼联了=");
                if (BikeUtils.isEmpty(saveMac)) {
                    return;
                }
//                autoConnDevice(saveMac,true);
            }

            if (action.equals(BleConstant.COMM_BROADCAST_ACTION)) {
                int[] valueArray = intent.getIntArrayExtra(BleConstant.COMM_BROADCAST_KEY);
                if (valueArray[0] == BleConstant.MEASURE_COMPLETE_VALUE) {
                    DataOperateManager.getInstance(ConnStatusService.this).setMeasureDataSave(BleOperateManager.getInstance());
                }
            }

            //锻炼结束了，获取一次锻炼数据
            if (action.equals(BleConstant.BLE_COMPLETE_EXERCISE_ACTION)) {
                DataOperateManager.getInstance(ConnStatusService.this).getExerciseData();
            }
        }
    };
}

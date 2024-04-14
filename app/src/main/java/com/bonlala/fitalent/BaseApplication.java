package com.bonlala.fitalent;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

import com.amap.api.maps.MapsInitializer;
import com.blala.blalable.BleApplication;
import com.blala.blalable.BleOperateManager;
import com.bonlala.action.DebugLoggerTree;
import com.bonlala.fitalent.ble.ConnStatusService;
import com.bonlala.fitalent.emu.ConnStatus;
import com.bonlala.fitalent.emu.DeviceType;
import com.bonlala.fitalent.http.RequestHandler;
import com.bonlala.fitalent.http.RequestServer;
import com.bonlala.fitalent.service.AlertService;
import com.bonlala.fitalent.utils.BikeUtils;
import com.bonlala.fitalent.utils.LanguageUtils;
import com.bonlala.fitalent.utils.MmkvUtils;
import com.bonlala.sport.SportAmapService;
import com.bonlala.sport.db.SportDbDataBase;
import com.hjq.http.EasyConfig;
import com.hjq.http.config.IRequestInterceptor;
import com.hjq.http.model.HttpHeaders;
import com.hjq.http.model.HttpParams;
import com.hjq.http.request.HttpRequest;
import com.hjq.toast.ToastUtils;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mmkv.MMKV;

import org.litepal.LitePal;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import okhttp3.OkHttpClient;
import timber.log.Timber;

/**
 * Created by Admin
 * Date 2022/8/5
 * @author Admin
 */
public class BaseApplication extends BleApplication {


    private static BaseApplication baseApplication;

    //连接状态的服务
    private static ConnStatusService connStatusService;


    private static SportAmapService sportAmapService;


    /**连接的名称**/
    private String connBleName;

    /**连接状态枚举**/
    private ConnStatus connStatus = ConnStatus.NOT_CONNECTED;

    /**数据同步状态的枚举**/


    /**是否是中文状态**/
    private boolean isChinese = false;


    /**是否需要重新加载页面，用于连接成功后重新加载页面**/
    private boolean isNeedReloadPage = false;


    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this;
        initApps();
    }


    public  SportDbDataBase getSportDbDataBase(){
        return SportDbDataBase.getSportDbDataBase(this);
    }


    private void initApps(){
        Timber.plant(new DebugLoggerTree());
        LitePal.initialize(this);
//        SQLiteDatabase sqLiteDatabase = LitePal.getDatabase();
//        Timber.e("---------sql="+sqLiteDatabase.getPath());
        isChinese = LanguageUtils.isChinese();
        //腾讯mmkv
        MMKV.initialize(this);
        MmkvUtils.initMkv();
        ToastUtils.init(this);
        initNet();

        // 设置全局的 Header 构建器
//        SmartRefreshLayout.setDefaultRefreshHeaderCreator((cx, layout) ->
//                new ClassicsHeader(baseApplication).setc.setColorSchemeColors(ContextCompat.getColor(baseApplication, R.color.white)));
        // 设置全局的 Footer 构建器
//        SmartRefreshLayout.setDefaultRefreshFooterCreator((cx, layout) -> new SmartBallPulseFooter(application));
        // 设置全局初始化器
        SmartRefreshLayout.setDefaultRefreshInitializer((cx, layout) -> {
            // 刷新头部是否跟随内容偏移
            layout.setEnableHeaderTranslationContent(true)
                    // 刷新尾部是否跟随内容偏移
                    .setEnableFooterTranslationContent(true)
                    // 加载更多是否跟随内容偏移
                    .setEnableFooterFollowWhenNoMoreData(false)
                    // 内容不满一页时是否可以上拉加载更多
                    .setEnableLoadMoreWhenContentNotFull(false)
                    // 仿苹果越界效果开关
                    .setEnableOverScrollDrag(false);
        });

        //启动监听连接状态的服务
        bindConnStatusService();

        boolean isPrivacy = MmkvUtils.getPrivacy();
        if(isPrivacy){
            CrashReport.initCrashReport(this, "396d1bb894", false);
            MapsInitializer.updatePrivacyShow(this,true,true);
            MapsInitializer.updatePrivacyAgree(this,true);
        }
    }


    public void setAgree(){
        CrashReport.initCrashReport(this, "396d1bb894", false);
        MapsInitializer.updatePrivacyShow(this,true,true);
        MapsInitializer.updatePrivacyAgree(this,true);
    }


    //获取连接状态
    public ConnStatus getConnStatus(){
        return connStatus;
    }

    //设置连接状态
    public void setConnStatus(ConnStatus connStatus){
        this.connStatus = connStatus;
    }



    public SportAmapService getSportAmapService(){
        return sportAmapService;
    }


    public static BaseApplication getInstance(){
        return baseApplication;
    }


    public  BleOperateManager getBleOperate(){
        return BleOperateManager.getInstance();
    }


    public ConnStatusService getConnStatusService(){
        return connStatusService;
    }


    private void bindConnStatusService(){
        Intent intent = new Intent(this, ConnStatusService.class);
        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);


        Intent appIntent = new Intent(this, AlertService.class);
        bindService(appIntent,appServiceConnection,Context.BIND_AUTO_CREATE);

        Intent sportIntent = new Intent(this, SportAmapService.class);
        bindService(sportIntent,sportServiceConnection,Context.BIND_AUTO_CREATE);
    }


    private final ServiceConnection sportServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                sportAmapService = ((SportAmapService.SportBinder)iBinder).getService();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            sportAmapService = null;
        }
    };


    private final ServiceConnection appServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                connStatusService = ((ConnStatusService.ConnBinder) iBinder).getService();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            connStatusService = null;
        }
    };

    private void initNet(){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();
        //是否是中文
        boolean isChinese = LanguageUtils.isChinese();
        EasyConfig.with(okHttpClient)
                // 是否打印日志
                .setLogEnabled(BuildConfig.DEBUG)
                // 设置服务器配置
                .setServer(new RequestServer())
                // 设置请求处理策略
                .setHandler(new RequestHandler(this))
                // 设置请求重试次数
                .setRetryCount(3)
                // 添加全局请求参数
                //.addParam("token", "6666666")
                // 添加全局请求头
                //.addHeader("time", "20191030")
                .setInterceptor(new IRequestInterceptor() {
                    @Override
                    public void interceptArguments(@NonNull HttpRequest<?> httpRequest, @NonNull HttpParams params, @NonNull HttpHeaders headers) {
                        headers.put("Accept-Language", LanguageUtils.isChinese() ? "zh-CN" : "en");
                    }
                })
                // 启用配置
                .into();
    }

    public String getConnBleName() {
        return connBleName;
    }

    public void setConnBleName(String connBleName) {
        this.connBleName = connBleName;
    }

    public boolean getIsChinese(){
        return LanguageUtils.isChinese();
    }


    /**
     * 根据连接过的设备名称判断类型
     * @param bleName 蓝牙名称
     * @return 设备类型
     */
    public int getUserDeviceType(String bleName){
        if(BikeUtils.isEmpty(bleName)){
            return 0;
        }

        //W560B
        if(bleName.toLowerCase(Locale.CHINA).contains(DeviceType.DEVICE_W560B_NAME)){
            return DeviceType.DEVICE_W560B;
        }
        //W575
        if(bleName.toLowerCase(Locale.CHINA).contains(DeviceType.DEVICE_W575_NAME)){
            return DeviceType.DEVICE_W575;
        }

        if(bleName.toLowerCase(Locale.ROOT).contains(DeviceType.DEVICE_W561_NAME)){
            return DeviceType.DEVICE_561;
        }

        return DeviceType.DEVICE_561;
    }


    public boolean isNeedReloadPage() {
        return isNeedReloadPage;
    }

    public void setNeedReloadPage(boolean needReloadPage) {
        isNeedReloadPage = needReloadPage;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Timber.e("----------onTerminate");
    }
}

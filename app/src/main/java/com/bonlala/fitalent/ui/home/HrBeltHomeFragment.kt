package com.bonlala.fitalent.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Menu
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blala.blalable.BleConstant
import com.blala.blalable.BleOperateManager
import com.blala.blalable.listener.WriteBackDataListener
import com.bonlala.action.TitleBarFragment
import com.bonlala.fitalent.BaseApplication
import com.bonlala.fitalent.HomeActivity
import com.bonlala.fitalent.MainActivity
import com.bonlala.fitalent.R
import com.bonlala.fitalent.activity.RecordHistoryActivity
import com.bonlala.fitalent.activity.history.ExerciseRecordActivity
import com.bonlala.fitalent.adapter.HomeUiAdapter
import com.bonlala.fitalent.bean.HomeHeartBean
import com.bonlala.fitalent.bean.HomeSourceBean
import com.bonlala.fitalent.ble.DataOperateManager
import com.bonlala.fitalent.ble.W575OperateManager
import com.bonlala.fitalent.db.DBManager
import com.bonlala.fitalent.db.model.DataRecordModel
import com.bonlala.fitalent.db.model.ExerciseModel
import com.bonlala.fitalent.dialog.HrBletDisconnDialog
import com.bonlala.fitalent.emu.*
import com.bonlala.fitalent.listeners.OnHrBeltSportTimeListener
import com.bonlala.fitalent.listeners.OnItemClickListener
import com.bonlala.fitalent.listeners.OnStartOrEndListener
import com.bonlala.fitalent.utils.BikeUtils
import com.bonlala.fitalent.utils.LanguageUtils
import com.bonlala.fitalent.utils.MmkvUtils
import com.bonlala.fitalent.view.HomeDeviceStatusView
import com.bonlala.fitalent.view.HrBeltRealTimeView
import com.bonlala.fitalent.viewmodel.HrBeltViewModel
import com.google.gson.Gson
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_hr_belt_home_layout.*
import kotlinx.android.synthetic.main.fragment_hr_belt_home_layout.homeRefreshHeader
import kotlinx.android.synthetic.main.item_home_wall_real_hr_layout.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


/**
 * 心率带W561B设备
 * Created by Admin
 *Date 2022/12/13
 */
class HrBeltHomeFragment : TitleBarFragment<HomeActivity>(),OnRefreshListener{


    private val viewModel by viewModels<HrBeltViewModel>()

    private val gson = Gson()

    fun  getInstance() : HrBeltHomeFragment{
        return HrBeltHomeFragment()
    }


    private var hrBeltRealTimeView : HrBeltRealTimeView ?= null
    private var homeDeviceStatusView : HomeDeviceStatusView ?= null

    //实时心率的临时集合，用于计算最大最小心率
    private var realTimeHrList = mutableListOf<Int>()


    //是否开始运动了
    private var isStartSport = false
    //运动开始记录的心率数据
    private var recordExerciseHrList = mutableListOf<Int>()

    //数据源
    private var sourceList = mutableListOf<HomeSourceBean>()

    private var homeRecyclerView : RecyclerView?= null
    private var homeUiAdapter : HomeUiAdapter?= null


    private var hrSportStartTime = 0L
    private var hrSportTime = 0


    //刷新
    private var homeRefreshLayout : SmartRefreshLayout ?= null
    private var homeRefreshHeader : ClassicsHeader?= null


    private var mHandler: Handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            if(msg.what == 0x00){
               getDataForDb()
            }
            if(msg.what == 0x08){
                DBManager.getInstance().deleteHrExerciseData("user_1001",MmkvUtils.getConnDeviceMac())
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        //强制退出保存一下状态
        MmkvUtils.saveHrBeltFocusExit(true)
        super.onSaveInstanceState(outState)
        //屏幕常亮
        attachActivity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    }



    override fun getLayoutId(): Int {
        return R.layout.fragment_hr_belt_home_layout
    }

    override fun initView() {

        //注册广播
        val intentFilter = IntentFilter()
        intentFilter.addAction(BleConstant.BLE_CONNECTED_ACTION)
        intentFilter.addAction(BleConstant.BLE_DIS_CONNECT_ACTION)
        intentFilter.addAction(BleConstant.COMM_BROADCAST_ACTION)
        intentFilter.addAction(BleConstant.BLE_SCAN_COMPLETE_ACTION)
        intentFilter.addAction(BleConstant.BLE_24HOUR_SYNC_COMPLETE_ACTION)
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.registerReceiver(broadcastReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            activity?.registerReceiver(broadcastReceiver, intentFilter)
        }
        homeRefreshHeader = findViewById(R.id.homeRefreshHeader)
        homeRefreshLayout = findViewById(R.id.homeRefreshLayout)
        homeRecyclerView = findViewById(R.id.homeRecyclerView)
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

        homeDeviceStatusView = findViewById(R.id.homeDeviceStatusView)
        hrBeltRealTimeView = findViewById(R.id.hrBeltRealTimeView)
        //取消动画
        homeRecyclerView?.itemAnimator = null
        homeRecyclerView?.layoutManager = linearLayoutManager
        homeUiAdapter = HomeUiAdapter(activity,sourceList)
        homeRecyclerView?.adapter = homeUiAdapter

        homeRefreshLayout?.setOnRefreshListener(this)

        //设置刷新头部
        val isChinese = LanguageUtils.isChinese()
        val sdf = SimpleDateFormat(if(isChinese) "MM-dd HH:mm" else "MMM dd HH:mm",if(isChinese) Locale.CHINESE else Locale.ENGLISH)
        homeRefreshHeader?.setTimeFormat(sdf)


        //添加功能，进入次fragment已经表明用户绑定过设备，有绑定的记录，判断类型即可
        val deviceType = DBManager.getBindDeviceType()
        Timber.e("------deviceType="+deviceType)
        if(deviceType == DeviceType.DEVICE_W575){
            sourceList.add(HomeSourceBean(HomeDateType.HOME_TYPE_DETAIL_HR,null))
        }

        sourceList.add(HomeSourceBean(HomeDateType.HOME_HR_WALL_SPORT_RECORD,null))
        homeUiAdapter?.notifyDataSetChanged()


        homeUiAdapter?.setOnItemClickListener {
            Timber.e("------type="+it)
            if(it == HomeDateType.HOME_TYPE_DETAIL_HR){    //心率记录

                val intent = Intent(attachActivity,RecordHistoryActivity::class.java)
                intent.putExtra("home_type", HomeDateType.HOME_TYPE_DETAIL_HR)
                intent.putExtra("is_w575",true)
                startActivity(intent)

            }
            if(it == HomeDateType.HOME_HR_WALL_SPORT_RECORD){
                startActivity(Intent(attachActivity,ExerciseRecordActivity::class.java))
            }

        }



        homeDeviceClick()

       //开始或结束的状态
        hrBeltRealTimeView?.setOnStartEndListener(object : OnStartOrEndListener{
            //开始
            override fun startOrEndStatus(isStart: Boolean) {

                Timber.e("------开始运动")
                isStartSport = true
                recordExerciseHrList.clear()
                realTimeHrList.clear()
                //将柱状图的数据清0
                hrBeltRealTimeView?.setClearRealHrBarChartView()
                //将运动的累计卡路里清0
                hrBeltRealTimeView?.setInitTotalKcal()
                //隐藏底部菜单
                attachActivity.setVisibilityBottomMenu(false)
                //设置是否在运动模式中
                attachActivity.setImgStatus(true)
                //将锻炼记录隐藏掉
                homeRecyclerView?.visibility = View.GONE
            }
            //结束
            override fun endSportStatus(startTime: Long, sportTime: Int) {
                endSport(startTime,sportTime)
            }

        })


        //实时的运动回调，用于退出app时使用
        hrBeltRealTimeView?.setOnHrBeltSportListener { startTime, sportTime ->
            hrSportStartTime = startTime
            hrSportTime = sportTime
        }




        getHeartBtn.setOnClickListener {
            W575OperateManager.getInstance(attachActivity).getHistoryHrData(0)
        }

        syncTimeBtn.setOnClickListener {
            BleOperateManager.getInstance().syncDeviceTime {

            }
        }

        getTimeBtn.setOnClickListener {
            BleOperateManager.getInstance().writeCommonByte(BleConstant().w575Time,object : WriteBackDataListener{
                override fun backWriteData(data: ByteArray?) {

                }

            })
        }


        viewModel.focusExitData.observe(this,observable)


        dealFocusData()
    }


    //强制退出
    private val observable : Observer<ExerciseModel> =
        Observer<ExerciseModel> { t ->
            if(t != null){
                hrBeltRealTimeView?.showAlertIsSave(true,t.avgHr,t.hourMinute,t.kcal,t
                ) { position ->
                    Timber.e("-----position="+position)
                    if (position == 0x01) {
                        //删除
                        DBManager.getInstance().deleteHrExerciseData(t.userId,t.deviceMac)
                    } else {
                        mHandler.sendEmptyMessageDelayed(0x00, 500)
                        mHandler.postAtTime(Runnable {
                            DBManager.getInstance().deleteHrExerciseData("user_1001",MmkvUtils.getConnDeviceMac())
                        },1000)
                    }
                }

            }
        }


    //结束运动
    private fun endSport(startTime: Long, sportTime: Int){
        val hour = sportTime / 3600
        val minute = (sportTime - hour * 3600) / 60
        val seconds = sportTime % 60

        isStartSport = false
        //计算数据，保存到数据库
        attachActivity.setVisibilityBottomMenu(true)
        //设置是否在运动模式中
        attachActivity.setImgStatus(false)
        //将锻炼记录隐藏掉
        homeRecyclerView?.visibility = View.VISIBLE
        val recordModel = DataRecordModel()
        recordModel.dayStr = BikeUtils.getCurrDate()
        val status = hrBeltRealTimeView?.getCountDownStatus()
        recordModel.dataType = DbType.DB_TYPE_EXERCISE
        recordModel.deviceMac = MmkvUtils.getConnDeviceMac()
        recordModel.deviceName = MmkvUtils.getConnDeviceName()
        recordModel.saveLongTime = System.currentTimeMillis()


        //计算最大最小，平均心率，计算平均心率时去掉0
        val tempAvgList = mutableListOf<Int>()
        recordExerciseHrList.forEach {
            if(it!=0){
                tempAvgList.add(it)
            }
        }


        val avgValue = tempAvgList.average().toInt()

        val exerciseModel = ExerciseModel()
        val allKcal = hrBeltRealTimeView?.getSportTotalKcal()?.toInt()
        if (allKcal != null) {
            exerciseModel.kcal = allKcal
        }
        exerciseModel.userId = "user_1001"
        exerciseModel.endTime = BikeUtils.getFormatDate(System.currentTimeMillis(),"yyyy-MM-dd HH:mm:ss")
        exerciseModel.avgHr = avgValue
        exerciseModel.dayStr = BikeUtils.getCurrDate()
        exerciseModel.hrArray = Gson().toJson(recordExerciseHrList)
        exerciseModel.startTime = BikeUtils.getFormatDate(startTime,"yyyy-MM-dd HH:mm:ss")
        exerciseModel.deviceMac = MmkvUtils.getConnDeviceMac()
        exerciseModel.sportHour = hour
        exerciseModel.sportMinute = minute
        exerciseModel.sportSecond = seconds
        //运动类型
        exerciseModel.type = W560BExerciseType.getHrBeltSportType(status)


        if (allKcal != null) {
            hrBeltRealTimeView?.showAlertIsSave(false,avgValue,exerciseModel.hourMinute,allKcal,exerciseModel
            ) { mHandler.sendEmptyMessageDelayed(0x00, 500) }
        }

    }



    private fun dealFocusData(){

        //判断是否连接过设备
        //判断是否连接过
        val bindMac = MmkvUtils.getConnDeviceMac()
        if(!BikeUtils.isEmpty(bindMac) && !isStartSport){
            viewModel.getFocusExitData(bindMac)
        }
    }


    //连接设备的点击
    private fun homeDeviceClick(){

        homeDeviceStatusView?.setOnClickListener {

            val connMac = MmkvUtils.getConnDeviceMac()

            if(BikeUtils.isEmpty(connMac)){
                startActivity(MainActivity::class.java)
                return@setOnClickListener
            }
        }

        homeDeviceStatusView?.setOnStatusViewClick {
            val userMac = DBManager.getBindMac()
            if(BikeUtils.isEmpty(userMac)){
                startActivity(MainActivity::class.java)
                return@setOnStatusViewClick
            }
            if(BaseApplication.getInstance().connStatus == ConnStatus.CONNECTED ){

                return@setOnStatusViewClick
            }

            BaseApplication.getInstance().connStatus = ConnStatus.CONNECTING
            attachActivity.autoConnDevice()
            homeDeviceStatusView?.setHomeConnStatus(ConnStatus.CONNECTING)
        }

    }

    private var homeHrB : HomeHeartBean ?=null

    override fun initData() {

        Timber.e("-------initData")

        showDeviceStatus()

        showEmptyData()
        hrBeltRealTimeView?.setClearRealHrBarChartView()
        realTimeHrList.clear()
        BaseApplication.getInstance().bleOperate.setRealTimeDataListener { ht, step, kcal, distance ->
            Timber.e("----实时心率="+ht)
            showRealHr(ht)
        }

        viewModel.todayHrBeltExercise.observe(this){
            showEmptyData()
            if(it == null){
                showEmptyData()
            }else{
                val type = DBManager.getBindDeviceType()
                if(type == DeviceType.DEVICE_W575){
                    if(sourceList.size >1){
                        sourceList[1].dataSource = Gson().toJson(it)
                        homeUiAdapter?.notifyDataSetChanged()
                    }

                }
                if(type == DeviceType.DEVICE_561){
                    sourceList[0].dataSource = Gson().toJson(it)
                    homeUiAdapter?.notifyDataSetChanged()
                }

            }
        }
        val bindType = DBManager.getBindDeviceType()
        if(bindType == DeviceType.DEVICE_W575){
            //心率 详细
            viewModel.detailHr.observe(viewLifecycleOwner){
               // Timber.e("-------心率="+gson.toJson(it))
                if(homeHrB == null){
                    homeHrB = HomeHeartBean()
                }
                homeHrB?.dayStr = it.dayStr
                // homeHrB?.singleHr = it.singleHr
                // homeHrB?.singleHrTime = it.singleHrTime
                homeHrB?.hrList = it.heartList
                // homeHrB?.detailHrTime = it.detailHrTime
                sourceList[0].dataSource = gson.toJson(homeHrB)
                homeUiAdapter?.notifyItemChanged(0)
            }
        }

    }


    //计算实时的心率
    private fun showRealHr(ht : Int){
        if(ht<30){
            hrBeltRealTimeView?.setZeroRealChart()
            if(isStartSport){
                recordExerciseHrList.add(0)
            }
            return
        }
        realTimeHrList.add(ht)
//        Timber.e("-----isStartSport="+isStartSport)
        if(isStartSport){
            recordExerciseHrList.add(ht)
        }

        //最大心率
        val maxValue = realTimeHrList.maxOf { i : Int -> i }
        //最小值
        val minValue = realTimeHrList.minOf { i : Int -> i }
        val avgValue = realTimeHrList.average().toInt()
        hrBeltRealTimeView?.setRealTimeHrValue(ht,maxValue,minValue,avgValue,isStartSport)
    }

    //展示空数据，可以作为初始化使用
    private fun showEmptyData(){
        sourceList.clear()

        homeRefreshLayout?.finishRefresh()
        homeRefreshLayout?.setEnableRefresh(false)

        //连续心率
        val bindType = DBManager.getBindDeviceType()
        Timber.e("-------showEmptyType="+bindType)
        if(bindType == DeviceType.DEVICE_W575){
            sourceList.add(HomeSourceBean(HomeDateType.HOME_TYPE_DETAIL_HR,null))
        }

        //运动记录
        sourceList.add(HomeSourceBean(HomeDateType.HOME_HR_WALL_SPORT_RECORD,null))
        homeUiAdapter?.notifyDataSetChanged()

        //将柱状图的数据清0
        hrBeltRealTimeView?.setClearRealHrBarChartView()
        //将运动的累计卡路里清0
        hrBeltRealTimeView?.setInitTotalKcal()
    }


    override fun onActivityResume() {
        super.onActivityResume()
        Timber.e("------onActivityResume")

    }

    override fun onFragmentResume(first: Boolean) {
        super.onFragmentResume(first)
        Timber.e("------onFragmentResume"+(activity?.isFinishing)+" "+first)
        showDeviceStatus()
    }




    //展示连接状态
    private fun showDeviceStatus(){
        //判断是否连接过

        val userMac = DBManager.getBindMac()
        if(BikeUtils.isEmpty(userMac)){
            if(hrBeltRealTimeView != null){
                hrBeltRealTimeView?.visibility = View.GONE
            }
            showEmptyData()
            return
        }

        val bindMac = MmkvUtils.getConnDeviceMac()
        if(BikeUtils.isEmpty(bindMac)){  //未连接过
            homeDeviceStatusView?.setIsConnRecord(false,"")
            hrBeltRealTimeView?.visibility = View.GONE
            showEmptyData()
            return
        }

        if(hrBeltRealTimeView != null){
            hrBeltRealTimeView?.visibility = View.VISIBLE
        }
        //设置显示的图片
        homeDeviceStatusView?.setRightImgView(DBManager.getBindDeviceType())
        getDataForDb()
        val bleName = MmkvUtils.getConnDeviceName()
        homeDeviceStatusView?.setIsConnRecord(true,bleName)
        //连接过，判断状态
        homeDeviceStatusView?.setHomeConnStatus(BaseApplication.getInstance().connStatus)

        val status = hrBeltRealTimeView?.getCountDownStatus()
        //如果未连接就不显示实时心率
        if(BaseApplication.getInstance().connStatus == ConnStatus.CONNECTED || status != CountDownStatus.DEFAULT_STATUS){
            hrBeltRealTimeView?.visibility = View.VISIBLE
            if(homeRefreshLayout != null){
                homeRefreshLayout?.setEnableRefresh(true)
            }
        }else{
            hrBeltRealTimeView?.visibility = View.GONE
            if(homeRefreshLayout != null){
                homeRefreshLayout?.setEnableRefresh(false)
            }
        }

        //W561B心率带没有刷新
        if(DBManager.getBindDeviceType() == DeviceType.DEVICE_561){
            homeRefreshLayout?.finishRefresh()
            homeRefreshLayout?.setEnableRefresh(false)
        }

    }


    override fun onResume() {
        super.onResume()
        getDataForDb()
    }

    private fun getDataForDb(){
        val mac = DBManager.getBindMac()
        if(BikeUtils.isEmpty(mac)){
            return
        }
//        viewModel.focusExitData.removeObserver(observable)
        viewModel.getTodayHrBeltExerciseData(mac,attachActivity)
        //心率 单次+详细
        viewModel.queryLastDetailHr(mac)

    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            activity?.unregisterReceiver(broadcastReceiver)
        }catch (e : Exception){
            e.printStackTrace()
        }

    }



    private val broadcastReceiver : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1?.action
            Timber.e("------心率带--action="+action)
            //连接状态
            if (action == BleConstant.BLE_CONNECTED_ACTION || action == BleConstant.BLE_DIS_CONNECT_ACTION || action == BleConstant.BLE_SCAN_COMPLETE_ACTION){

                val isConn = BaseApplication.getInstance().connStatus == ConnStatus.NOT_CONNECTED//BaseApplication.getInstance().connStatus == ConnStatus.CONNECTED || BaseApplication.getInstance().connStatus ==ConnStatus.IS_SYNC_DATA || BaseApplication.getInstance().connStatus == ConnStatus.CONNECTING
                Timber.e("----------状态="+BaseApplication.getInstance().connStatus+" "+isConn)
                if(isConn ){
                    hrBeltRealTimeView?.setZeroRealChart()
                    if(hrBeltRealTimeView?.getCountDownStatus() != CountDownStatus.DEFAULT_STATUS){
                        showReconnDialog()
                    }else{
                        closeReconnDialog()
                    }

                }

                showDeviceStatus()
            }

            //心率同步完成了
            if(action == BleConstant.BLE_24HOUR_SYNC_COMPLETE_ACTION){
                if(homeRefreshLayout != null && homeRefreshLayout?.isRefreshing == true){
                    homeRefreshLayout?.finishRefresh()
                }
                getDataForDb()
            }


            //监听任务键，保存强制退出的数据
            if(action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS){
                val reason = p1.getStringExtra("reason")
                Timber.e("-----reason="+reason+" "+(reason == "recentapps"))
                if(BikeUtils.isEmpty(reason)){
                    return
                }
                //最近任务键
                if(reason == "recentapps" || reason == "fs_gesture"){
                    saveFocusExitData()
                }
            }
        }

    }


    //强制退出app时保存运动数据
    private fun saveFocusExitData(){
        Timber.e("-----ssssss="+hrSportStartTime)
        if(isStartSport){
            if(hrSportStartTime.toInt() == 0 || hrSportTime == 0){
                return
            }
            val hour = hrSportTime / 3600
            val minute = (hrSportTime - hour * 3600) / 60
            val seconds = hrSportTime % 60

            val recordModel = DataRecordModel()
            recordModel.dayStr = BikeUtils.getCurrDate()
            val status = hrBeltRealTimeView?.getCountDownStatus()
            recordModel.dataType = DbType.DB_TYPE_EXERCISE
            recordModel.deviceMac = MmkvUtils.getConnDeviceMac()
            recordModel.deviceName = MmkvUtils.getConnDeviceName()
            recordModel.saveLongTime = System.currentTimeMillis()


            //计算最大最小，平均心率，计算平均心率时去掉0
            val tempAvgList = mutableListOf<Int>()
            recordExerciseHrList.forEach {
                if(it!=0){
                    tempAvgList.add(it)
                }
            }


            val avgValue = tempAvgList.average().toInt()

            val exerciseModel = ExerciseModel()
            val allKcal = hrBeltRealTimeView?.getSportTotalKcal()?.toInt()
            if (allKcal != null) {
                exerciseModel.kcal = allKcal
            }
            exerciseModel.userId = "user_1001"
            exerciseModel.endTime = BikeUtils.getFormatDate(System.currentTimeMillis(),"yyyy-MM-dd HH:mm:ss")
            exerciseModel.avgHr = avgValue
            exerciseModel.dayStr = BikeUtils.getCurrDate()
            exerciseModel.hrArray = Gson().toJson(recordExerciseHrList)
            exerciseModel.startTime = BikeUtils.getFormatDate(hrSportStartTime,"yyyy-MM-dd HH:mm:ss")
            exerciseModel.deviceMac = MmkvUtils.getConnDeviceMac()
            exerciseModel.sportHour = hour
            exerciseModel.sportMinute = minute
            exerciseModel.sportSecond = seconds
            //运动类型
            exerciseModel.type = W560BExerciseType.getHrBeltSportType(status)
            val str = Gson().toJson(exerciseModel)
            Timber.e("-------保存数据="+str)
            DBManager.getInstance().saveHrBeltTempExerciseData(exerciseModel.userId,exerciseModel.deviceMac,str)

        }
    }



    //重连
    private fun showReconnDialog(){
        hrBeltRealTimeView?.reconnectOrEndDialog { attachActivity.autoConnDevice() }
    }

    //关闭重连的弹窗，连接成功关闭
    private fun closeReconnDialog(){
        hrBeltRealTimeView?.closeReconnectOrEndDialog()
    }




    private var forwardCountTime = 0

    private var handlers: Handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            if(msg.what == 0x00){
                forwardCountTime++
                hrBeltTimerTv.text = BikeUtils.formatSecond(forwardCountTime)
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Timber.e("----onDestroyView----")
        viewModel.focusExitData.removeObserver(observable)
//        try {
//            attachActivity.unregisterReceiver(broadcastReceiver)
//        }catch (e : Exception){
//            e.printStackTrace()
//        }
    }

    //刷新
    override fun onRefresh(refreshLayout: RefreshLayout) {
        if(BaseApplication.getInstance().connStatus == ConnStatus.IS_SYNC_DATA){
            homeRefreshLayout?.setEnableRefresh(false)
            homeRefreshLayout?.finishRefresh()
            return
        }

        if(BaseApplication.getInstance().connStatus != ConnStatus.CONNECTED){
            homeRefreshLayout?.setEnableRefresh(false)
            homeRefreshLayout?.finishRefresh()
            return
        }
        homeRefreshLayout?.setEnableRefresh(true)
        W575OperateManager.getInstance(attachActivity).getHistoryHrData(0x00)
    }
}
package com.bonlala.fitalent

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.blala.blalable.BleConstant
import com.blala.blalable.Utils
import com.bonlala.action.ActivityManager
import com.bonlala.action.AppActivity
import com.bonlala.fitalent.activity.GuideActivity
import com.bonlala.fitalent.activity.ShowWebActivity
import com.bonlala.fitalent.adapter.ScanDeviceAdapter
import com.bonlala.fitalent.bean.BleBean
import com.bonlala.fitalent.db.DBManager
import com.bonlala.fitalent.db.model.UserInfoModel
import com.bonlala.fitalent.dialog.ConnGuideDialogView
import com.bonlala.fitalent.emu.ConnStatus
import com.bonlala.fitalent.listeners.OnItemClickListener
import com.bonlala.fitalent.utils.BikeUtils
import com.bonlala.fitalent.utils.BonlalaUtils
import com.bonlala.fitalent.utils.MmkvUtils
import com.bonlala.fitalent.viewmodel.ScanViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.hjq.permissions.XXPermissions
import com.hjq.toast.ToastUtils
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.util.*

/**搜索页面**/
class MainActivity : AppActivity() ,OnItemClickListener{

    private val viewModel by viewModels<ScanViewModel>()

    private var scanDeviceAdapter : ScanDeviceAdapter ?= null
    private var listData : MutableList<BleBean> ?= null


    //用于去重的list
    private var repeatList : MutableList<String> ?= null

    //是否正常连接中，连接中不能退出
    private var isConnStatus = false

    //搜索不到的指引url
    var scanUrl : String ?= null

    private var handlers : Handler = object :Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if(msg.what == 0x00){
                BaseApplication.getInstance().bleOperate.stopScanDevice()
            }
            if(msg.what == 0x01){   //连接超时
                connTimeOut()
            }
        }
    }


    private fun connTimeOut(){
       hideDialog()
        ToastUtils.show(resources.getString(R.string.string_conn_timeout)+" "+resources.getString(R.string.string_reconnect))
        BaseApplication.getInstance().bleOperate.stopScanDevice()
        verifyScanFun()
    }


    private fun verifyScanFun(){
        Timber.e("-------isBleEnable="+BikeUtils.isBleEnable(this))
        //判断蓝牙是否开启
        if(!BikeUtils.isBleEnable(this)){
            BikeUtils.openBletooth(this)
            return
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            XXPermissions.with(this).permission(arrayOf(Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN)).request { permissions, all ->
                //verifyScanFun()
            }

        }

        //判断权限
        val isPermission = ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if(!isPermission){
            XXPermissions.with(this).permission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)).request { permissions, all ->
                verifyScanFun()
            }
           // ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),0x00)
            return
        }

        //判断蓝牙是否打开
        val isOpenBle = BonlalaUtils.isOpenBlue(this@MainActivity)
        if(!isOpenBle){
            BonlalaUtils.openBluetooth(this)
            return
        }

        startScanDevice()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        verifyScanFun()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        verifyScanFun()
    }

    //搜索
    private fun startScanDevice(){
        listData?.clear()
        repeatList?.clear()
        startGif()
        BaseApplication.getInstance().bleOperate.scanBleDevice(object : SearchResponse{
            override fun onSearchStarted() {
                Log.e("sanc","-----开始扫描")
            }

            override fun onDeviceFounded(p0: SearchResult) {
                scanStatusTv.text = resources.getString(R.string.string_scan_ing)

                val bleName = p0.name
                if(BikeUtils.isEmpty(bleName) || bleName.equals("NULL") || BikeUtils.isEmpty(p0.address))
                    return

                val record = p0.getScanRecord()
//                Timber.e("-------record="+Utils.formatBtArrayToString(record))
                if(!bleName.lowercase(Locale.ROOT).contains("w561b")
                    && !bleName.lowercase(Locale.ROOT).contains("w560b") && !bleName.lowercase(
                        Locale.ROOT).contains("w575"))
                    return
                if(repeatList?.contains(p0.address) == true)
                    return
               repeatList?.add(p0.address)
                listData?.add(BleBean(p0.device,p0.rssi))
                listData?.sortBy {
                    Math.abs(it.rssi)
                }
                scanDeviceAdapter?.notifyDataSetChanged()

            }

            override fun onSearchStopped() {
                scanStatusTv.text = resources.getString(R.string.string_scan_complete)
                stopGif()
            }

            override fun onSearchCanceled() {
                scanStatusTv.text = resources.getString(R.string.string_scan_complete)

            }

        },1500 * 1000,1)
    }


    override fun onDestroy() {
        super.onDestroy()
        BaseApplication.getInstance().bleOperate.stopScanDevice()
    }

    override fun getLayoutId(): Int {
       return R.layout.activity_main
    }

    override fun initView() {
        scanDescTv.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        scanDescTv.setOnClickListener {
            if(scanUrl == null)
                return@setOnClickListener
            startActivity(ShowWebActivity::class.java, arrayOf("url","title"), arrayOf(scanUrl,resources.getString(R.string.string_device_cant_conn)))
        }
    }

    override fun initData() {
        repeatList = ArrayList<String>()
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        scanRecyclerView.layoutManager = linearLayoutManager
//        scanRecyclerView.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))
        listData =ArrayList<BleBean>()
        scanDeviceAdapter = ScanDeviceAdapter(this,listData)
        scanRecyclerView.adapter = scanDeviceAdapter
        scanDeviceAdapter?.setOnItemClickListener(this)


        viewModel.notScanUrl.observe(this){
            scanUrl = it
        }
        viewModel.getNotScanUrl(this)

        verifyScanFun()
    }

    override fun onIteClick(position: Int) {
        handlers.sendEmptyMessage(0x00)

        val bleMac = listData?.get(position)?.bluetoothDevice?.address
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val bleName =  listData?.get(position)?.bluetoothDevice?.name

        showDialog(resources.getString(R.string.string_conning),false)
        handlers.sendEmptyMessageDelayed(0x01,15 * 1000)
        BaseApplication.getInstance().connStatusService.connDeviceBack(bleName,bleMac
        ) { mac, status ->
            handlers.removeMessages(0x01)
            //连接成功
            BaseApplication.getInstance().connBleName = bleName
            BaseApplication.getInstance().connStatus = ConnStatus.CONNECTED
            MmkvUtils.saveConnDeviceName(bleName)
            MmkvUtils.saveConnDeviceMac(bleMac)

            //保存用户绑定的Mac
            var userInfo = DBManager.getUserInfo()
            Timber.e("-----userInfo="+(userInfo == null)+(Gson().toJson(userInfo)))
            if(userInfo == null){
                userInfo = UserInfoModel()
            }
            userInfo.userBindMac = bleMac
            userInfo.userBindDeviceType = BaseApplication.getInstance().getUserDeviceType(bleName)
            DBManager.getInstance().updateUserInfo(userInfo)


            BaseApplication.getInstance().isNeedReloadPage = true

            val broadIntent = Intent()
            broadIntent.action = BleConstant.BLE_CONNECTED_ACTION
            sendBroadcast(broadIntent)

            showConnGuideDialog()
        }

    }


    private fun showConnGuideDialog(){
        val dialog = ConnGuideDialogView(this, com.bonlala.base.R.style.BaseDialogTheme)
        dialog.show()
        dialog.setOnItemClick {
            dialog.dismiss()
            if(it == 1){
                ActivityManager.getInstance().finishActivity(MainActivity::class.java)
                finish()
                return@setOnItemClick
            }
            //进入玩转设备页面
            startActivity(GuideActivity::class.java)
            ActivityManager.getInstance().finishActivity(MainActivity::class.java)
            finish()

        }
    }

    //开始GIF
    private fun startGif(){
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        Glide.with(context).asGif().load(R.drawable.ic_scan_gif).apply(requestOptions)
            .into(scanImgView)
    }

    //停止gif
    private fun stopGif(){
        try {
            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            Glide.with(context).asGif().load(R.drawable.ic_scan_gif).apply(requestOptions)
                .into(scanImgView)
            if(scanImgView.drawable is GifDrawable){

                val drawable = scanImgView.drawable as GifDrawable
                drawable.stop()
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

}
package com.bonlala.sport

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.bonlala.action.TitleBarFragment
import com.bonlala.fitalent.BaseApplication
import com.bonlala.fitalent.HomeActivity
import com.bonlala.fitalent.R
import com.bonlala.fitalent.dialog.CountDownTimerView
import com.bonlala.fitalent.emu.MediaPlayerType
import com.bonlala.fitalent.utils.MediaPlayerUtils
import com.bonlala.sport.activity.SportGuideActivity
import com.bonlala.sport.activity.SportSettingActivity
import com.bonlala.sport.adapter.SportHomeTypeAdapter
import com.bonlala.sport.amap.AmapLocationService
import com.bonlala.sport.model.SportTypeBean
import com.bonlala.sport.view.SportHomeCyclingView
import com.bonlala.sport.view.SportHomeTypeDataView
import com.bonlala.sport.view.SportRealHrAndGpsView
import com.bonlala.sport.viewmodel.HomeTypeSportViewModel
import com.hjq.permissions.XXPermissions
import com.hjq.shape.layout.ShapeFrameLayout
import timber.log.Timber

/**
 * Create by sjh
 * @Date 2024/4/10
 * @Desc
 */
class HomeSportFragment : TitleBarFragment<HomeActivity>() {

    private var viewModel : HomeTypeSportViewModel ?= null

    private var sportHomePreviewMapView : MapView ?= null
    private var amapLocationService : AmapLocationService ?= null

    private var sportHomeTypeRy : RecyclerView ?= null
    private var typeAdapter : SportHomeTypeAdapter ?= null
    private var typeList : MutableList<SportTypeBean> ?= null

    private var sportRealView : SportRealHrAndGpsView ?= null

    private var indoorImg : ImageView ?= null

    private var aMap: AMap? = null
    private var bundle : Bundle ?= null

    private var selectIndex = 0


    private var sportHomeTotalView : SportHomeTypeDataView ?= null





    companion object{
        fun getInstance() : HomeSportFragment{
            return HomeSportFragment()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_sport_home_layout
    }

    override fun initView() {

        sportHomeTotalView = findViewById(R.id.sportHomeTotalView)

        indoorImg = findViewById(R.id.indoorImg)
        sportRealView = findViewById(R.id.sportRealView)
        sportHomeTypeRy = findViewById(R.id.sportHomeTypeRy)
        sportHomePreviewMapView = findViewById(R.id.sportHomePreviewMapView)

        val linearLayoutManager = GridLayoutManager(attachActivity,4)
        sportHomeTypeRy?.layoutManager = linearLayoutManager
        typeList = ArrayList<SportTypeBean>()
        typeAdapter = SportHomeTypeAdapter(attachActivity,typeList!!)
        sportHomeTypeRy?.adapter = typeAdapter

        typeAdapter?.setItemClick(object : OnSportCommItemClickListener{
            override fun onItemClick(position: Int) {
                showCheckTypeIndex(position)
            }

        })

        //设置
        findViewById<ImageView>(R.id.sportHomeSettImage).setOnClickListener {
            startActivity(SportSettingActivity::class.java)
        }

        //开始运动
        findViewById<FrameLayout>(R.id.homeSportStartLayout).setOnClickListener {
            startToCountDown()

        }

        sportHomePreviewMapView?.onCreate(bundle)
        aMap = sportHomePreviewMapView?.map
        // 显示实时交通状况
        aMap?.isTrafficEnabled = false
        //地图模式可选类型：MAP_TYPE_NORMAL,MAP_TYPE_SATELLITE,MAP_TYPE_NIGHT
        // 普通地图模式
        aMap?.mapType = AMap.MAP_TYPE_NORMAL
        //初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        val myLocationStyle: MyLocationStyle
        myLocationStyle = MyLocationStyle()
        //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.interval((10 * 1000).toLong())
        //设置定位蓝点的Style
        //设置定位蓝点的Style
        aMap?.myLocationStyle = myLocationStyle
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap?.isMyLocationEnabled = true
        aMap?.uiSettings?.setLogoBottomMargin(-100)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = savedInstanceState
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        sportHomePreviewMapView?.onSaveInstanceState(outState)
    }

    override fun initData() {
        mediaPlayerUtils = MediaPlayerUtils()
        viewModel = ViewModelProvider(this)[HomeTypeSportViewModel::class.java]
        amapLocationService = AmapLocationService(attachActivity)
        amapLocationService?.setOnLocationListener(onLocationListener)
        startToLocation()

        val list = viewModel?.getHomeSportTypeData(attachActivity)
        typeList?.addAll(list!!)
        typeAdapter?.notifyDataSetChanged()
        showCheckTypeIndex(0)

        viewModel?.totalRecordBean?.observe(this){
            sportHomeTotalView?.setTotalData(it)
        }

        viewModel?.lastRecordBean?.observe(this){
            if(it == null){
                sportHomeTotalView?.setLastEmptyData()
                return@observe
            }
            sportHomeTotalView?.setLastTotalBean(it)
        }
    }


    private fun startToLocation(){
        val isP = ActivityCompat.checkSelfPermission(attachActivity,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
        if(isP){
            amapLocationService?.startLocation()
            return
        }

        ActivityCompat.requestPermissions(attachActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),0x00)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 0x00 ){
            amapLocationService?.startLocation()
        }
    }


    override fun onResume() {
        super.onResume()
        sportHomePreviewMapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
      //  sportHomePreviewMapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (amapLocationService != null) {
            amapLocationService!!.stopLocation()
            amapLocationService!!.destroyLocation()
        }
        if (sportHomePreviewMapView != null) {
            sportHomePreviewMapView?.onDestroy()
        }
    }


    //定位回调
    private val onLocationListener: AmapLocationService.OnLocationListener =
        AmapLocationService.OnLocationListener { searLocalBean ->
            if (aMap != null) {
                aMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            searLocalBean.latitude,
                            searLocalBean.longitude
                        ), 15f
                    )
                )
            }
            //  previewMapGpsStatusImg.setImageResource(showGpsStatus(searLocalBean.getAccuracy()))
            // gpsLogTv.setText(new Gson().toJson(searLocalBean));
            Timber.e("-----海拔高度=" + searLocalBean.getAltitude())
            sportRealView?.setGpsStatus(searLocalBean.accuracy)
        }



    private fun showCheckTypeIndex(position : Int){
        selectIndex = position
        typeList?.forEachIndexed { index, sportTypeBean ->
            sportTypeBean.isChecked = index == position
        }
        typeAdapter?.notifyDataSetChanged()

        indoorImg?.visibility = if(position == 1) View.VISIBLE else View.GONE
        sportHomePreviewMapView?.visibility = if(position ==1) View.GONE else View.VISIBLE
        sportRealView?.setGpsVisibility(position !=1)

        sportHomeTotalView?.setType(changeType(position))

        viewModel?.getRecordDbByType(changeType(position))

        viewModel?.getLastRecordByType(changeType(position))
    }

    private var mediaPlayerUtils : MediaPlayerUtils ?= null

    //开始3秒倒计时
    private fun startToCountDown(){
        val s = ActivityCompat.checkSelfPermission(attachActivity,Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED
        if(!s){
            ActivityCompat.requestPermissions(attachActivity, arrayOf(Manifest.permission.BODY_SENSORS,Manifest.permission.ACTIVITY_RECOGNITION),0x01)
            return
        }

        val l = ActivityCompat.checkSelfPermission(attachActivity,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
        if(l){
            ActivityCompat.requestPermissions(attachActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),0x02)
            return
        }

        val dialog = CountDownTimerView(attachActivity, com.bonlala.base.R.style.BaseDialogTheme)
        dialog.show()
        dialog.setCancelable(false)
        mediaPlayerUtils?.playToAudio(context, MediaPlayerType.AUDIO_THREE)
        dialog.startCountDown(3)
        dialog.setOnCompleteListener {
            dialog.dismiss()
            startToSport()
        }
        val window = dialog.window
        val windowLayout = window?.attributes
        val metrics : DisplayMetrics = resources.displayMetrics
        val width : Int = metrics.widthPixels
        val height : Int = metrics.heightPixels
        if (windowLayout != null) {
            windowLayout.width = width
            windowLayout.height = height
        }

        if (window != null) {
            window.attributes = windowLayout
        }
    }


    private fun startToSport(){
        startActivity(SportGuideActivity::class.java)
        BaseApplication.getInstance().sportAmapService?.startToSport(changeType(selectIndex))
    }

    private fun changeType(type : Int) : Int{
        if(type == 0){
            return 0
        }
        if(type == 1){
            return 3
        }
        if(type == 2){
            return 1
        }
        return 2
    }
}
package com.bonlala.sport.activity

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.ImageView
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.PolylineOptions
import com.bonlala.action.AppActivity
import com.bonlala.fitalent.BaseApplication
import com.bonlala.fitalent.R
import com.bonlala.sport.OnSportTimerListener
import com.bonlala.sport.OnSportingBackLister
import com.bonlala.sport.model.SportingBean
import com.bonlala.sport.view.CommSportRecordView
import com.bonlala.sport.view.SportRealHrAndGpsView
import com.google.gson.Gson
import com.hjq.bar.TitleBar
import timber.log.Timber


/**
 * Create by sjh
 * @Date 2024/4/11
 * @Desc
 */
class SportTrackActivity : AppActivity() {


    private var trackTitleBar : TitleBar ?= null



    private var trackMap : MapView ?= null
    private var aMap: AMap? = null
    private var bundle : Bundle ?= null

    //定位图片
    private var trackLocationImg : ImageView ?= null
    private var trackRecordView : CommSportRecordView ?= null



    //gps
    private var sportTrackGpsView : SportRealHrAndGpsView ?= null

    private var isCanvasStartMark = false

    override fun getLayoutId(): Int {
        return R.layout.activity_real_time_sport_map_layout
    }

    override fun initView() {
        trackTitleBar = findViewById(R.id.trackTitleBar)
        trackRecordView = findViewById(R.id.trackRecordView)
        trackLocationImg = findViewById(R.id.trackLocationImg)
        trackMap = findViewById(R.id.trackMap)
        sportTrackGpsView = findViewById(R.id.sportTrackGpsView)
        trackLocationImg?.setOnClickListener {
            val l = BaseApplication.getInstance()?.sportAmapService?.recentLatLng
            aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(l, 18f))
        }

    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        bundle = savedInstanceState
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        trackMap?.onSaveInstanceState(outState)
    }

    private var lngList = mutableListOf<LatLng>()

    override fun initData() {
        initMap()

        BaseApplication.getInstance()?.sportAmapService?.setOnSportingBackLister(object : OnSportingBackLister{

            override fun backSportData(
                sportBean: SportingBean,
                 linkMap: List<LatLng>,
                distance: Float
            ) {
                Timber.e("------isFinishing=$isFinishing"+sportBean.lat+"   "+sportBean.lng)
                if(isFinishing){
                    return
                }
                lngList.clear()
                aMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            sportBean.lat,
                            sportBean.lng
                        ), 18f
                    )
                )
                //绘制开始图标
                if(!isCanvasStartMark){
                    isCanvasStartMark = true
                    val list = BaseApplication.getInstance().sportAmapService?.sportPointMap
                    if(list?.size!!>0){
                        canvasStartMark(list[0].latitude,list[0].longitude)
                    }else{
                        canvasStartMark(sportBean.lat,sportBean.lng)
                    }

                }

                sportTrackGpsView?.setGpsStatus(sportBean.accuracy)
                lngList.addAll(linkMap)
                Timber.e("----绘制轨迹记录="+Gson().toJson(lngList))
                aMap?.addPolyline(
                    PolylineOptions().addAll(lngList).width(20f).color(Color.parseColor("#4DDA64")))
                val d = if(distance == 0.0F) "0.0" else (distance / 1000)
                //设置数据
                trackRecordView?.setDistance(d as Float)

            }



            override fun backTimerTime(time: Int) {
                trackRecordView?.setSportTime(time)
            }

        })

        if(BaseApplication.getInstance()?.sportAmapService?.isPauseSport == true){
            var bean = BaseApplication.getInstance()?.sportAmapService?.tempSportBean
            if(bean == null){
                return
            }
            val d = if(bean.totalDis == 0.0F) 0F else (bean.totalDis / 1000)
            //设置数据
            trackRecordView?.setDistance(d )
            trackRecordView?.setSportTime(bean.totalTime)

            val list = BaseApplication.getInstance()?.sportAmapService?.sportPointMap
            if(list!!.size>0){
                val start = list[0]
                canvasStartMark(start.latitude,start.longitude)
                lngList.clear()
                lngList.addAll(list)
                Timber.e("----绘制轨迹记录="+Gson().toJson(lngList))
                aMap?.addPolyline(
                    PolylineOptions().addAll(lngList).width(20f).color(Color.parseColor("#4DDA64")))
            }
        }
    }



    //绘制开始
    private fun canvasStartMark(lat : Double,lng : Double){
        isCanvasStartMark = true
        val startIcon = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources,R.mipmap.ic_amap_start_img))
        val latLng = LatLng(lat, lng)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.icon(startIcon)
        markerOptions.isFlat = true
        aMap?.addMarker(markerOptions)
    }


    override fun onResume() {
        super.onResume()
        trackMap?.onResume()
    }

    override fun onDestroy() {

        trackMap?.onDestroy()
        Timber.e("--------ondestory")
       // BaseApplication.getInstance().sportAmapService?.clearListener()
        super.onDestroy()
    }

    private fun initMap(){
        trackMap?.onCreate(bundle)
        aMap = trackMap?.map
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
        myLocationStyle?.strokeColor(Color.TRANSPARENT)
        myLocationStyle.strokeWidth(1F)
        myLocationStyle.radiusFillColor(Color.TRANSPARENT)
        //设置定位蓝点的Style
        aMap?.myLocationStyle = myLocationStyle

        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap?.isMyLocationEnabled = true
        aMap?.uiSettings?.setLogoBottomMargin(-100)
    }
}
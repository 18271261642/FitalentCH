package com.bonlala.sport.activity

import android.widget.ImageView
import android.widget.TextView
import com.amap.api.maps.model.LatLng
import com.bonlala.action.AppActivity
import com.bonlala.fitalent.BaseApplication
import com.bonlala.fitalent.R
import com.bonlala.fitalent.utils.SpannableUtils
import com.bonlala.sport.OnSportingBackLister
import com.bonlala.sport.model.SportingBean
import com.bonlala.sport.view.CommSportRecordView
import com.bonlala.sport.view.SportPauseContinueView
import com.bonlala.sport.view.SportRealHrAndGpsView
import timber.log.Timber

/**
 * Create by sjh
 * @Date 2024/4/11
 * @Desc
 */
class SportGuideActivity : AppActivity() {

    //总距离
    private var sportGuideTotalDistanceTv : TextView ?= null
    private var sportGuideGpsView : SportRealHrAndGpsView ?= null
    private var sportGuideRecordView : CommSportRecordView ?= null


    private var sportPauseOrEndView : SportPauseContinueView ?= null


    override fun getLayoutId(): Int {
        return R.layout.activity_sport_guide_layout
    }

    override fun initView() {
        sportPauseOrEndView = findViewById(R.id.sportPauseOrEndView)
        sportGuideRecordView = findViewById(R.id.sportGuideRecordView)
        sportGuideGpsView = findViewById(R.id.sportGuideGpsView)
        sportGuideTotalDistanceTv = findViewById(R.id.sportGuideTotalDistanceTv)
        findViewById<ImageView>(R.id.guideToMapImageView).setOnClickListener {

            startActivity(SportTrackActivity::class.java)
        }
        sportGuideRecordView?.setTxt3(resources.getString(R.string.string_consumption))

        sportPauseOrEndView?.setOnSportEndListener(object : SportPauseContinueView.OnSportEndListener{
            override fun onEndSport() {
                BaseApplication.getInstance().sportAmapService?.stopToSport()
                finish()
            }

        })
    }

    override fun initData() {
        sportGuideTotalDistanceTv?.text = SpannableUtils.getTargetType("--","km")

    }


    override fun onResume() {
        super.onResume()
        Timber.e("--------onResume=")
        BaseApplication.getInstance()?.sportAmapService?.setOnSportingBackLister(object : OnSportingBackLister{

            override fun backSportData(
                sportBean: SportingBean,
                linkMap: List<LatLng>,
                distance: Float
            ) {
                val d = if(distance == 0.0F) "0.0" else (distance / 1000)
                sportGuideGpsView?.setGpsStatus(sportBean.accuracy)
                sportGuideTotalDistanceTv?.text = SpannableUtils.getTargetType(String.format("%.2f",d),"km")
            }

            override fun backTimerTime(time: Int) {
                sportGuideRecordView?.setSportTime(time)
            }

        })
    }


    override fun onDestroy() {
        BaseApplication.getInstance().sportAmapService?.stopToSport()
        super.onDestroy()
    }

    private var isSport = true

    override fun onBackPressed() {
        if(isSport){
            return
        }
        super.onBackPressed()
    }

}
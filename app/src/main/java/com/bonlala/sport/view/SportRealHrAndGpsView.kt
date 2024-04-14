package com.bonlala.sport.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bonlala.fitalent.R

/**
 * Create by sjh
 * @Date 2024/4/10
 * @Desc
 */
class SportRealHrAndGpsView : LinearLayout {

    private var sportRealHrTv : TextView ?= null
    private var sportRealGpsImg : ImageView ?= null

    private var gpsCardView : CardView ?= null


    constructor(context: Context) : super (context){

    }

    constructor(context: Context,attribute : AttributeSet) : super (context,attribute){
        initViews(context)
    }

    constructor(context: Context, attribute: AttributeSet, defStyleAttr : Int) : super (context,attribute,defStyleAttr){
        initViews(context)
    }


    private fun initViews(context: Context){
        val view = LayoutInflater.from(context).inflate(R.layout.sport_heart_and_gps_layout,this,true)
        sportRealHrTv = view.findViewById(R.id.sportRealHrTv)
        sportRealGpsImg = view.findViewById(R.id.sportRealGpsImg)
        gpsCardView = view.findViewById(R.id.gpsCardView)
    }


    fun setGpsVisibility(show : Boolean){
        gpsCardView?.visibility = if(show) View.VISIBLE else View.GONE
    }

    fun setGpsStatus(accuracy : Float){
        if(accuracy<=65){
            sportRealGpsImg?.setImageResource(R.mipmap.ic_sport_gps_3)
            return
        }
        if(accuracy >65 && accuracy<100){
            sportRealGpsImg?.setImageResource(R.mipmap.ic_sport_gps_2)
            return
        }
        if(accuracy>100 && accuracy<200){
            sportRealGpsImg?.setImageResource(R.mipmap.ic_sport_gps_1)
            return
        }
        sportRealGpsImg?.setImageResource(R.mipmap.ic_sport_gps_0)
    }
}
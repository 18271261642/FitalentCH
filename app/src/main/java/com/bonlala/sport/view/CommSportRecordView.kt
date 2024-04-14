package com.bonlala.sport.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.bonlala.fitalent.R
import com.bonlala.fitalent.utils.BikeUtils
import com.bonlala.fitalent.utils.SpannableUtils

/**
 * Create by sjh
 * @Date 2024/4/11
 * @Desc
 */
class CommSportRecordView : LinearLayout {

    //配速
    private var mapSportPaceTv : TextView ?= null
    //时长
    private var mapSportDurationTv : TextView ?= null
    //消耗
    private var mapSportConsumeTv : TextView ?=  null

    private var mapRecordTxt3Tv : TextView ?= null



    constructor(context: Context) : super (context){

    }

    constructor(context: Context, attribute : AttributeSet) : super (context,attribute){
        initViews(context)
    }

    constructor(context: Context, attribute: AttributeSet, defStyleAttr : Int) : super (context,attribute,defStyleAttr){
        initViews(context)
    }


    private fun initViews(context: Context){
        val view = LayoutInflater.from(context).inflate(R.layout.comm_map_sport_record_view,this,true)
        mapSportPaceTv = view.findViewById(R.id.mapSportPaceTv)
        mapSportDurationTv = view.findViewById(R.id.mapSportDurationTv)
        mapSportConsumeTv = view.findViewById(R.id.mapSportConsumeTv)
        mapRecordTxt3Tv = view.findViewById(R.id.mapRecordTxt3Tv)
        setEmptyData()
    }

    fun setEmptyData(){
        mapSportPaceTv?.text = "0'00''"
        mapSportDurationTv?.text = "00:00:00"
        mapSportConsumeTv?.text = "0.0"
    }


    //设置第三个文字
    fun setTxt3(txt : String){
        mapRecordTxt3Tv?.text = txt
    }


    //设置消耗
    fun setConsumeValue(kcal : Int){
        mapSportConsumeTv?.text = SpannableUtils.getTargetType(kcal.toString(),"kcal")
    }


    //设置公里
    fun setDistance(dis : Float){
        mapSportConsumeTv?.text = String.format("%.2f",dis)
    }


    //用时
    fun setSportTime(time : Int){
        mapSportDurationTv?.text = BikeUtils.formatSecond(time)
    }
}
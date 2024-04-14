package com.bonlala.sport.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.bonlala.fitalent.R
import com.bonlala.fitalent.utils.BikeUtils
import com.bonlala.fitalent.utils.CalculateUtils
import com.bonlala.sport.db.SportRecordDb
import com.bonlala.sport.model.SportTotalBean
import com.google.gson.Gson
import timber.log.Timber

/**
 * Create by sjh
 * @Date 2024/4/12
 * @Desc
 */
class SportHomeTypeDataView : LinearLayout {

    private var sportTypeTotalTypeTv : TextView ?= null

    //总距离
    private var sportTypeTotalDisTv : TextView ?= null
    //总时长
    private var sportTypeTotalTimeTv :TextView ?= null
    //总消耗
    private var sportTypeTotalConsumeTv : TextView ?= null

    private var sportTypeLastTypeTv : TextView ?= null
    private var sportTypeLastDataTimeTv : TextView ?= null
    private var sportTypeLastDisTv : TextView ?= null
    private var sportTypeLastDurationTv : TextView ?= null





    constructor(context: Context) : super (context){

    }

    constructor(context: Context, attribute : AttributeSet) : super (context,attribute){
        initViews(context)
    }

    constructor(context: Context, attribute: AttributeSet, defStyleAttr : Int) : super (context,attribute,defStyleAttr){
        initViews(context)
    }


    private fun initViews(context: Context){
        val view = LayoutInflater.from(context).inflate(R.layout.sport_type_total_data_layout,this,true)
        sportTypeTotalTypeTv = view.findViewById(R.id.sportTypeTotalTypeTv)
        sportTypeTotalDisTv = view.findViewById(R.id.sportTypeTotalDisTv)
        sportTypeTotalTimeTv = view.findViewById(R.id.sportTypeTotalTimeTv)
        sportTypeTotalConsumeTv = view.findViewById(R.id.sportTypeTotalConsumeTv)
        sportTypeLastTypeTv = view.findViewById(R.id.sportTypeLastTypeTv)
        sportTypeLastDataTimeTv = view.findViewById(R.id.sportTypeLastDataTimeTv)
        sportTypeLastDisTv = view.findViewById(R.id.sportTypeLastDisTv)
        sportTypeLastDurationTv = view.findViewById(R.id.sportTypeLastDurationTv)

    }


    //设置类型
    fun setType(type : Int){
       val typeStr = checkType(type)
        sportTypeTotalTypeTv?.text = String.format(context.resources.getString(R.string.string_sport_total_data),typeStr)
        sportTypeLastTypeTv?.text = String.format(context.resources.getString(R.string.string_sport_last_data),typeStr)

    }


    fun setTotalData(bean : SportTotalBean){
        val dis = bean.totalDistance
        sportTypeTotalDisTv?.text = if(dis==0) "--" else (CalculateUtils.div(dis.toDouble(),1000.0,2)).toString()
        sportTypeTotalTimeTv?.text = if(bean.totalTime ==0) "--" else BikeUtils.formatSecond(bean.totalTime)
        sportTypeTotalConsumeTv?.text = if(bean.totalConsume == 0) "--" else bean.totalConsume.toString()
    }

    fun setLastEmptyData(){
        sportTypeLastDataTimeTv?.text =""
        sportTypeLastDisTv?.text = "--"
        sportTypeLastDurationTv?.text = "--"
    }

    fun setLastTotalBean(bean : SportRecordDb?){
        if(bean == null){
            sportTypeLastDataTimeTv?.text =""
            sportTypeLastDisTv?.text = "--"
            sportTypeLastDurationTv?.text = "--"

            return
        }
        sportTypeLastDataTimeTv?.text = BikeUtils.getFormatDate(bean.endTime*1000,"yyyy-MM-dd HH:mm:ss")
        sportTypeLastDisTv?.text = (CalculateUtils.div(bean.totalDistance.toDouble(),1000.0,2)).toString()
        sportTypeLastDurationTv?.text = BikeUtils.formatSecond(bean.sportTotalTime)

    }


    private fun checkType(type : Int) : String{
        if(type == 0){  //户外跑
          return context.resources.getString(R.string.string_type_outdoor)
        }
        if(type == 1){  //骑行
            return context.resources.getString(R.string.string_type_cycling)
        }
        if(type == 2){  //行走
            return context.resources.getString(R.string.string_type_run)
        }
        return context.resources.getString(R.string.string_type_indoor)
    }
}
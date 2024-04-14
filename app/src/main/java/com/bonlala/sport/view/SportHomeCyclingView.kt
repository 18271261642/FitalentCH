package com.bonlala.sport.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.bonlala.fitalent.R
import com.bonlala.fitalent.view.NaviTxt

/**
 * Create by sjh
 * @Date 2024/4/12
 * @Desc
 */
class SportHomeCyclingView : LinearLayout {

    //最近一次
    private var sportTypeCyclingLastTotalDisTv : NaviTxt ?= null
    private var sportTypeCyclingLastTimeTv : TextView ?= null

    private var sportTypeCyclingTotalDisTv : NaviTxt ?= null




    constructor(context: Context) : super (context){

    }

    constructor(context: Context, attribute : AttributeSet) : super (context,attribute){
        initViews(context)
    }

    constructor(context: Context, attribute: AttributeSet, defStyleAttr : Int) : super (context,attribute,defStyleAttr){
        initViews(context)
    }


    private fun initViews(context: Context){
        val view = LayoutInflater.from(context).inflate(R.layout.sport_home_type_cycling_layout,this,true)
        sportTypeCyclingLastTotalDisTv = view.findViewById(R.id.sportTypeCyclingLastTotalDisTv)
        sportTypeCyclingLastTimeTv = view.findViewById(R.id.sportTypeCyclingLastTimeTv)
        sportTypeCyclingTotalDisTv = view.findViewById(R.id.sportTypeCyclingTotalDisTv)



    }

}
package com.bonlala.sport.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bonlala.fitalent.BaseApplication
import com.bonlala.fitalent.R
import com.bonlala.fitalent.view.PausePressView

/**
 * Create by sjh
 * @Date 2024/4/11
 * @Desc
 */
class SportPauseContinueView : LinearLayout {


    private var endListener : OnSportEndListener ? = null

    fun setOnSportEndListener(c : OnSportEndListener){
        this.endListener = c
    }


    private var sportPauseParentLayout : ConstraintLayout ?= null
    private var sportPauseLayout : FrameLayout ?= null
    private var sportingPressView : PausePressView ?= null
    private var sportLockImageView : ImageView ?= null
    private var sportPauseOrLongTv : TextView ?= null

    private var unLockTv : TextView ?= null


    private var sportContinueParentLayout : LinearLayout ?= null
    private var sportContinueLayout : FrameLayout ?= null
    private var continueStopPressView : PausePressView ?= null


    constructor(context: Context) : super (context){

    }

    constructor(context: Context, attribute : AttributeSet) : super (context,attribute){
        initViews(context)
    }

    constructor(context: Context, attribute: AttributeSet, defStyleAttr : Int) : super (context,attribute,defStyleAttr){
        initViews(context)
    }



    private fun initViews(context: Context){
        val view = LayoutInflater.from(context).inflate(R.layout.item_sport_pause_continue_view,this,true)
        sportPauseParentLayout = view.findViewById(R.id.sportPauseParentLayout)
        sportPauseLayout = view.findViewById(R.id.sportPauseLayout)
        sportingPressView = view.findViewById(R.id.sportingPressView)
        sportLockImageView = view.findViewById(R.id.sportLockImageView)
        sportContinueParentLayout = view.findViewById(R.id.sportContinueParentLayout)
        sportContinueLayout = view.findViewById(R.id.sportContinueLayout)
        continueStopPressView = view.findViewById(R.id.continueStopPressView)
        sportPauseOrLongTv = view.findViewById(R.id.sportPauseOrLongTv)
        unLockTv = view.findViewById(R.id.unLockTv)

        //暂停
        sportPauseLayout?.setOnClickListener {
            BaseApplication.getInstance().sportAmapService?.setPauseOrContinueSport(true)
            setSportStatus(false)
        }

        //锁住
        sportLockImageView?.setOnClickListener {
            sportingPressView?.visibility = View.VISIBLE
            unLockTv?.visibility = View.VISIBLE
            sportPauseOrLongTv?.text = context.resources.getString(R.string.string_sport_locking)
            sportLockImageView?.visibility = View.GONE
        }

        //继续
        sportContinueLayout?.setOnClickListener {
            BaseApplication.getInstance().sportAmapService?.setPauseOrContinueSport(false)
            setSportStatus(true)
        }

        sportingPressView?.setOnCountDownStateChangeListener(object : PausePressView.OnCountDownStateChangeListener{
            override fun onCountDownEnd() {
                sportingPressView?.visibility = View.GONE
                unLockTv?.visibility = View.GONE
                sportPauseOrLongTv?.text = context.resources.getString(R.string.string_sport_pause)
                sportLockImageView?.visibility = View.VISIBLE
            }

            override fun onCountDownCancel() {

            }

        })

        //长按结束
        continueStopPressView?.setOnCountDownStateChangeListener(object :
            PausePressView.OnCountDownStateChangeListener{
            override fun onCountDownEnd() {
                endListener?.onEndSport()
            }

            override fun onCountDownCancel() {

            }

        })
    }



    //运动中或暂停状态
    fun setSportStatus(isSporting : Boolean){
        sportPauseParentLayout?.visibility = if(isSporting) View.VISIBLE else View.GONE
        sportContinueParentLayout?.visibility = if(isSporting) View.GONE else View.VISIBLE

    }


    interface OnSportEndListener{
        fun onEndSport()
    }

}
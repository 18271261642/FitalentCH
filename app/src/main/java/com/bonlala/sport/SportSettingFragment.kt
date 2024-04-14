package com.bonlala.sport

import android.os.Bundle
import com.bonlala.action.TitleBarFragment
import com.bonlala.fitalent.R
import com.bonlala.sport.activity.SportSettingActivity

/**
 * Create by sjh
 * @Date 2024/4/12
 * @Desc
 */
class SportSettingFragment : TitleBarFragment<SportSettingActivity>() {


    companion object{
        fun getInstance(type : Int) : SportSettingFragment{
            val bundle = Bundle()
            bundle.putInt("type",type)
            val fragment = SportSettingFragment()
            fragment.arguments = bundle
            return fragment
        }
    }


    override fun getLayoutId(): Int {
        return R.layout.fragment_sport_setting_layout
    }

    override fun initView() {

    }

    override fun initData() {

    }
}
package com.bonlala.sport.activity

import android.os.Build
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bonlala.action.AppActivity
import com.bonlala.fitalent.R
import com.bonlala.sport.SportSettingFragment
import com.bonlala.sport.adapter.FragmentListAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Create by sjh
 * @Date 2024/4/12
 * @Desc
 */
class SportSettingActivity : AppActivity() {


    private var sportSetTabLayout : TabLayout ?= null
    private var sportSetViewPager2 : ViewPager2 ?= null

    private var tabLayoutMediator: TabLayoutMediator? = null
    private var fragmentList: MutableList<Fragment>? = null
    private var adapter : FragmentListAdapter ?= null



    override fun getLayoutId(): Int {
        return R.layout.activity_sport_setting_layout
    }

    override fun initView() {
        sportSetTabLayout = findViewById(R.id.sportSetTabLayout)
        sportSetViewPager2 = findViewById(R.id.sportSetViewPager2)



    }

    override fun initData() {
        fragmentList = ArrayList<Fragment>()
        adapter = FragmentListAdapter(fragmentList!!,this@SportSettingActivity)
        sportSetViewPager2?.adapter = adapter
        sportSetViewPager2?.registerOnPageChangeCallback(callBack)

        val titleArray = arrayListOf<String>(resources.getString(R.string.string_type_outdoor),resources.getString(R.string.string_type_indoor)
        ,resources.getString(R.string.string_type_cycling),resources.getString(R.string.string_type_run))
        titleArray.forEachIndexed { index, s ->
            val tab =sportSetTabLayout?.newTab()
            tab!!.setText(s)
            sportSetTabLayout?.addTab(tab)
            val fm = SportSettingFragment.getInstance(index)

            fragmentList?.add(fm)
        }
        sportSetViewPager2?.offscreenPageLimit =titleArray.size
        adapter?.notifyDataSetChanged()
        tabLayoutMediator = TabLayoutMediator(sportSetTabLayout!!,sportSetViewPager2!!
        ) { tab, position ->
            tab.text = titleArray[position]
            tab.view.setOnLongClickListener { true }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tab.view.tooltipText = null
            }
        }
        tabLayoutMediator?.attach()
    }


    private  val callBack : ViewPager2.OnPageChangeCallback = object : ViewPager2.OnPageChangeCallback(){
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            sportSetViewPager2?.currentItem = position
        }

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
        }
    }

}
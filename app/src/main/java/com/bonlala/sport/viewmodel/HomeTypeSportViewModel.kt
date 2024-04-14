package com.bonlala.sport.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.bonlala.fitalent.R
import com.bonlala.fitalent.view.SingleLiveEvent
import com.bonlala.sport.db.SportDBManager
import com.bonlala.sport.db.SportRecordDb
import com.bonlala.sport.model.SportTotalBean
import com.bonlala.sport.model.SportTypeBean

/**
 * Create by sjh
 * @Date 2024/4/10
 * @Desc
 */
class HomeTypeSportViewModel : ViewModel() {


    val totalRecordBean = SingleLiveEvent<SportTotalBean>()
    var lastRecordBean = SingleLiveEvent<SportRecordDb>()

    fun getHomeSportTypeData(context: Context) : MutableList<SportTypeBean>{
        val list = mutableListOf<SportTypeBean>()
        list.add(SportTypeBean(1,context.resources.getString(R.string.string_type_outdoor),context.resources.getDrawable(R.drawable.sport_type_outdoor),true))
        list.add(SportTypeBean(2,context.resources.getString(R.string.string_type_indoor),context.resources.getDrawable(R.drawable.sport_type_intdoor),false))
        list.add(SportTypeBean(3,context.resources.getString(R.string.string_type_cycling),context.resources.getDrawable(R.drawable.sport_type_cycling),false))
        list.add(SportTypeBean(4,context.resources.getString(R.string.string_type_run),context.resources.getDrawable(R.drawable.sport_type_run),false))
        return list
    }


    fun getRecordDbByType(type : Int) {
        val bean = SportDBManager.getSportDBManager().getTotalByType(type)
        totalRecordBean.postValue(bean)

    }

    fun getLastRecordByType(type : Int){
        val bean = SportDBManager.getSportDBManager().findDbByType(type)
        lastRecordBean.postValue(bean)
    }
}
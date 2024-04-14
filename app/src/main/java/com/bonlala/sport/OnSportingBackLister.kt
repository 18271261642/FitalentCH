package com.bonlala.sport

import com.amap.api.maps.model.LatLng
import com.bonlala.sport.model.SportingBean

/**
 * Create by sjh
 * @Date 2024/4/11
 * @Desc
 */
interface OnSportingBackLister {

    fun backSportData(sportBean : SportingBean,linkMap : List<LatLng>,distance : Float)

    fun backTimerTime(time : Int)
}
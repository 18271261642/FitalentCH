package com.bonlala.fitalent.listeners;

/**
 * 心率带运动模式实时返回开始时间和运动时间的listener，用于退出app前保存数据
 * Created by Admin
 * Date 2023/2/15
 * @author Admin
 */
public interface OnHrBeltSportTimeListener {

    /**
     * 实时返回开始时间和运动时间
     * @param startTime 开始时间
     * @param sportTime 运动时间
     */
    void backSportTime(long startTime, int sportTime);
}

package com.bonlala.fitalent.activity.history


import android.os.Build
import android.view.View
import androidx.fragment.app.viewModels
import com.bonlala.action.TitleBarFragment
import com.bonlala.fitalent.R
import com.bonlala.fitalent.activity.RecordHistoryActivity
import com.bonlala.fitalent.db.DBManager
import com.bonlala.fitalent.db.model.OneDayStepModel
import com.bonlala.fitalent.db.model.StepItem
import com.bonlala.fitalent.emu.StepType
import com.bonlala.fitalent.utils.*
import com.bonlala.fitalent.view.StepChartView
import com.bonlala.fitalent.viewmodel.HistoryStepViewModel
import com.github.mikephil.charting.charts.BarChart
import com.google.gson.Gson
import kotlinx.android.synthetic.main.common_history_bot_date_layout.*
import kotlinx.android.synthetic.main.fragment_history_step_layout.*
import timber.log.Timber

/**
 * 步数fragment
 * Created by Admin
 *Date 2022/9/27
 */
class HistoryStepFragment : TitleBarFragment<RecordHistoryActivity>() {

    private val viewModel by viewModels<HistoryStepViewModel>()

    private var stepChart : BarChart ?= null

    private var stepChartView : StepChartView ?= null

    private var dayStr : String ?= null

    //类型 日，周，月，年
    private var dataType =  StepType.DAY

    private val dateList = mutableListOf<String>()


    override fun getLayoutId(): Int {

       return R.layout.fragment_history_step_layout
    }

    override fun initView() {
        stepChart = findViewById(R.id.stepBarChart)
        stepChartView = findViewById(R.id.stepChartView)


        setOnClickListener(R.id.stepDayLayout,R.id.stepWeekLayout,R.id.stepMonthLayout,R.id.stepYearLayout,
        R.id.commonHistoryLeftImg,R.id.commonHistoryRightImg,R.id.commonHistoryCurrentTv,R.id.commonHistoryCalendarImg)
    }

    override fun initData() {

        dayStr = BikeUtils.getCurrDate()
        showResult()

        val mac = DBManager.getBindMac()
        viewModel.getAllStepRecord(mac)
        showDayCheck(0)

    }

    private fun showResult(){
        val stepGoal = MmkvUtils.getStepGoal()
        //记录的数据
        viewModel.allStepRecord.observe(viewLifecycleOwner){
            Timber.e("------记录的数据="+Gson().toJson(it))
            dateList.clear()
            dateList.addAll(it)
            if(it == null){
                //默认日
                showDayCheck(0)
            }else{
                dayStr = it[0]
                getDayData()
            }
        }


        //日
        viewModel.onDayStepStr.observe(viewLifecycleOwner){
            Timber.e("------一天的计步="+Gson().toJson(it))

            if(it == null){
                showEmptyData()
                return@observe
            }
            showValidData(StepType.DAY,it.detailStep,it.dayStep,it.dayDistance,it.dayCalories)

        }

        //月
        viewModel.oneMonthStepList.observe(viewLifecycleOwner){
            val stepStr = it.detailStep
            showStepGoal(stepStr,BikeUtils.getMonthLastDay(dayStr,false),MmkvUtils.getStepGoal())
            showValidData(StepType.MONTH,it.detailStep,it.dayStep,it.dayDistance,it.dayCalories)
        }


        //年
        viewModel.oneYearStepData.observe(viewLifecycleOwner){
            val map = it
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                map.forEach { i, oneDayStepModel ->
                    val stepStr = oneDayStepModel.detailStep
//                    showStepGoal(stepStr,365,stepGoal)
                    attachActivity.setStepSchedule(i.toFloat(),365f)
                    showValidData(StepType.YEAR,oneDayStepModel.detailStep,oneDayStepModel.dayStep,oneDayStepModel.dayDistance,oneDayStepModel.dayCalories)
                }
            }

        }

        //周
        viewModel.onWeekStepList.observe(viewLifecycleOwner){
            val stepStr = it.detailStep
            showStepGoal(stepStr,7,stepGoal)

            showValidData(StepType.WEEK,it.detailStep,it.dayStep,it.dayDistance,it.dayCalories)
        }
    }


    private fun showStepGoal(stepDetail : String,days: Int,stepGoal : Int){
        val stepList = GsonUtils.getGsonObject<List<StepItem>>(stepDetail)
        var count = 0
        stepList?.forEachIndexed { index, stepItem ->
            Timber.e("------年="+stepItem.step+" "+stepGoal)
            if(stepItem.step>=stepGoal){
                count++
            }
        }
        attachActivity.setStepSchedule(count.toFloat(),days.toFloat())
    }



    //日的数据
    private fun getDayData(){
        val mac =DBManager.getBindMac()
        if(BikeUtils.daySizeOrEqual(dayStr,BikeUtils.getCurrDate())){
            commonHistoryCurrentTv.visibility = View.GONE
            commonHistoryRightImg.visibility = View.INVISIBLE
            dayStr = BikeUtils.getCurrDate()
            dayStr?.let { viewModel.getOnDayStepByDay(it,mac) }
            return
        }

        commonHistoryRightImg.visibility = View.VISIBLE
        if(!BikeUtils.isEqualDay(dayStr,BikeUtils.getCurrDate())){
            commonHistoryCurrentTv.visibility = View.VISIBLE
        }else{
            commonHistoryCurrentTv.visibility = View.GONE
        }

        dayStr?.let { viewModel.getOnDayStepByDay(it,mac) }

    }


    //年的数据
    private fun getYearData(){
        val mac = DBManager.getBindMac()

        //目标步数
        val goalStep = MmkvUtils.getStepGoal()

        if(BikeUtils.daySizeOrEqual(dayStr,BikeUtils.getCurrDate())){
            commonHistoryCurrentTv.visibility = View.GONE
            commonHistoryRightImg.visibility = View.INVISIBLE
            dayStr = BikeUtils.getCurrDate()
            viewModel.getOneYearStep(attachActivity,mac,dayStr.toString(),goalStep)
            return
        }

        commonHistoryRightImg.visibility = View.VISIBLE

        if(!BikeUtils.isEqualDay(dayStr,BikeUtils.getCurrDate())){
            commonHistoryCurrentTv.visibility = View.VISIBLE
        }else{
            commonHistoryCurrentTv.visibility = View.GONE
        }
        viewModel.getOneYearStep(attachActivity,mac,dayStr.toString(),goalStep)
    }


    //周的数据
    private fun getWeekData(){
        val mac = DBManager.getBindMac()
        if(BikeUtils.daySizeOrEqual(dayStr,BikeUtils.getCurrDate())){
            commonHistoryCurrentTv.visibility = View.GONE
            commonHistoryRightImg.visibility = View.INVISIBLE
            dayStr = BikeUtils.getCurrDate()
            viewModel.getOneWeekStepData(attachActivity, mac,dayStr.toString())
            return
        }
        commonHistoryRightImg.visibility = View.VISIBLE
        if(!BikeUtils.isEqualDay(dayStr,BikeUtils.getCurrDate())){
            commonHistoryCurrentTv.visibility = View.VISIBLE
        }else{
            commonHistoryCurrentTv.visibility = View.GONE
        }
        viewModel.getOneWeekStepData(attachActivity, mac,dayStr.toString())
    }


    //组装月的数据，自然月
    private fun getMonthData(){
        Timber.e("-------月="+dayStr)
        if(BikeUtils.daySizeOrEqual("yyyy-MM",dayStr,BikeUtils.getDayByMonth(BikeUtils.getCurrDate()))){
            commonHistoryCurrentTv.visibility = View.GONE
            commonHistoryRightImg.visibility = View.INVISIBLE
            dayStr = BikeUtils.getDayByMonth(BikeUtils.getCurrDate())
            viewModel.getOneMonthStep(dayStr.toString(),DBManager.getBindMac())
            return
        }

        commonHistoryRightImg.visibility = View.VISIBLE

        if(!BikeUtils.isEqualDay("yyyy-MM",dayStr,BikeUtils.getDayByMonth(BikeUtils.getCurrDate()))){
            commonHistoryCurrentTv.visibility = View.VISIBLE
        }else{
            commonHistoryCurrentTv.visibility = View.GONE
        }

        viewModel.getOneMonthStep(dayStr.toString(),DBManager.getBindMac())
    }


    //展示有效的数据
    private fun showValidData(type : StepType,detailStep : String,dayStep : Int,dayDistance : Int,dayCalories : Int){
        val oneDayStepModel = OneDayStepModel()
        Timber.e("-----有效的数据="+detailStep)
        val stepItemList = GsonUtils.getGsonObject<List<StepItem>>(detailStep)
        val tempList = stepItemList?.reversed()
        (0 until tempList?.size!!).also { range->
            range.forEach fe@{
                if(tempList.get(it).step !=0){
                    tempList.get(it).isChecked = true
                    return@also
                }

            }
        }

        oneDayStepModel.detailStep =Gson().toJson(tempList.reversed())
        oneDayStepModel.dayStr = dayStr
        oneDayStepModel.dayStep = dayStep
        oneDayStepModel.dayDistance = dayDistance
        oneDayStepModel.dayCalories = dayCalories
        stepChartView?.stepType = type
        stepChartView?.oneDayStepModel = oneDayStepModel

        //是否达标
        val stepGoal = MmkvUtils.getStepGoal()
        if(type == StepType.DAY){   //日
            attachActivity.setStepSchedule(oneDayStepModel.dayStep.toFloat(),stepGoal.toFloat())
        }


        //总的计步
        stepTotalStepTv.text = oneDayStepModel.dayStep.toString()
        //距离
        val distance = oneDayStepModel.dayDistance
        val kmDis = CalculateUtils.mToKm(distance)
        val isKm = MmkvUtils.getUnit()
        stepHistoryDistanceTv.text = if(isKm) ":$kmDis" else ":"+CalculateUtils.kmToMiValue(kmDis).toString()
        historyStepUnitTv.text = if(isKm) "km" else "mi"

        stepHistoryKcalTv.text = ":"+oneDayStepModel.dayCalories.toString()

        val isChinese = LanguageUtils.isChinese()


        //计算平均步数，日不显示
        if(type != StepType.DAY){
            var countDayStep = 0
            var countDayNumbers = 0
            tempList.reversed().forEach {
                if(it.step != 0){
                    countDayStep+=it.step
                    countDayNumbers++
                }
            }
            if(countDayStep == 0 || countDayStep == 0){
                stepHistoryAvgStepTv.text = "--"
            }else{
                val avgStep = countDayStep / countDayNumbers

                stepHistoryAvgStepTv.text = ":"+avgStep.toInt().toString()
            }

        }


        if(type == StepType.WEEK){
            //val weekCalendar = BikeUtils.getDayCalendar(dayStr)
            //周的第一天和最后一天，周日到周六
            val sunDay =if(isChinese) BikeUtils.getWeekSunToStaChinese(BikeUtils.transToDate(dayStr)) else BikeUtils.getWeekSunToStaForEnglish(BikeUtils.transToDate(dayStr))
            commonHistoryDateTv.text = sunDay
        }
        else if(type == StepType.MONTH){
            commonHistoryDateTv.text = if(isChinese) dayStr else BikeUtils.getDayByMonthEn(dayStr)
        }

        else if(type == StepType.YEAR){
            val yearStr = BikeUtils.getDayOfYear(dayStr)
            commonHistoryDateTv.text = yearStr.toString()
        }
        else{

            commonHistoryDateTv.text = if(isChinese) dayStr else BikeUtils.getFormatEnglishDate(dayStr)
        }

    }


    //展示空的数据
    private fun showEmptyData(){
        val isChinese = LanguageUtils.isChinese()
        attachActivity.setStepSchedule(0f,MmkvUtils.getStepGoal().toFloat())
        commonHistoryDateTv.text =if(isChinese) dayStr else BikeUtils.getFormatEnglishDate(dayStr)
        //总的计步
        stepTotalStepTv.text = "-- "
        //距离
        stepHistoryDistanceTv.text = ":--"
        stepHistoryKcalTv.text = ":--"

        val oneDayStepModel = OneDayStepModel()
        oneDayStepModel.detailStep = emptyHourListData()
        oneDayStepModel.dayStr = dayStr
        oneDayStepModel.dayStep = 0
        oneDayStepModel.dayDistance = 0
        oneDayStepModel.dayCalories = 0
        stepChartView?.stepType = StepType.DAY
        stepChartView?.oneDayStepModel = oneDayStepModel
    }





    private fun showDayCheck(code : Int){
        stepDayView.visibility = View.INVISIBLE
        stepWeekView.visibility = View.INVISIBLE
        stepMonthView.visibility = View.INVISIBLE
        stepYearView.visibility = View.INVISIBLE

        commonHistoryCalendarImg.visibility = if(code == 0) View.VISIBLE else View.GONE
        dayStr = BikeUtils.getCurrDate()

        if(code == 0){  //日
            stepDayView.visibility = View.VISIBLE
            dataType = StepType.DAY
//            dayStr = BikeUtils.getCurrDate()
//            getDayData()
            stepAvgLayout.visibility = View.GONE
            attachActivity.setTypeGoal(resources.getString(R.string.string_daily_day))
        }
        if(code == 1){  //周
            stepWeekView.visibility = View.VISIBLE
            dataType = StepType.WEEK
//            dayStr = BikeUtils.getCurrDate()
//            getWeekData()
            stepAvgLayout.visibility = View.VISIBLE
            attachActivity.setTypeGoal(resources.getString(R.string.string_week))

        }
        if(code == 2){
            stepMonthView.visibility = View.VISIBLE
            dataType = StepType.MONTH
//            dayStr = BikeUtils.getFormatDate(System.currentTimeMillis(),"yyyy-MM")
//            getMonthData()
            stepAvgLayout.visibility = View.VISIBLE
            attachActivity.setTypeGoal(resources.getString(R.string.string_month))
        }
        if(code == 3){
            stepYearView.visibility = View.VISIBLE
            dataType = StepType.YEAR
            attachActivity.setTypeGoal(resources.getString(R.string.string_year))
        }

        backCurrentDay()
    }


    override fun onClick(view: View?) {
        super.onClick(view)
        val id = view?.id;
        when (id){
            //回到当天
            R.id.commonHistoryCurrentTv->{
                backCurrentDay()
            }

            R.id.stepDayLayout->{
                showDayCheck(0)
            }
            R.id.stepWeekLayout->{
                showDayCheck(1)
            }
            R.id.stepMonthLayout->{
                showDayCheck(2)
            }
            R.id.stepYearLayout->{
                showDayCheck(3)
            }

            //前一天
            R.id.commonHistoryLeftImg->{
                selectDate(true)
            }
            //后一天
            R.id.commonHistoryRightImg->{
                selectDate(false)
            }


            //日历
            R.id.commonHistoryCalendarImg->{
                showCalendar()
            }
        }
    }

    //显示日历
    private fun showCalendar(){
        showCalendarSelectDialog(dateList
        ) { day ->
            hidCalendarDialog()
            dayStr = day
            getDayData()
        }

    }




    //设置日期，前true或后false
    private fun selectDate(date : Boolean){

        if(dataType == StepType.DAY){
            val timeLong = BikeUtils.getBeforeOrAfterDay(dayStr,date)
            dayStr = BikeUtils.getFormatDate(timeLong,"yyyy-MM-dd")
            getDayData()
        }

        if(dataType == StepType.WEEK){
            val beforeOrAfterDay = BikeUtils.getBeforeOrAfterDay2(dayStr,if(date) -7 else 7)
            dayStr = BikeUtils.getFormatDate(beforeOrAfterDay,"yyyy-MM-dd")
            getWeekData()
        }

        if(dataType == StepType.MONTH){
            dayStr = BikeUtils.getNextOrLastMonth(dayStr,date)
            getMonthData()
        }

        if(dataType == StepType.YEAR){
            dayStr = BikeUtils.getPreviewOrNextYear(dayStr,date)
            getYearData()
        }

//        if(!BikeUtils.isEqualDay(dayStr,BikeUtils.getCurrDate())){
//            commonHistoryCurrentTv.visibility = View.VISIBLE
//        }else{
//            commonHistoryCurrentTv.visibility = View.GONE
//        }
    }

    //回到当天
    private fun backCurrentDay(){
        dayStr = BikeUtils.getCurrDate()
        if(dataType == StepType.DAY){
            getDayData()
        }

        if(dataType == StepType.WEEK){
            getWeekData()
        }

        if(dataType == StepType.MONTH){
            dayStr = BikeUtils.getDayByMonth(dayStr)
            getMonthData()
        }

        if(dataType == StepType.YEAR){
            getYearData()
        }
        commonHistoryCurrentTv.visibility = View.GONE
    }


    //空的天数据，24个小时
    private fun emptyHourListData() : String{
        val emptyStepList = ArrayList<StepItem>()
        for(i in 0..23){
            val stepItem = StepItem(0,i)
            emptyStepList.add(stepItem)
        }
        return Gson().toJson(emptyStepList)
    }


}
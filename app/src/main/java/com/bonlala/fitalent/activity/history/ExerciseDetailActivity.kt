package com.bonlala.fitalent.activity.history

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import com.blala.blalable.Utils
import com.bonlala.action.AppActivity
import com.bonlala.fitalent.BaseApplication
import com.bonlala.fitalent.R
import com.bonlala.fitalent.adapter.ItemExerciseAdapter
import com.bonlala.fitalent.bean.ExerciseItemBean
import com.bonlala.fitalent.chartview.LineChartEntity
import com.bonlala.fitalent.chartview.PieChartUtils
import com.bonlala.fitalent.db.DBManager
import com.bonlala.fitalent.db.model.ExerciseModel
import com.bonlala.fitalent.emu.DeviceType
import com.bonlala.fitalent.emu.W560BExerciseType
import com.bonlala.fitalent.utils.*
import com.hjq.permissions.XXPermissions
import kotlinx.android.synthetic.main.activity_exercise_detail_layout.*
import kotlinx.android.synthetic.main.item_detail_sport_top_layout.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.math.min

/**
 * 锻炼详情页面
 * Created by Admin
 *Date 2022/10/20
 */
class ExerciseDetailActivity : AppActivity() {

    private var list : MutableList<ExerciseItemBean> ?= null
    private  var itemAdapter : ItemExerciseAdapter ?= null

    override fun getLayoutId(): Int {
       return R.layout.activity_exercise_detail_layout
    }

    override fun initView() {
        itemDetailSportTempBackImg.visibility = View.INVISIBLE
        
        XXPermissions.with(this).permission(Manifest.permission.WRITE_EXTERNAL_STORAGE).request { permissions, all ->  }

        list = mutableListOf()
        val gridLayoutManager = GridLayoutManager(
            context, 3
        )
        itemDetailSportTypeRy.layoutManager = gridLayoutManager
        itemAdapter = ItemExerciseAdapter(this,list)
        itemDetailSportTypeRy.adapter = itemAdapter
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onRightClick(view: View?) {
        super.onRightClick(view)

        showShare(true)
        GlobalScope.launch {
            delay(100)
            toShare()
        }

    }

    private fun toShare(){
        val bitmap = shotNestedScrollView(exerciseShareView)

        val str = FileUtils.savePic(bitmap,this)
        if(BikeUtils.isEmpty(str)){

            return
        }
        Timber.e("---str="+str)
        val u2 = FileProvider.getUriForFile(
            context, context.packageName.toString() + ".provider",
            File(str)
        )

        shareImage(this@ExerciseDetailActivity,u2,resources.getString(R.string.string_share))
    }


    override fun initData() {
        val exercise_itemStr = intent.getStringExtra("exercise_item")
        if(exercise_itemStr == null)
            return
        val exceriseB = GsonUtils.getGsonObject<ExerciseModel>(exercise_itemStr)

        Timber.e("-----dbbb="+exceriseB.toString())
        if(exceriseB == null)
            return
        val avgHr = intent.getIntExtra("avg_hr",0)
        exerciseAvgTv.text = avgHr.toString()
        showDetail(exceriseB,avgHr)
    }


    private fun showDetail(exerciseModel: ExerciseModel,avgHr : Int){

        val isChinese = BaseApplication.getInstance().isChinese

        itemDetailSportDateTv.text = if(isChinese) exerciseModel.dayStr else BikeUtils.getFormatEnglishDate(exerciseModel.dayStr)
        val tempList = getTypeMap(exerciseModel)
        list?.clear()
        if (tempList != null) {
            list?.addAll(tempList)
        }
        itemAdapter?.notifyDataSetChanged()


        itemDetailSportTypeImg.setImageResource(W560BExerciseType.getTypeResource(exerciseModel.type))

        val cusName = exerciseModel.hrBeltInputName
        Timber.e("----cusName="+cusName)
        itemDetailSportTypeNameTv.text = if(BikeUtils.isEmpty(cusName)) W560BExerciseType.getHrBeltInputType(
            exerciseModel.type,
            context
        ) else cusName



//        itemDetailSportTypeNameTv.text = W560BExerciseType.getW560BTypeName(
//            exerciseModel.type,
//            context
//        )
        itemDetailStartTimeTv.text = exerciseModel.startTimeStr + "~" + exerciseModel.endTimeStr
        itemDetailSportTimeTv.text = exerciseModel.hourMinute
        //心率
        val hrArray = exerciseModel.hrArray
        if(hrArray != null){
            Timber.e("---hr="+hrArray)
            val hrList = GsonUtils.getGsonObject<List<Int>>(hrArray)

            if(hrList == null || hrList.isEmpty()){

                return
            }

            val chartList = mutableListOf<LineChartEntity>()
            Timber.e("-----长度="+hrList?.size+" "+exerciseModel.changeSecond())

            val tempListHr = mutableListOf<Int>()
            hrList.forEach {
                if(it !=0 ){
                    tempListHr.add(it)
                }
            }

            //最大心率
            val maxHr = MmkvUtils.getMaxUserHeartValue()

            val hrMaxValue = Collections.max(hrList)
            val hrMinValue =Collections.min(tempListHr)

            var countHr = 0

            var point1 = 0
            var point2 = 0
            var point3 = 0
            var point4 = 0
            var point5 = 0
            var point6 = 0

            //这里这一个判断，可能会出现实际的心率数组值比运动时间多的问题，就判断如果心率数组比运动时间多，就截取心率数组
            val tempList = mutableListOf<Int>()



            //是否是心率带
            val isHrBelt = DBManager.getBindDeviceType() == DeviceType.DEVICE_561 || DBManager.getBindDeviceType() == DeviceType.DEVICE_W575
            //是心率带
            if(isHrBelt){
                tempList.clear()
                val hrBeltList = exerciseModel.hourHrList

                val minuteItem = exerciseModel.exerciseTime
                if(hrBeltList.size> minuteItem){
                    tempList.addAll(hrBeltList.subList(0,minuteItem))
                }else{
                    tempList.addAll(hrBeltList)
                }
            }



            tempList.forEachIndexed { index, i ->
                Timber.e("-----index="+index+" -="+i)
                //心率强度百分比
                //心率强度百分比
               // val maxHr = MmkvUtils.getMaxUserHeartValue()
                val hrPercent = HeartRateConvertUtils.hearRate2Percent(i, maxHr)
                countHr+=i
                //心率点数，根据点数设置背景和颜色
                val point = HeartRateConvertUtils.hearRate2Point(i, hrPercent)
                if(point == 0){
                    point1++
                }
                if(point == 1){
                    point2++
                }
                if(point == 2){
                    point3++
                }
                if(point == 3){
                    point4++
                }
                if(point == 4){
                    point5++
                }
                if(point == 5){
                    point6++
                }

                val color = HeartRateConvertUtils.getColorByPoint(context, point)
                if(i != 0){
                    chartList.add(LineChartEntity(index.toString(), i.toFloat(),color))
                }
            }

            exerciseChartView.setData(chartList,true,hrMaxValue,hrMinValue, exerciseModel.startTimeStr,exerciseModel.endTimeStr)

            exerciseMaxTv.text = hrMaxValue.toString()
            exerciseMinTv.text = hrMinValue.toString()
//            if (hrList != null) {
//                exerciseAvgTv.text = (countHr / hrList.size).toInt().toString()
//            }


            val timeList = mutableListOf<Int>()
            timeList.add(point1)
            timeList.add(point2)
            timeList.add(point3)
            timeList.add(point4)
            timeList.add(point5)
            timeList.add(point6)
            exerciseTimeChart.setExerciseTime(timeList)


            val pieChartUtils = PieChartUtils()
            pieChartUtils.setPieChart(exercisePieChart)
            pieChartUtils.setData(timeList,100f,this@ExerciseDetailActivity,false)


            showHrBeltHrColor(hrMaxValue,hrMinValue,avgHr)
        }
    }


    private fun getTypeResource(type: Int): Int {
        if (type == W560BExerciseType.TYPE_WALK) {
            return R.mipmap.ic_sport_walk
        }
        if (type == W560BExerciseType.TYPE_RUN) {
            return R.mipmap.ic_sport_run
        }
        if (type == W560BExerciseType.TYPE_RIDE) {
            return R.mipmap.ic_sport_ride
        }
        if (type == W560BExerciseType.TYPE_MOUNTAINEERING) {
            return R.mipmap.ic_sport_mountaineering
        }
        if (type == W560BExerciseType.TYPE_FOOTBALL) {
            return R.mipmap.ic_sport_football
        }
        if (type == W560BExerciseType.TYPE_BASKETBALL) {
            return R.mipmap.ic_sport_basketball
        }
        if (type == W560BExerciseType.TYPE_PINGPONG) {
            return R.mipmap.ic_sport_pingpong
        }


        //心率带普通计时

        //心率带普通计时
        if (type == W560BExerciseType.HR_BELT_FORWARD_TYPE) {
            return R.mipmap.ic_hr_belt_forward
        }

        //系列带倒计时

        //系列带倒计时
        if (type == W560BExerciseType.HR_BELT_COUNTDOWN_TYPE) {
            return R.mipmap.ic_hr_belt_countdown
        }

        //心率带分组计时

        //心率带分组计时
        if (type == W560BExerciseType.HR_BELT_GROUP_TYPE) {
            return R.mipmap.ic_hr_belt_group
        }

        return if (type == W560BExerciseType.TYPE_BADMINTON) {
            R.mipmap.ic_sport_badmination
        }


        else R.mipmap.ic_sport_walk
    }


    private fun shotNestedScrollView(nestedScrollView: NestedScrollView?): Bitmap? {
        return if (nestedScrollView == null) {
            null
        } else try {
            var h = 0
            // 获取ScrollView实际高度
            for (i in 0 until nestedScrollView.childCount) {
                h += nestedScrollView.getChildAt(i).height
            }
            // 创建对应大小的bitmap
            val bitmap = Bitmap.createBitmap(nestedScrollView.width, h, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            //nestedScrollView.draw(canvas)
            nestedScrollView.getChildAt(0).draw(canvas)

            // 保存图片
            //savePicture(nestedScrollView.context, bitmap)
            bitmap
        } catch (oom: OutOfMemoryError) {
            null
        }
    }


    //分享图片
    private fun shareImage(context: Context, uri: Uri?, title: String?) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/png"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        //context.startActivity(Intent.createChooser(intent, title))
        startActivityForResult(intent,0x00)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.e("------分享="+resultCode+" "+requestCode)

        showShare(false)
    }


    private fun getTypeMap(exerciseModel: ExerciseModel): List<ExerciseItemBean>? {
        val type = exerciseModel.type
        val isKm = MmkvUtils.getUnit()
        val isW560 = DBManager.getBindDeviceType() == 56011
        if(isW560){
            return if (type == W560BExerciseType.TYPE_WALK || type == W560BExerciseType.TYPE_RUN) {
                val distance = exerciseModel.distance
                val disStr = CalculateUtils.mToKm(distance)
                val list: MutableList<ExerciseItemBean> = ArrayList()


                list.add(
                    ExerciseItemBean(
                        resources.getString(R.string.string_distance),
                        getTargetType(if(isKm) disStr.toString() else CalculateUtils.kmToMiValue(disStr).toString(), if(isKm) "km" else "mi")
                    )
                )
                list.add(
                    ExerciseItemBean(
                        resources.getString(R.string.string_consumption),
                        getTargetType(exerciseModel.kcal.toString() + "", context.resources.getString(R.string.string_kcal))
                    )
                )
                list.add(
                    ExerciseItemBean(
                        resources.getString(R.string.string_count_step),
                        getTargetType(
                            exerciseModel.countStep.toString() + "",
                            resources.getString(R.string.string_step)
                        )
                    )
                )
                list.add(
                    ExerciseItemBean(
                        resources.getString(R.string.string_avg_hr),
                        getTargetType(exerciseModel.avgHr.toString() + "", "bpm")
                    )
                )

                //计算速度
                val time = exerciseModel.exerciseMinute
                //速度=距离/时间
                //速度=距离/时间
                //速度=距离/时间
                val speed = CalculateUtils.div(exerciseModel.avgSpeed.toDouble(), 10.0, 2)


                //计算配速


                //计算配速

                val pace = CalculateUtils.div(time.toDouble(),if(isKm) disStr.toDouble() else CalculateUtils.kmToMiValue(disStr).toDouble(),3)

                list.add(
                    ExerciseItemBean(
                        resources.getString(R.string.string_place),
                        getTargetType(
                            CalculateUtils.getFloatPace(pace.toFloat()),
                            ""
                        )
                    )
                )

                list.add(
                    ExerciseItemBean(
                        resources.getString(R.string.string_speed),
                        getTargetType(if(isKm)CalculateUtils.keepPoint(speed, 2).toString() else CalculateUtils.keepPoint(
                            CalculateUtils.kmToMiValue(speed.toFloat()).toDouble(),2).toString(), if(isKm) "km/h" else "mi/h")
                    )
                )
                list
            } else {
                val list: MutableList<ExerciseItemBean> = ArrayList()
                list.add(
                    ExerciseItemBean(
                        resources.getString(R.string.string_avg_hr),
                        getTargetType(exerciseModel.avgHr.toString() + "", "bpm")
                    )
                )
                list.add(
                    ExerciseItemBean(
                        resources.getString(R.string.string_consumption),
                        getTargetType(exerciseModel.kcal.toString() + "", context.resources.getString(R.string.string_kcal))
                    )
                )
                list
            }
        }else{
            val list: MutableList<ExerciseItemBean> = ArrayList()
            //心率的集合
            val hrList = exerciseModel.hrList
            //最大心率
            val maxValue = if (hrList == null) 0 else Collections.max(hrList)
            //最小心率
            val minValue = if (hrList == null) 0 else Collections.min(hrList)

            //平均心率
//            list.add(
//                ExerciseItemBean(
//                    resources.getString(R.string.string_avg_hr),
//                    getTargetType(
//                        if (exerciseModel.avgHr == 0) "--" else exerciseModel.avgHr.toString() + "",
//                        "bpm"
//                    )
//                )
//            )
//            //最大心率
//            list.add(
//                ExerciseItemBean(
//                    resources.getString(R.string.string_max_hr),
//                    getTargetType(if (maxValue == 0) "--" else maxValue.toString() + "", "bpm")
//                )
//            )
//
//            //最小心率
//            list.add(
//                ExerciseItemBean(
//                    resources.getString(R.string.string_min_hr),
//                    getTargetType(if (minValue == 0) "--" else minValue.toString() + "", "bpm")
//                )
//            )
            //卡路里
            list.add(
                ExerciseItemBean(
                    resources.getString(R.string.string_consumption),
                    getTargetType(exerciseModel.kcal.toString() + "", context.resources.getString(R.string.string_kcal))
                )
            )

            return list
        }

    }


    private fun showShare(isShare : Boolean){
        exerciseDescTv.visibility = if(isShare) View.GONE else View.VISIBLE
        exerciseDescLayout.visibility = if(isShare) View.GONE else View.VISIBLE
        shareLogoLayout.visibility = if(isShare) View.VISIBLE else View.GONE
    }



    //根据心率大小计算心率区间，显示不同的颜色
    private fun showHrBeltHrColor(max : Int,min : Int,avg : Int){
        val maxHr = MmkvUtils.getMaxUserHeartValue()
        //心率强度百分比
        val maxHrPercent = HeartRateConvertUtils.hearRate2Percent(max, maxHr)
        val minHrPercent = HeartRateConvertUtils.hearRate2Percent(min, maxHr)
        val avgHrPercent = HeartRateConvertUtils.hearRate2Percent(avg, maxHr)


        val maxPoint = HeartRateConvertUtils.hearRate2Point(max,maxHrPercent)
        val minPoint = HeartRateConvertUtils.hearRate2Point(min,minHrPercent)
        val avgPoint = HeartRateConvertUtils.hearRate2Point(avg,avgHrPercent)

        val maxColor = HeartRateConvertUtils.getColorByPoint(this,maxPoint)
        val minColor = HeartRateConvertUtils.getColorByPoint(this,minPoint)
        val avgColor = HeartRateConvertUtils.getColorByPoint(this,avgPoint)

        //最大
        exerciseMaxTv.setTextColor(maxColor)
        hrBeltMaxTxt.setTextColor(maxColor)
        //最小
        exerciseMinTv.setTextColor(minColor)
        hrBeltMinTxt.setTextColor(minColor)
        //平均
        hrBeltAvgTxt.setTextColor(avgColor)
        exerciseAvgTv.setTextColor(avgColor)

    }

}
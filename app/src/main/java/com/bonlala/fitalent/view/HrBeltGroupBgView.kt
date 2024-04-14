package com.bonlala.fitalent.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.bonlala.fitalent.R
import com.bonlala.fitalent.bean.HrBeltGroupBean
import com.bonlala.widget.utils.MiscUtil
import timber.log.Timber

/**
 * 心率带，倒计时进度条
 * Created by Admin
 *Date 2022/12/9
 */
class HrBeltGroupBgView : View {


    /**背景的画笔 */
    private var bgPaint: Paint? = null

    /**当前进度的画笔 */
    private var currentPaint: Paint? = null

    /**当前进度的颜色**/
    private var currentColor: Int? = null

    /**宽度和高度 */
    private var mWidth = 0f

    /**宽度和高度 */
    private var mHeight: Float = 0f

    /**用户绘制圆角的path**/
    private var path: Path? = null

    /**进度的圆角path**/
    private var schedulePath: Path? = null

    /**背景的颜色集合**/
    private val bgColors = mutableListOf(Color.parseColor("#E3E3ED"), Color.parseColor("#C5C5F1"))

    //中途休息的画笔
    private var restPaint: Paint? = null

    /**当前的进度，动态设置**/
    private var currentSchedule = 0

    //是否需要绘制背景，背景只需要绘制一次即可
    private var isCanvasBg = true


    //数据集合
    private var dataSourceList = mutableListOf<HrBeltGroupBean>()

    /**所以的秒，用于计算单位的长度**/
    private var allCountTime = 0


    private var pathBg: Path? = null
    private var currentPath : Path ?= null



    constructor(context: Context) : super(context) {

    }


    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initAttrs(context, attributeSet)
    }


    constructor(context: Context, attributeSet: AttributeSet, default: Int) : super(
        context,
        attributeSet,
        default
    ) {
        initAttrs(context, attributeSet)
    }


    private fun initAttrs(context: Context, attributeSet: AttributeSet) {
        val typeArray = context.obtainStyledAttributes(attributeSet, R.styleable.HrBeltGroupView)
        currentColor = typeArray.getColor(
            R.styleable.HrBeltGroupView_hr_belt_group_current_color,
            Color.parseColor("#7002DF6D")
        )

        typeArray.recycle()

        initPatin()
    }


    private fun initPatin() {
        bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bgPaint!!.style = Paint.Style.FILL
        bgPaint!!.isAntiAlias = true
        bgPaint!!.strokeWidth = MiscUtil.dipToPx(context, 2f).toFloat()

        currentPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        currentPaint!!.style = Paint.Style.FILL
        currentPaint!!.isAntiAlias = true
        currentPaint!!.strokeWidth = MiscUtil.dipToPx(context, 2f).toFloat()
        currentPaint!!.color = currentColor!!

        restPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        restPaint!!.style = Paint.Style.FILL
        restPaint!!.isAntiAlias = true
        restPaint!!.strokeWidth = MiscUtil.dipToPx(context, 2f).toFloat()
        restPaint!!.color = Color.parseColor("#C5C5F1")

        path = Path()
        schedulePath = Path()

        pathBg = Path()

        currentPath = Path()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = measuredWidth.toFloat()
        mHeight = measuredHeight.toFloat()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Timber.e("-----allCountTime=" + allCountTime + " currentSchedule=" + currentSchedule)
        //没有数据时清零
        if (allCountTime == 0) {
            bgPaint?.let { canvas?.drawRect(RectF(0f, 0f, 0f, 0f), it) }
            return
        }
        canvasView(canvas)
    }

    private var bgRectF : RectF ?= null

    //开始绘制，绘制背景
    private fun canvasView(canvas: Canvas?) {
        //单位长度
        val unitLengthWidth = mWidth / allCountTime
        val radius = mHeight / 2
        //两头圆角
        val radiusRectF = RectF(0f, 0f, mWidth, mHeight)
        path?.addRoundRect(radiusRectF, radius, radius, Path.Direction.CCW)
        if (canvas != null) {
            path?.let { canvas.clipPath(it) }
        }
        //总长度
        var allLength = 0f

        dataSourceList.forEachIndexed { index, hrBeltGroupBean ->

            //每个类型的间隔
            val length = hrBeltGroupBean.endTime - hrBeltGroupBean.startTime
            //总长度
            allLength = (allLength + length * unitLengthWidth)
            val type = hrBeltGroupBean.type
            bgPaint?.color = if (type == 0) bgColors[0] else bgColors[1]

            bgRectF = RectF(allLength - length * unitLengthWidth, 0f, allLength, mHeight)
            //绘制背景
           // bgPaint?.let { canvas?.drawRect(rectF, it) }
            val cPath = Path()
            cPath.addRect(bgRectF!!,Path.Direction.CCW)
            bgPaint?.let { canvas?.drawPath(cPath, it) }

        }


    }


    /**
     * 设置背景
     */
    fun setBackgroundSource(sourceList: MutableList<HrBeltGroupBean>, allTime: Int) {
        dataSourceList.clear()
        allCountTime = allTime
        dataSourceList.addAll(sourceList)
        invalidate()
    }


    /**
     * 绘制进度
     */
    fun setCurrentSchedule(value: Int) {
        currentSchedule = value
        // isCanvasBg = false
        Timber.e("-----setCurrentSchedule=" + currentSchedule)
        postInvalidate()
    }

    /**
     * 获取当前进度
     */
    fun getCurrentSchedule(): Int {
        return currentSchedule
    }

    fun setInitCurrent() {
        allCountTime = 0
        invalidate()
    }

}
package com.bonlala.fitalent.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.bonlala.fitalent.R

/**
 * Created by Admin
 *Date 2022/12/13
 */
class PausePressView : AppCompatImageView {


    private var bgPaint : Paint ?= null
    private var bgCirColor : Int ?= null


    private var paint: Paint? = null
    private var defaultHeight = 0
    private var defaultWidth = 0
    private var progressColor : Int ?= null

    private var rect: RectF? = null

    //倒计时的时长
    private val countDownDuration = 2

    //当前的进度
    private var currentProgress = 0f

    //手是否有释放
    private var isRelease = false

    //是否显示背景圆环
    private var isShowBgCircle = false


    private var onCountDownStateChangeListener : OnCountDownStateChangeListener ?= null


    fun setOnCountDownStateChangeListener(listener: OnCountDownStateChangeListener) {
        this.onCountDownStateChangeListener = listener
    }


    constructor(context: Context) : super(context) {

    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initAttr(context,attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet, default: Int) : super(
        context,
        attributeSet,
        default
    ) {

        initAttr(context,attributeSet)

    }


    private fun initAttr(context: Context,attributeSet: AttributeSet){

        val typeArray = context.obtainStyledAttributes(attributeSet,R.styleable.PausePressView)
        isShowBgCircle = typeArray.getBoolean(R.styleable.PausePressView_is_show_bg_circle,false)
        bgCirColor = typeArray.getColor(R.styleable.PausePressView_press_bg_circle_color,Color.WHITE)
        progressColor = typeArray.getColor(R.styleable.PausePressView_press_progress_color,Color.WHITE)

        typeArray.recycle()
        initPaint(context)
        setPadding(0, 0, 0, 0)
    }

    private fun initPaint(context: Context) {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.color = progressColor!!
        paint!!.style = Paint.Style.STROKE
        paint!!.isAntiAlias = true
        paint!!.strokeCap = Paint.Cap.ROUND
        paint!!.strokeWidth = 10f


        bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bgPaint?.style = Paint.Style.STROKE
        bgPaint?.isAntiAlias = true
        bgPaint?.strokeCap = Paint.Cap.ROUND
        bgPaint?.strokeWidth = 10F
        bgPaint?.color = bgCirColor!!

    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val layoutParams = this.layoutParams;
        if (layoutParams != null) {
            defaultWidth = layoutParams.width;
            defaultHeight = layoutParams.height;
        }
        //获取到图片资源的大小，然后设置大小
        setMeasuredDimension(defaultWidth, defaultHeight);

    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val measuredWidth = measuredWidth
        val measuredHeight = measuredHeight
        rect = RectF(10f, 10f, (measuredWidth - 10).toFloat(), (measuredHeight - 10).toFloat())


        canvas?.drawCircle((measuredWidth!!/2).toFloat(), (measuredHeight!!/2).toFloat(),
            (measuredWidth!!/2-10).toFloat(),bgPaint!!)


        paint!!.color = context.resources.getColor(com.bonlala.base.R.color.transparent)
        //绘制底线
        canvas!!.drawArc(rect!!, -90f, 360f, false, paint!!)
        paint!!.color = progressColor!!
        //绘制前景线
        canvas.drawArc(rect!!, -90f, currentProgress, false, paint!!)

    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.action

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                //开始计时
                this.isRelease = false
                startCountDown()
            }
            MotionEvent.ACTION_UP -> {
                this.isRelease = true
                //告诉外部
                if (currentProgress < 360 && onCountDownStateChangeListener != null) {
                    onCountDownStateChangeListener!!.onCountDownCancel()
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                this.isRelease = true

                //告诉外部
                if (currentProgress < 360 && onCountDownStateChangeListener != null) {
                    onCountDownStateChangeListener!!.onCountDownCancel()
                }
            }

        }

        return true
    }


    /**
     * 开始倒计时
     */
    private fun startCountDown() {
        if (currentProgress >= 360) {
            currentProgress = 0f
        }
        //换算成毫秒
        val duration: Int
        duration = if (!isRelease) {
            countDownDuration * 1000
        } else {
            //往回倒时只要1秒的动画即可
            1000
        }
        //第20毫秒绘制一次进度
        val rate = duration / 20
        //求度数
        val perDegree = 360 * 1.0f / rate
        post(object : Runnable {
            override fun run() {
                if (!isRelease) {
                    currentProgress += perDegree
                } else {
                   // currentProgress -= perDegree
                    currentProgress = 0f
                }
                if (currentProgress < 0) {
                    currentProgress = 0f
                }
                if (currentProgress >= 360) {
                    //结束了
                    currentProgress = 360f
                    //告诉外部
                    onCountDownStateChangeListener?.onCountDownEnd()
                    currentProgress = 0f
                }
                //重新绘制
                invalidate()
                if (currentProgress > 0 && currentProgress < 360) {
                    postDelayed(this, 20)
                }
            }
        })
    }


    interface OnCountDownStateChangeListener {
        //倒计时结束
        fun onCountDownEnd()

        //用户取消倒计时
        fun onCountDownCancel()
    }
}
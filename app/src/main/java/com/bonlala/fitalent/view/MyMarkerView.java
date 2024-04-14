
package com.bonlala.fitalent.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.widget.TextView;

import com.bonlala.fitalent.R;
import com.bonlala.fitalent.bean.ChartHrBean;
import com.bonlala.fitalent.utils.BikeUtils;
import com.bonlala.fitalent.utils.HeartRateConvertUtils;
import com.bonlala.widget.utils.MiscUtil;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

import java.util.List;

import timber.log.Timber;


/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */
@SuppressLint("ViewConstructor")
public class MyMarkerView extends MarkerView {

    private  TextView tvContent;
    private  TextView timeTv;

    private List<ChartHrBean> list;

    private Context context;



    public MyMarkerView(Context context, int layoutResource,List<ChartHrBean> chartHrBeanList) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.tvContent);
        timeTv = findViewById(R.id.hrTimeTv);
        this.list = chartHrBeanList;
        this.context = context;
    }




    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        try {
//            Timber.e("---e="+e.getX()+" "+e.getY()+" ");
            if (e instanceof CandleEntry) {

                CandleEntry ce = (CandleEntry) e;
                int index = (int) e.getX();
                int time = list.get(index).getTime();

                Timber.e("--111--time="+time);
                timeTv.setText(BikeUtils.formatMinuteSleep(time));
                String v = Utils.formatNumber(ce.getHigh(), 0, true);
                int hrValue = Integer.parseInt(v);
                int point = HeartRateConvertUtils.hearRate2Point(hrValue);
                int color = HeartRateConvertUtils.getColorByPoint(context,point);
                tvContent.setText(Utils.formatNumber(ce.getHigh(), 0, true));
                tvContent.setTextColor(color);
            } else {
                int index = (int) e.getX();
                int time = list.get(index).getTime();
                Timber.e("--22--time="+time);
                String v = Utils.formatNumber(e.getY(), 0, true);
                int hrValue = Integer.parseInt(v);

                int point = HeartRateConvertUtils.hearRate2Point(hrValue);
                int color = HeartRateConvertUtils.getColorByPoint(context,point);


                timeTv.setText(BikeUtils.formatMinuteSleep(time));
                tvContent.setText(Utils.formatNumber(e.getY(), 0, true));
                tvContent.setTextColor(color);
            }
        }catch (Exception es){
            es.printStackTrace();
        }


        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
       // Timber.e("-----getHeight="+getHeight());
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }

    @Override
    public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {
        MPPointF offset = getOffset();
//        Chart chart = getChartView();
//        float width = getWidth();
//        float height = getHeight();
//        //如果超过右边界，则向左偏移markerView的宽度
//        if (posX > chart.getWidth() - width) {
//            offset.x = -width;
//        } else {//默认情况，不偏移（因为是点是在左上角）
//            offset.x = 0;
//            if (posX > width / 2) {//如果大于markerView的一半，说明箭头在中间，所以向右偏移一半宽度
//                offset.x = -(width / 2);
//            }
//        }

        return super.getOffsetForDrawingAtPoint(posX,posY);
    }

    @Override
    public void draw(Canvas canvas, float posX, float posY) {

       // Timber.e("--postX="+posX+" "+posY);
        MPPointF mpPointF = getOffsetForDrawingAtPoint(posX,posY);
        float width = getWidth();
        float height = getHeight();
        mpPointF.y = -height;
        mpPointF.x = -width/2;
        //绘制方法是直接复制过来的，没动
        int saveId = canvas.save();

        // translate to the correct position and draw
        float lefWidth = posX + mpPointF.x;
        Timber.e("--------dd="+(posX+mpPointF.x)+" "+posX+" "+mpPointF.x+" "+width+"\n"+(lefWidth+width/2));
        if(lefWidth< -25){
            lefWidth = 0;
        }

        if(lefWidth>0 && lefWidth+width+width/2>=(posX-10f))
            lefWidth = posX-width+width/4;
//        if(lefWidth+getWidth()/2>=(posX-10f)){
//
//            lefWidth = posX-getWidth();
//        }

        canvas.translate(lefWidth, -30f);
        draw(canvas);
        canvas.restoreToCount(saveId);

       // super.draw(canvas, posX, posY);
    }
}

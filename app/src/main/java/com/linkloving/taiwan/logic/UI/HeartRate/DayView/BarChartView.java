package com.linkloving.taiwan.logic.UI.HeartRate.DayView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;


import com.linkloving.taiwan.utils.ViewUtils.ScreenUtils;
import com.linkloving.taiwan.utils.logUtils.MyLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义组件：条形统计图
 */
public class BarChartView extends View {
    public static final String TAG = BarChartView.class.getSimpleName();
    private int screenW, screenH;
    private List<BarChartItemBean> mItems = new ArrayList<BarChartItemBean>();
    //第一个是深色,第二个是total色,第三是画横线
    private int[] mBarColors = new int[]{Color.rgb(26, 43, 95), Color.rgb(38, 174, 222), Color.rgb(217, 173, 18)};
    private Paint barPaint, linePaint, textPaint;
    private Rect barRect;
    private int lineStrokeWidth;
    private double barItemWidth, barSpace, oneHourHight;
    private Rect deepSleepRect;
    private Paint deepSleepPaint;
    private SimpleDateFormat simpleDateFormat;
    private final static long ONE_DAY_MILLISECOND = 86400 ;
    private final static long EIGHT_HOUR_SECONDS  =28800 ;
    private final static long THIRTY_MINUTES_SECONDS = 2400 ;
    private Paint lineGradientPaint;

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        screenW = ScreenUtils.getScreenW(context);
        screenH = ScreenUtils.getScreenH(context);
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        barPaint = new Paint();
        deepSleepPaint = new Paint();
        barPaint.setColor(mBarColors[0]);
        linePaint = new Paint();
        lineStrokeWidth = ScreenUtils.dp2px(context, 1);
        linePaint.setStrokeWidth(lineStrokeWidth);
        linePaint.setAntiAlias(true);
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        barRect = new Rect(0, 0, 0, 0);
        deepSleepRect = new Rect(0, 0, 0, 0);
        barItemWidth = screenW * 0.09;
        barSpace = screenW * 0.035;
//        设置一个一小时的高度
        oneHourHight = screenH * 0.017;
        lineGradientPaint = new Paint();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.rgb(0, 132, 173));
//        画2 4 6 8 10 线
        linePaint.setColor(mBarColors[2]);
        for (int i = 0 ; i<5;i++){
            canvas.drawLine( (float) (screenW * 0.1), (float) (oneHourHight * (4+6*i)), (float) (screenW ), (float) (oneHourHight * (4+6*i)), linePaint);
            MyLog.e(TAG,"2468横线"+(float) (oneHourHight * (4+6*i)));
//            画竖的小线.
            if(i%2!=0){
                canvas.drawLine((float) (screenW * (0.2 + i * 0.18)), (float) (oneHourHight * 28),(float) (screenW * (0.2 + i * 0.18)),(float) (oneHourHight * 27.5) , linePaint);
            }else{
                canvas.drawLine((float) (screenW * (0.2 + i * 0.18)), (float) (oneHourHight * 28),(float) (screenW * (0.2 + i * 0.18)),(float) (oneHourHight * 27) , linePaint);
            }
        }
        textPaint.setTextSize(ScreenUtils.dp2px(getContext(), 16));
        /** 先调用一下下面这方法将开始时间装换成相应的百分比*/
        transformToPoint();
        for (int i = 0; i < mItems.size(); i++) {
            if (i<mItems.size()-1){
                //大于30分钟就不画线
                if ((mItems.get(i+1).starttime-mItems.get(i).starttime)<THIRTY_MINUTES_SECONDS) {
                    double  startX = (float) (((((double) (mItems.get(i).starttime ) / ONE_DAY_MILLISECOND) * (screenW * 0.72)) + 0.2 * screenW)) ;
                    double startY = (float) (oneHourHight * 28 - (mItems.get(i).itemDeepValue * 1000 / 200 * oneHourHight * 24) / 1000) ;
                    double endX =   (float) (((((double) (mItems.get(i + 1).starttime ) / ONE_DAY_MILLISECOND) * (screenW * 0.72)) + 0.2 * screenW)) ;
                    double endY = (float) (oneHourHight * 28 - (mItems.get(i + 1).itemDeepValue * 1000 / 200 * oneHourHight * 24) / 1000) ;

                    int startColor = switchIntToColor((int) mItems.get(i).itemDeepValue);
                    int endColor = switchIntToColor((int) mItems.get(i + 1).itemDeepValue);
                    Shader mShader = new LinearGradient((int)startX, (int)startY,(int) endX,(int) endY,

                            new int[] { startColor,endColor},

                            null, Shader.TileMode.CLAMP);

                    lineGradientPaint.setShader(mShader);
                    lineGradientPaint.setStrokeWidth(2);
                    /**将开始时间装换成相应的百分比*/
                    canvas.drawLine((float) (((((double) (mItems.get(i).starttime ) / ONE_DAY_MILLISECOND) * (screenW * 0.72)) + 0.2 * screenW)),
                            (float) (oneHourHight * 28 - (mItems.get(i).itemDeepValue * 1000 / 200 * oneHourHight * 24) / 1000),
                            (float) (((((double) (mItems.get(i + 1).starttime ) / ONE_DAY_MILLISECOND) * (screenW * 0.72)) + 0.2 * screenW)),
                            (float) (oneHourHight * 28 - (mItems.get(i + 1).itemDeepValue * 1000 / 200 * oneHourHight * 24) / 1000),
                            lineGradientPaint );
                    mShader = null ;
                }
            }
        }

//        画 2 4 6 8 10H 文字
        String[] twoHour = {"200", "150", "100", "50", "0"};
        textPaint.setColor(Color.WHITE);
        for (int i = 0; i < 5; i++) {
            String typeText = twoHour[i];
            float texttypeStartx = (float) 0.02*screenW;
            float texttypeStarty = (float) (oneHourHight * (4.5+6*i));
            canvas.drawText(typeText, texttypeStartx, texttypeStarty, textPaint);
        }

        String[] week = {"00:00", "06:00", "12:00", "18:00","24:00" };
        for (int i = 0; i < 5; i++) {
            String typeText = week[i];
            float texttypeStartx = (float) (screenW * (0.15 + i * 0.18));
            float texttypeStarty = (float) (oneHourHight * 29.5);
            MyLog.e(TAG,texttypeStartx+"------"+texttypeStarty);
            canvas.drawText(typeText, texttypeStartx, texttypeStarty, textPaint);
        }
    }

    private int switchIntToColor(int value){
        int color = 0 ;
        if (value>130){
            color = Color.rgb(255,0,0);
        }else if (value>100){
            color = Color.rgb(250,238,0);
        }else if (value>80){
            color = Color.rgb(55,216,150);
        }else {
            color = Color.rgb(0,255,255);
        }
        return color;
    }


    private Paint setPaintGradientColor(int x ){
        Paint paint = new Paint();
        Shader mShader = new LinearGradient(0, 0, 999, 999,

                new int[] { Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW },

                null, Shader.TileMode.REPEAT);
        paint.setShader(mShader);
        return paint;
     /*   ////////////////////////////////////////////////////第三列
            *//*
             * LinearGradient shader = new LinearGradient(0, 0, endX, endY, new
             * int[]{startColor, midleColor, endColor},new float[]{0 , 0.5f,
             * 1.0f}, TileMode.MIRROR);
             * 参数一为渐变起初点坐标x位置，参数二为y轴位置，参数三和四分辨对应渐变终点
             * 其中参数new int[]{startColor, midleColor,endColor}是参与渐变效果的颜色集合，
             * 其中参数new float[]{0 , 0.5f, 1.0f}是定义每个颜色处于的渐变相对位置， 这个参数可以为null，如果为null表示所有的颜色按顺序均匀的分布
             *//*
        Shader mShader = new LinearGradient(0, 0, 100, 100,

                new int[] { Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW },

                null, Shader.TileMode.REPEAT);

        // Shader.TileMode三种模式

        // REPEAT:沿着渐变方向循环重复

        // CLAMP:如果在预先定义的范围外画的话，就重复边界的颜色

        // MIRROR:与REPEAT一样都是循环重复，但这个会对称重复

        paint.setShader(mShader);// 用Shader中定义定义的颜色来话
*/

    }



    /**
     * 将时间转化成点
     */
    public  void  transformToPoint(){
        MyLog.e(TAG,"transform执行了");
        for (BarChartItemBean record :mItems) {
//            取出一天的时间是多少。
            record.starttime= (record.starttime-2*EIGHT_HOUR_SECONDS) % ONE_DAY_MILLISECOND;
        }
    }

    public List<BarChartItemBean> getItems() {
        return mItems;
    }

    public void setItems(List<BarChartItemBean> items) {

        this.mItems = items;
        invalidate();
    }

    /**
     * A model class to keep the bar item info.
     */
    public static class BarChartItemBean {
        public long starttime;
        public float itemDeepValue;
        public float itemLightValue;


        public BarChartItemBean(long time, float maxHeartrate, float minHeartrate) {
            this.starttime = time;
            this.itemDeepValue = maxHeartrate;
            this.itemLightValue = minHeartrate;
        }
    }
}
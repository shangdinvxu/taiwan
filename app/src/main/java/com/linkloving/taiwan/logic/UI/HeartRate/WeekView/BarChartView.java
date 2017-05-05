package com.linkloving.taiwan.logic.UI.HeartRate.WeekView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.linkloving.taiwan.R;
import com.linkloving.taiwan.logic.UI.HeartRate.GreendaoUtils;
import com.linkloving.taiwan.utils.ViewUtils.ChartParameter;
import com.linkloving.taiwan.utils.ViewUtils.DetailBitmapCreator;
import com.linkloving.taiwan.utils.ViewUtils.ScreenUtils;
import com.linkloving.taiwan.utils.logUtils.MyLog;
import com.zhy.autolayout.AutoRelativeLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import Trace.GreenDao.heartrate;

/**
 * 自定义组件：条形统计图
 */
public class BarChartView extends RelativeLayout {
    public static final String TAG = BarChartView.class.getSimpleName();
    private int screenW, screenH;
    private List<BarChartItemBean> mItems = new ArrayList<BarChartItemBean>();
    //第一个是深色,第二个是total色,第三是画横线
    private int[] mBarColors = new int[]{Color.rgb(26, 43, 95), Color.rgb(38, 174, 222), Color.WHITE};
    private Paint barPaint, linePaint, textPaint;
    private Rect barRect;
    private int lineStrokeWidth;
    private double barItemWidth, barSpace, oneHourHight;
    private Rect deepSleepRect;
    private Paint deepSleepPaint;
    private final static long ONEDAYMILLISECOND = 86400 ;

    /**
     * 滑动的线
     */
    private Context context;
    private FrameLayout framelayout;
    private GreendaoUtils greendaoUtils;
    private TextView timeView;
    private TextView avgView;
    private TextView maxView;
    private View popupView, pointPopupView;
    ImageView dataView;
    ImageView lineView;
    FrameLayout linearLayout;
    int imageWidth;
    int imageHeight;
    /**
     * 图片生成器
     */
    DetailBitmapCreator detailBitmapCreator2;
    float xScale;
    float yScale;
    /**
     * // 每个像素xxx分钟
     */
    float xlineScale;
    private int i = 0;
    //图表日期
    Date firstSundayOfThisWeek;
    private ArrayList<Float> locationXList;
    private boolean isShowPopupWindow = false;
    public PopupWindow popupWindow = new PopupWindow();
    public PopupWindow pointPopupWindow = new PopupWindow();
    private int locationY = 0;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Paint lineGradientPaint;

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        this.context = context;
        InitDetailChartControl(context);
    }

    /**
     * 初始化图表
     *
     * @param context
     */
    private void InitDetailChartControl(Context context)
    {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        screenW = ScreenUtils.getScreenW(context);
        screenH = ScreenUtils.getScreenH(context);
        View view = inflater.inflate(R.layout.tw_heartrate_chat, this);

        framelayout = (FrameLayout) view.findViewById(R.id.chat);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) framelayout.getLayoutParams();
        layoutParams.topMargin = (int) (screenH*0.05);
        layoutParams.leftMargin= (int) (screenW *0.198);
        layoutParams.width = (int) (screenW *0.724);
        layoutParams.height = (int) (screenH*0.43);
        MyLog.e(TAG, "-----topMargin+"+layoutParams.topMargin+"-----leftMargin+"+layoutParams.leftMargin+"-----width+"+layoutParams.width+"-----height+"+layoutParams.height);
        framelayout.setLayoutParams(layoutParams);
        InitView();
        getViewHigh();
        popupView = LayoutInflater.from(getContext()).inflate(R.layout.tw_heartrate_popupwindow, null);
        pointPopupView = LayoutInflater.from(getContext()).inflate(R.layout.tw_heartrate_point_popupwindow, null);
        timeView = (TextView) popupView.findViewById(R.id.popuptime);
        avgView = (TextView) popupView.findViewById(R.id.avg);
        maxView = (TextView) popupView.findViewById(R.id.max);
        greendaoUtils = new GreendaoUtils(context);
//        设置一个一小时的高度
        oneHourHight = screenH * 0.017;
    }

    private void InitView()
    {
        this.dataView = (ImageView) findViewById(R.id.sleep_chat1);
        linearLayout= (FrameLayout) findViewById(R.id.chat);
        lineView = (ImageView) findViewById(R.id.line_chat);
//        timetv = (TextView) findViewById(R.id.time);
        linearLayout.setOnTouchListener(new OnTouchListenerImpl());
    }

    int point = -1 ;

    private class OnTouchListenerImpl implements OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            MyLog.e(TAG,"onTouch X:"+event.getX());
//            String time = new SimpleDateFormat(ToolKits.DATE_FORMAT_YYYY_MM_DD).format(firstSundayOfThisWeek);
//            //获取今天开始时间的long值
//            long timeLong = TimeUtils.stringToLong(time, "yyyy-MM-dd");
//            MyLog.e(TAG,"firstSundayOfThisWeek:"+timeLong);//图表日期的-6点

            boolean flagShow = false ;
            if(event.getX()> 0 && event.getX()<dataView.getWidth()) {
                for (int i = 0; i < 7; i++) {
                    MyLog.e(TAG+"weekview",(screenW * (i * 0.12 - 0.02))+"      "+ screenW * (0.02 + i * 0.12)+ "          "+event.getX());
                    if ((screenW * (i * 0.12 - 0.005)) < event.getX() && event.getX() < (screenW * (0.005 + i * 0.12))) {
                        point = i;
                        long time = firstSundayOfThisWeek.getTime();
                        time = time + 86400000 * i;
                        List<heartrate> heartrates = searRecord(time);
                        if (heartrates.size() == 0) {
                            MyLog.e(TAG, "heartrates.size()为0");
                            maxView.setText("0");
                            avgView.setText("0");
                        } else {
                            MyLog.e(TAG, "heartrates.size()不为0");
                            int avg = 0 ;
                            int max = 0 ;
                            for (heartrate h :heartrates){
                                avg = h.getAvg()+avg ;
                                max = h.getMax()+max ;
                            }
                            flagShow = true ;
                            maxView.setText(max/heartrates.size()+ "");
                            avgView.setText(avg/heartrates.size() + "");
                        }
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(time + 43200000);
                        Date time1 = calendar.getTime();
                        String format = simpleDateFormat.format(time1);
                        timeView.setText(format);
                        MyLog.e(TAG, "时间是+" + format);
                        MyLog.e(TAG, "point是" + point);
                    }
                }
                moveLineViewWithFinger(lineView,event.getX(),event.getRawX(),flagShow);
            }
            if (event.getAction()==MotionEvent.ACTION_UP){
                i=0;
                popupWindow.dismiss();
                pointPopupWindow.dismiss();
            }
            return true;
        }

        private List<heartrate> searRecord(long nowtime) {
            List<heartrate> heartrates = greendaoUtils.searchOneDay(nowtime,nowtime+86400000);
            return heartrates;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int[] location = new int[2];
        getLocationInWindow(location);
        int i = location[0];
        locationY = location[1];
    }


    public void showPointFirstPopupWindow(int x, int y) {
        pointPopupWindow = new PopupWindow(pointPopupView, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        pointPopupWindow.setTouchable(true);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        pointPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        pointPopupWindow.setClippingEnabled(false);
        pointPopupWindow.showAtLocation(framelayout, Gravity.TOP | Gravity.LEFT, x, y);
    }

    public void showFirstPopupWindow(int x, int y) {
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindow.setClippingEnabled(false);
        popupWindow.showAtLocation(framelayout, Gravity.TOP | Gravity.LEFT, x, y);
    }

    /**
     * 设置View的布局属性，使得view随着手指移动 注意：view所在的布局必须使用RelativeLayout 而且不得设置居中等样式
     *
     * @param view
     * @param rawX
     */
    private void moveLineViewWithFinger(View view, float rawX,float rawleftX ,boolean show) {
        AutoRelativeLayout.LayoutParams layoutParams = (AutoRelativeLayout.LayoutParams) view.getLayoutParams();
        layoutParams.leftMargin = (int) rawX - view.getWidth() / 2;
//        layoutParams.leftMargin = (int) (rawX- screenW*0.15);
        view.setLayoutParams(layoutParams);
            if (show){
                popupWindow.dismiss();
            showFirstPopupWindow((int) rawX+(int)(screenW*0.027),locationY);
                //圆点
                pointPopupWindow.dismiss();
                showPointFirstPopupWindow((int) rawleftX-10,
                        (int) ((oneHourHight * 28 - (mItems.get(point).itemLightValue * 1000 / 200 * oneHourHight * 24) / 1000)+locationY-10));
            }else {
                popupWindow.dismiss();
                pointPopupWindow.dismiss();
            }

    }

    private void initValues()
    {
        initXScale();
        detailBitmapCreator2 = new DetailBitmapCreator(context);
        detailBitmapCreator2.initChartParameter(new ChartParameter(xScale, yScale, imageWidth, imageHeight));
    }
    private void initXScale()
    {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;//用
        // 屏幕的宽度
        imageWidth =dataView.getWidth();
        xScale = (float) imageWidth / (120*24f);// 24小时一屏,120是一个小时的时间片(30s一片)  每个时间片所占的像素
        xlineScale = (60*24f) / (float) imageWidth;// 每个像素xxx分钟
        yScale = imageHeight / 80;
    }
    private void  getViewHigh(){
        final ViewTreeObserver vto = linearLayout.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                imageHeight= linearLayout.getMeasuredHeight();
                int width = getMeasuredWidth();
                initValues();
                MyLog.i("linearLayout获取到的高度:" + imageHeight+"   "+width);
                return true;
            }
        });
    }
    public void initDayIndex( Date firstSundayOfThisWeek)
    {
        dataView.setImageBitmap(null);
        this.firstSundayOfThisWeek = firstSundayOfThisWeek;

    }


    private void init(Context context) {
        screenW = ScreenUtils.getScreenW(context);
        screenH = ScreenUtils.getScreenH(context);
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
        for (int i = 0 ; i<7;i++){
            canvas.drawLine( (float) (screenW * 0.1), (float) (oneHourHight * (4+6*i)), (float) (screenW ), (float) (oneHourHight * (4+6*i)), linePaint);
            MyLog.e(TAG,"2468横线"+(float) (oneHourHight * (4+6*i)));
//            画竖的小线.
            if(i%2!=0){
                canvas.drawLine((float) (screenW * (0.2 + i * 0.12)), (float) (oneHourHight * 28),(float) (screenW * (0.2 + i * 0.12)),(float) (oneHourHight * 27.5) , linePaint);
            }else{
                canvas.drawLine((float) (screenW * (0.2 + i * 0.12)), (float) (oneHourHight * 28),(float) (screenW * (0.2 + i * 0.12)),(float) (oneHourHight * 27) , linePaint);
            }
        }
        textPaint.setTextSize(
                ScreenUtils.dp2px(getContext(), 16));
        /** 先调用一下下面这方法将开始时间装换成相应的百分比*/
//        transformToPoint();
        linePaint.setColor( Color.rgb(217, 173, 18));
        for (int i = 0; i < mItems.size(); i++) {
            if (i<mItems.size()-1){
                if (mItems.get(i).itemDeepValue==0||mItems.get(i+1).itemDeepValue==0) continue;
                double startX =(float) (screenW * (0.2 + i * 0.12));
                double startY =  (float)(oneHourHight*28- (mItems.get(i).itemLightValue*1000/200*oneHourHight * 24)/1000);
                double endX =  (float) (screenW * (0.2 + (i+1) * 0.12));
                double endY =   (float)(oneHourHight*28-(mItems.get(i+1).itemLightValue*1000/200 *oneHourHight * 24)/1000);
                int startColor = switchIntToColor((int) mItems.get(i).itemDeepValue);
                int endColor = switchIntToColor((int) mItems.get(i + 1).itemDeepValue);
                Shader mShader = new LinearGradient((int) startX, (int) startY, (int) endX, (int) endY,

                        new int[]{startColor, endColor},

                        null, Shader.TileMode.CLAMP);
                lineGradientPaint.setShader(mShader);
                lineGradientPaint.setStrokeWidth(2);
                lineGradientPaint.setAntiAlias(true);
                canvas.drawLine( (float) (screenW * (0.2 + i * 0.12)),
                            (float)(oneHourHight*28- (mItems.get(i).itemLightValue*1000/200*oneHourHight * 24)/1000),
                            (float) (screenW * (0.2 + (i+1) * 0.12)),
                            (float)(oneHourHight*28-(mItems.get(i+1).itemLightValue*1000/200 *oneHourHight * 24)/1000),
                        lineGradientPaint);
                mShader = null;
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

        String[] week = {"S", "M", "T", "W","T","F","S"};
        for (int i = 0; i < 7; i++) {
            String typeText = week[i];
            float texttypeStartx = (float) (screenW * (0.19 + i * 0.12));
            float texttypeStarty = (float) (oneHourHight * 29.5);
            MyLog.e(TAG,texttypeStartx+"------"+texttypeStarty);
            canvas.drawText(typeText, texttypeStartx, texttypeStarty, textPaint);
        }
    }


    private int switchIntToColor(int value) {
        int color = 0;
        if (value > 130) {
            color = Color.rgb(255, 0, 0);
        } else if (value > 100) {
            color = Color.rgb(250, 238, 0);
        } else if (value > 80) {
            color = Color.rgb(55, 216, 150);
        } else {
            color = Color.rgb(0, 255, 255);
        }
        return color;
    }

    /**
     * 将时间转化成点
     */
    public  void  transformToPoint(){
        MyLog.e(TAG,"transform执行了");
        for (BarChartItemBean record :mItems) {
            long l = record.starttime % ONEDAYMILLISECOND;
             float per= (float)(l+28800) / ONEDAYMILLISECOND;
            MyLog.e(TAG,"per是.........."+per);
            record.starttime=l ;
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
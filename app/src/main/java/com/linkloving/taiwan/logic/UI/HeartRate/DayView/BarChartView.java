package com.linkloving.taiwan.logic.UI.HeartRate.DayView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.provider.ContactsContract;
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


import com.example.android.bluetoothlegatt.BLEProvider;
import com.linkloving.taiwan.R;
import com.linkloving.taiwan.logic.UI.HeartRate.GreendaoUtils;
import com.linkloving.taiwan.utils.ToolKits;
import com.linkloving.taiwan.utils.ViewUtils.ChartParameter;
import com.linkloving.taiwan.utils.ViewUtils.DetailBitmapCreator;
import com.linkloving.taiwan.utils.ViewUtils.ScreenUtils;
import com.linkloving.taiwan.utils.logUtils.MyLog;
import com.linkloving.taiwan.utils.sportUtils.TimeUtils;
import com.zhy.autolayout.AutoRelativeLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import Trace.GreenDao.heartrate;

import static com.linkloving.taiwan.R.id.sleep_chat1;

/**
 * 自定义组件：条形统计图
 */
public class BarChartView extends RelativeLayout {
    public static final String TAG = BarChartView.class.getSimpleName();
    private int screenW, screenH;
    private List<BarChartItemBean> mItems = new ArrayList<BarChartItemBean>();
    //第一个是深色,第二个是total色,第三是画横线
    private int[] mBarColors = new int[]{Color.rgb(26, 43, 95), Color.rgb(38, 174, 222), Color.WHITE};
    //    private int[] mBarColors = new int[]{Color.rgb(26, 43, 95), Color.rgb(38, 174, 222), Color.rgb(217, 173, 18)};
    private Paint barPaint, linePaint, textPaint;
    private Rect barRect;
    private int lineStrokeWidth;
    private double barItemWidth, barSpace, oneHourHight;
    private Rect deepSleepRect;
    private Paint deepSleepPaint;
    private SimpleDateFormat simpleDateFormat;
    private final static long ONE_DAY_MILLISECOND = 86400;
    private final static long EIGHT_HOUR_SECONDS = 28800;
    private final static long THIRTY_MINUTES_SECONDS = 2400;
    private Paint lineGradientPaint;


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
    Date chartDate;
    private ArrayList<Float> locationXList;
    private boolean isShowPopupWindow = false;
    public PopupWindow popupWindow = new PopupWindow();
    public PopupWindow pointPopupWindow = new PopupWindow();
    private int locationY = 0;

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        this.context = context;
        InitDetailChartControl(context);
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


    private void InitDetailChartControl(Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        screenW = ScreenUtils.getScreenW(context);
        screenH = ScreenUtils.getScreenH(context);
        View view = inflater.inflate(R.layout.tw_heartrate_chat, this);

        framelayout = (FrameLayout) view.findViewById(R.id.chat);
        ImageView referView = (ImageView) view.findViewById(sleep_chat1);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) framelayout.getLayoutParams();
        layoutParams.topMargin = (int) (screenH * 0.05);
        layoutParams.leftMargin = (int) (screenW * 0.198);
        layoutParams.width = (int) (screenW * 0.727);
        layoutParams.height = (int) (screenH * 0.43);
        MyLog.e(TAG, "-----topMargin+" + layoutParams.topMargin + "-----leftMargin+" + layoutParams.leftMargin + "-----width+" + layoutParams.width + "-----height+" + layoutParams.height);
        framelayout.setLayoutParams(layoutParams);
        InitView();
        getViewHigh();
        popupView = LayoutInflater.from(getContext()).inflate(R.layout.tw_heartrate_popupwindow, null);
        pointPopupView = LayoutInflater.from(getContext()).inflate(R.layout.tw_heartrate_point_popupwindow, null);
        timeView = (TextView) popupView.findViewById(R.id.popuptime);
        avgView = (TextView) popupView.findViewById(R.id.avg);
        maxView = (TextView) popupView.findViewById(R.id.max);
        greendaoUtils = new GreendaoUtils(context);
        oneHourHight = screenH * 0.017;
    }

    private void InitView() {
        this.dataView = (ImageView) findViewById(sleep_chat1);
        linearLayout = (FrameLayout) findViewById(R.id.chat);
        lineView = (ImageView) findViewById(R.id.line_chat);
//        timetv = (TextView) findViewById(R.id.time);
        linearLayout.setOnTouchListener(new OnTouchListenerImpl());
    }

    private class OnTouchListenerImpl implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            MyLog.e(TAG, "onTouch X:" + event.getX());
            String time = new SimpleDateFormat(ToolKits.DATE_FORMAT_YYYY_MM_DD).format(chartDate);
            //获取今天开始时间的long值
            long timeLong = TimeUtils.stringToLong(time, "yyyy-MM-dd");
            MyLog.e(TAG, "chartDate:" + timeLong);//图表日期的-6点
            if (event.getX() > 0 && event.getX() < dataView.getWidth()) {
                moveLineViewWithFinger(lineView, event.getX(), event.getRawX(), 0, true);
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                i = 0;
                popupWindow.dismiss();
                pointPopupWindow.dismiss();
            }
            return true;
        }


    }

    /**
     * 设置View的布局属性，使得view随着手指移动 注意：view所在的布局必须使用RelativeLayout 而且不得设置居中等样式
     *
     * @param view
     * @param rawX
     */
    private void moveLineViewWithFinger(View view, float rawX, float rawleftX, int y, boolean show) {
        AutoRelativeLayout.LayoutParams layoutParams = (AutoRelativeLayout.LayoutParams) view.getLayoutParams();
        layoutParams.leftMargin = (int) rawX - view.getWidth() / 2;
        MyLog.e(TAG, layoutParams.leftMargin + "          " + layoutParams.topMargin);
        view.setLayoutParams(layoutParams);
        isShowPopupWindow = false;
        int listPostion = 0;
        for (int i = 0; i < locationXList.size(); i++) {
            if (rawX < (locationXList.get(i) - screenW * 0.2 + 4) && rawX > (locationXList.get(i) - screenW * 0.2 - 1)) {
                isShowPopupWindow = true;
                listPostion = i;
                MyLog.e(TAG+"true", "isShowPopupWindow = true");
                break;
            }
        }
        MyLog.e(TAG+"true", "下面的执行");
        if (isShowPopupWindow) {
            maxView.setText((int) mItems.get(listPostion).itemLightValue + "");
            avgView.setText((int) mItems.get(listPostion).itemDeepValue + "");
            String nowtimeString = TimeUtils.formatTimeHHMM(mItems.get(listPostion).starttime + 16 * 3600);
            timeView.setText(nowtimeString);
            popupWindow.dismiss();
            showFirstPopupWindow((int) rawX + (int) (screenW * 0.027), locationY);
            //圆点
            pointPopupWindow.dismiss();
            showPointFirstPopupWindow((int) rawleftX - 10,
                    (int) ((oneHourHight * 28 - (mItems.get(listPostion).itemDeepValue * 1000 / 200 * oneHourHight * 24) / 1000)) + locationY - 10);
        } else {
            popupWindow.dismiss();
            pointPopupWindow.dismiss();
        }
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

    private void getViewHigh() {
        final ViewTreeObserver vto = linearLayout.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                imageHeight = linearLayout.getMeasuredHeight();
                int width = getMeasuredWidth();
                initValues();
                MyLog.i("linearLayout获取到的高度:" + imageHeight + "   " + width);
                return true;
            }
        });
    }


    private void initValues() {
        initXScale();
        detailBitmapCreator2 = new DetailBitmapCreator(context);
        detailBitmapCreator2.initChartParameter(new ChartParameter(xScale, yScale, imageWidth, imageHeight));
    }

    private void initXScale() {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;//用屏幕的宽度
        imageWidth = dataView.getWidth();
        xScale = (float) imageWidth / (120 * 24f);// 24小时一屏,120是一个小时的时间片(30s一片)  每个时间片所占的像素
        xlineScale = (60 * 24f) / (float) imageWidth;// 每个像素xxx分钟
        yScale = imageHeight / 80;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.rgb(0, 132, 173));
//        画2 4 6 8 10 线
        linePaint.setColor(mBarColors[2]);
        for (int i = 0; i < 5; i++) {
            canvas.drawLine((float) (screenW * 0.1), (float) (oneHourHight * (4 + 6 * i)), (float) (screenW), (float) (oneHourHight * (4 + 6 * i)), linePaint);
            MyLog.e(TAG, "2468横线" + (float) (oneHourHight * (4 + 6 * i)));
//            画竖的小线.
            if (i % 2 != 0) {
                canvas.drawLine((float) (screenW * (0.2 + i * 0.18)), (float) (oneHourHight * 28), (float) (screenW * (0.2 + i * 0.18)), (float) (oneHourHight * 27.5), linePaint);
            } else {
                canvas.drawLine((float) (screenW * (0.2 + i * 0.18)), (float) (oneHourHight * 28), (float) (screenW * (0.2 + i * 0.18)), (float) (oneHourHight * 27), linePaint);
            }
        }
        textPaint.setTextSize(ScreenUtils.dp2px(getContext(), 16));
        /** 先调用一下下面这方法将开始时间装换成相应的百分比*/
        for (int i = 0; i < mItems.size(); i++) {
            if (i < mItems.size() - 1) {
                //大于30分钟就不画线
                if ((mItems.get(i + 1).starttime - mItems.get(i).starttime) < THIRTY_MINUTES_SECONDS && (mItems.get(i + 1).starttime - mItems.get(i).starttime) > 59) {
                    double startX = (float) (((((double) (mItems.get(i).starttime) / ONE_DAY_MILLISECOND) * (screenW * 0.72)) + 0.2 * screenW));
                    double startY = (float) (oneHourHight * 28 - (mItems.get(i).itemDeepValue * 1000 / 200 * oneHourHight * 24) / 1000);
                    double endX = (float) (((((double) (mItems.get(i + 1).starttime) / ONE_DAY_MILLISECOND) * (screenW * 0.72)) + 0.2 * screenW));
                    double endY = (float) (oneHourHight * 28 - (mItems.get(i + 1).itemDeepValue * 1000 / 200 * oneHourHight * 24) / 1000);

                    int startColor = switchIntToColor((int) mItems.get(i).itemDeepValue);
                    int endColor = switchIntToColor((int) mItems.get(i + 1).itemDeepValue);
                    Shader mShader = new LinearGradient((int) startX, (int) startY, (int) endX, (int) endY,

                            new int[]{startColor, endColor},

                            null, Shader.TileMode.CLAMP);
                    lineGradientPaint.setShader(mShader);
                    lineGradientPaint.setStrokeWidth(2);
                    lineGradientPaint.setAntiAlias(true);
                    /**将开始时间装换成相应的百分比*/
                    canvas.drawLine((float) (((((double) (mItems.get(i).starttime) / ONE_DAY_MILLISECOND) * (screenW * 0.72)) + 0.2 * screenW)),
                            (float) (oneHourHight * 28 - (mItems.get(i).itemDeepValue * 1000 / 200 * oneHourHight * 24) / 1000),
                            (float) (((((double) (mItems.get(i + 1).starttime) / ONE_DAY_MILLISECOND) * (screenW * 0.72)) + 0.2 * screenW)),
                            (float) (oneHourHight * 28 - (mItems.get(i + 1).itemDeepValue * 1000 / 200 * oneHourHight * 24) / 1000),
                            lineGradientPaint);
                    mShader = null;
                }
            }
        }

//        画 2 4 6 8 10H 文字
        String[] twoHour = {"200", "150", "100", "50", "0"};
        textPaint.setColor(Color.WHITE);
        for (int i = 0; i < 5; i++) {
            String typeText = twoHour[i];
            float texttypeStartx = (float) 0.02 * screenW;
            float texttypeStarty = (float) (oneHourHight * (4.5 + 6 * i));
            canvas.drawText(typeText, texttypeStartx, texttypeStarty, textPaint);
        }

        String[] week = {"00:00", "06:00", "12:00", "18:00", "24:00"};
        for (int i = 0; i < 5; i++) {
            String typeText = week[i];
            float texttypeStartx = (float) (screenW * (0.15 + i * 0.18));
            float texttypeStarty = (float) (oneHourHight * 29.5);
            MyLog.e(TAG, texttypeStartx + "------" + texttypeStarty);
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
    public void transformToPoint() {
        if (mItems != null && mItems.size() > 1) {
            //如果获取的时间取到第二天的开始时间了，就把最后一条数据去掉
            if ((mItems.get(mItems.size() - 1).starttime - mItems.get(0).starttime) > 80000) {
                mItems.remove(mItems.size() - 1);
            }
        }
        for (BarChartItemBean record : mItems) {
//            取出一天的时间是多少。
            Date date = new Date();
            date.setTime(record.starttime * 1000);
            record.starttime = date.getHours() * 3600 + date.getMinutes() * 60 + date.getSeconds();
            date = null;

//            record.starttime= (record.starttime-2*EIGHT_HOUR_SECONDS) % ONE_DAY_MILLISECOND;
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

    public List<BarChartItemBean> getItems() {
        return mItems;
    }


    public void setItems(List<BarChartItemBean> items) {

        this.mItems = items;
        //        将时间变成的几点几分。
        transformToPoint();
        invalidate();
        locationXList = new ArrayList<>();
        for (BarChartItemBean itemBean : items) {
            locationXList.add(
                    (float) (((((double) (itemBean.starttime) / ONE_DAY_MILLISECOND) * (screenW * 0.72)) + 0.2 * screenW)));
            MyLog.e(TAG, (float) (((((double) (itemBean.starttime) / ONE_DAY_MILLISECOND) * (screenW * 0.72)) + 0.2 * screenW)) + "list 的 位置");
        }
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

    public void initDayIndex(Date date) {
        dataView.setImageBitmap(null);
        chartDate = date;
    }
}
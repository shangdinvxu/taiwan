package com.linkloving.taiwan.logic.UI.main.datachatactivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.linkloving.band.dto.SportRecord;
import com.linkloving.band.ui.BRDetailData;
import com.linkloving.taiwan.R;
import com.linkloving.taiwan.utils.logUtils.MyLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leo.wang on 2016/4/12.
 */
public class DetailChartControl extends RelativeLayout {
    //有两个textview,用来显示开始和结束时间
    ///还有一个 LinearLayout
    ImageView imageView1;
    RelativeLayout linearLayout;
    Context context;
    int imageWidth;
    int imageHeight;
    /** 每个时间片对应像素宽度（px/片) */
    float xScale;
    float yScale;
    /** 图片生成器 */
    DetailBitmapCreator detailBitmapCreator2;
    // !!!!!!!!!!!!
    private ArrayList<BRDetailData> detailDatas = new ArrayList<BRDetailData>();

    private List<SportRecord> sportRecords=new ArrayList<>();

    int dayindexNow;
    String timeNow;

    public DetailChartControl(Context context) {
        super(context);
        InitDetailChartControl(context);
    }
    public DetailChartControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        InitDetailChartControl(context);
    }
    public DetailChartControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
        inflater.inflate(R.layout.sleepchat, this);
        InitView();
        getViewHigh();

    }
    private void InitView()
    {
        this.imageView1 = (ImageView) findViewById(R.id.sleep_chat1);
        linearLayout= (RelativeLayout) findViewById(R.id.chat);
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
        int width = dm.widthPixels;//用屏幕的宽度
        imageWidth = width;
        xScale = (float) width / (120*30f);// 30小时一屏,120是一个小时的时间片(30s一片)  每个时间片所占的像素

        yScale = imageHeight / 80;
    }
        private void  getViewHigh(){
        final ViewTreeObserver vto = linearLayout.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                imageHeight= linearLayout.getMeasuredHeight();
                int width = DetailChartControl.this.getMeasuredWidth();
                initValues();
                MyLog.i("linearLayout获取到的高度:" + imageHeight+"   "+width);
                return true;
            }
        });
    }
    /**
     * 添加图表新条目（分为 左边添加 和 右边添加 两种）
     * @param
     * @return
     */
    private void AsyncAddDetailChart()
    {
        new AsyncTask<Void,Void,List<Bitmap>>(){
            @Override
            protected List<Bitmap> doInBackground(Void... params) {
                List<Bitmap> bitmaps=new ArrayList<Bitmap>();
                Bitmap bitmapChart2=detailBitmapCreator2.drawDetailChart(detailDatas,dayindexNow);
                bitmaps.add(bitmapChart2);
                return bitmaps;
            }
            @Override
            protected void onPostExecute(List<Bitmap> bitmaps) {
                imageView1.setImageBitmap(bitmaps.get(0));
            }
        }.execute();
    }


    public void initDayIndex(ArrayList<BRDetailData> list,int dayindex)
    {
        imageView1.setImageBitmap(null);

        detailDatas=list;
        dayindexNow=dayindex;

        if(detailDatas!=null && detailDatas.size()>0)
            AsyncAddDetailChart();
    }

    /**
     * @param list
     * @param startdata
     */
    /** @deprecated */
    public void initDayIndex(List<SportRecord> list,String startdata)
    {
        imageView1.setImageBitmap(null);
        sportRecords=list;
        timeNow=startdata;
        if(sportRecords!=null && sportRecords.size()>0)
            AsyncAddsportRecordDetailChart();
    }
    /**
     * 添加图表新条目（分为 左边添加 和 右边添加 两种）
     */
    /** @deprecated */
    private void AsyncAddsportRecordDetailChart()
    {
        new AsyncTask<Void,Void,List<Bitmap>>(){
            @Override
            protected List<Bitmap> doInBackground(Void... params) {
                List<Bitmap> bitmaps=new ArrayList<Bitmap>();
                Bitmap bitmapChart2=detailBitmapCreator2.drawDetailChart1(sportRecords,timeNow);
                bitmaps.add(bitmapChart2);
                return bitmaps;
            }
            @Override
            protected void onPostExecute(List<Bitmap> bitmaps) {
                imageView1.setImageBitmap(bitmaps.get(0));
            }
        }.execute();
    }



}

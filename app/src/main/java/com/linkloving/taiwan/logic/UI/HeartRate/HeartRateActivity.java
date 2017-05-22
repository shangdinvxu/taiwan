package com.linkloving.taiwan.logic.UI.HeartRate;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Scroller;
import android.widget.TextView;

import com.linkloving.taiwan.MyApplication;
import com.linkloving.taiwan.R;
import com.linkloving.taiwan.basic.toolbar.ToolBarActivity;
import com.linkloving.taiwan.logic.UI.HeartRate.DayView.BarChartView;
import com.linkloving.taiwan.logic.dto.UserEntity;
import com.linkloving.taiwan.utils.DateSwitcher;
import com.linkloving.taiwan.utils.ToolKits;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Trace.GreenDao.DaoMaster;
import Trace.GreenDao.heartrate;
import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Daniel.Xu on 2016/10/15.
 */

public class HeartRateActivity extends ToolBarActivity implements View.OnClickListener {
    private static final String TAG = HeartRateActivity.class.getSimpleName();
    @InjectView(R.id.groups_time)
    TextView groupsTime;
    @InjectView(R.id.Heartrate_day)
    RadioButton HeartrateDay;
    @InjectView(R.id.Heartrate_week)
    RadioButton HeartrateWeek;
    @InjectView(R.id.Heartrate_month)
    RadioButton HeartrateMonth;
    @InjectView(R.id.tw_Heartrate_buttom)
    RadioGroup twHeartrateButtom;
    @InjectView(R.id.resting)
    TextView restingText;
    @InjectView(R.id.avgerage)
    TextView avgerageText;
    @InjectView(R.id.middle_framelayout)
    FrameLayout middleFramelayout;
    @InjectView(R.id.timeToday)
    TextView timeToday;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd",Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tw_heartrate);
        ButterKnife.inject(this);

        HeartrateDay.setOnClickListener(this);
        HeartrateMonth.setOnClickListener(this);
        HeartrateWeek.setOnClickListener(this);
        initDay();
        UserEntity localUserInfoProvider = MyApplication.getInstance(HeartRateActivity.this).getLocalUserInfoProvider();
        String birthdate = localUserInfoProvider.getUserBase().getBirthdate();
        String[] split = birthdate.split("-");
        int i = Integer.parseInt(split[0]);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int resting = (int) ((220 - (year - i)) * 0.4);
        restingText.setText(resting + "");
        //中间的时间和心率的时间
        initTimeToday();
        //显示今天的平均心率。
        initAvgHeartrate();
//        initTestData();
    }

    /**
     * 插入测试数据供测试使用
     */
    private void initTestData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DaoMaster.DevOpenHelper heartrateHelper = new DaoMaster.DevOpenHelper(HeartRateActivity.this, "heartrate", null);
                SQLiteDatabase readableDatabase = heartrateHelper.getReadableDatabase();
                GreendaoUtils greendaoUtils = new GreendaoUtils(HeartRateActivity.this, readableDatabase);
                greendaoUtils.deleteAll();
                for (int i = 0; i < 10000; i++) {
                    greendaoUtils.addwhole(1492235115 + i * 60, (int) (Math.random()*100),  (int) (Math.random()*200),
                    (int) (Math.random()*100),  (int) (Math.random()*200));
                }
            }
        }).start();
    }

    private void initAvgHeartrate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        long dayStart = calendar.getTime().getTime();
        System.out.println("开始时间："+calendar.getTime());
        calendar.set(Calendar.HOUR,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        calendar.set(Calendar.MILLISECOND,999);
        long dayEnd = calendar.getTime().getTime();
        System.out.println("结束时间："+calendar.getTime());
        DaoMaster.DevOpenHelper heartrateHelper = new DaoMaster.DevOpenHelper(HeartRateActivity.this, "heartrate", null);
        SQLiteDatabase readableDatabase = heartrateHelper.getReadableDatabase();
        GreendaoUtils greendaoUtils = new GreendaoUtils(HeartRateActivity.this, readableDatabase);
        List<heartrate> heartrates = greendaoUtils.searchOneDay(dayStart, dayEnd);
        ArrayList<BarChartView.BarChartItemBean> list = new ArrayList<>();
        int rest = 0 ;
        int avg = 0 ;
        for (heartrate record : heartrates){
            BarChartView.BarChartItemBean barChartItemBean = new BarChartView.BarChartItemBean
                    (record.getStartTime(), record.getMax(), record.getAvg());
            if (record.getMax()>200)continue;
            list.add(barChartItemBean);
            rest = rest+record.getMax();
            avg = avg+record.getAvg();
        }
        int resting,avging = 0;
        if (list.size()==0){
            resting = 0 ;
            avging = 0 ;
        }else{
            resting = rest / list.size();
            avging = avg/list.size();
        }
        setAvgText( avging) ;
    }

    private void initTimeToday(){
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int calendarMonth = calendar.get(Calendar.MONTH);
        String s = DateSwitcher.monthSwitch(calendarMonth);
        String dateformat = dateFormat.format(date);
        timeToday.setText(s+" "+dateformat);
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd a HH:mm", Locale.US);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String format = simpleDateFormat.format(date);
        groupsTime.setText(format);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Heartrate_day:
                restartBotton();
                initDay();
                break;
            case R.id.Heartrate_week:
                restartBotton();
                initWeek();
                break;
            case R.id.Heartrate_month:
                restartBotton();
                initMonth();
                break;
        }

    }

    private void initMonth() {
        MonthDatefragment monthDatefragment = new MonthDatefragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.middle_framelayout, monthDatefragment);
        fragmentTransaction.commit();
   /*     MonthFragment monthFragment = new MonthFragment();
        monthFragment.setRestingBpmListener(new RestingBpm() {
            @Override
            public void setAvg(int average) {
                avgerageText.setText(average+"");
            }
        });*/
        monthDatefragment.setDataChangeListener(new IDataChangeListener() {
            @Override
            public void onDataChange(String data) {
                groupsTime.setText(data);
                timeToday.setText(data);
            }
        });

    }

    private void initWeek() {
        WeekDatefragment weekDatefragment = new WeekDatefragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.middle_framelayout, weekDatefragment);
        fragmentTransaction.commit();
 /*       WeekFragment weekFragment = new WeekFragment();
    weekFragment.setRestingBpmListener(new RestingBpm() {
        @Override
        public void setAvg(int average) {
            avgerageText.setText(average+"");
        }
    });*/
        weekDatefragment.setDataChangeListener(new IDataChangeListener() {
            @Override
            public void onDataChange(String data) {
                Log.e(TAG, "dateStr-------" + data);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                Date mondayOfThisWeek = null;
                Date sundayofThisWeek = null;
                try {
                    mondayOfThisWeek = ToolKits.getFirstSundayOfThisWeek(sdf.parse(data));
                    sundayofThisWeek = ToolKits.getStaurdayofThisWeek(sdf.parse(data));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat simYearMonth = new SimpleDateFormat("MM/dd");
                String startData = sdf.format(mondayOfThisWeek);
                String endData = simYearMonth.format(sundayofThisWeek);
                String monday = simYearMonth.format(mondayOfThisWeek);
                groupsTime.setText(startData + " - " + endData);
                timeToday.setText(monday + " - " + endData);
            }
        });
    }

    private void initDay() {
        DayDatefragment dayDatefragment = new DayDatefragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.middle_framelayout, dayDatefragment);
        fragmentTransaction.commit();
        dayDatefragment.setDataChangeListener(new IDataChangeListener() {
            @Override
            public void onDataChange(String data) {
                groupsTime.setText(data);
                timeToday.setText(data);
            }
        });
      /*  DayFragment dayFragment = new DayFragment();
        dayFragment.setRestingBpmListener(new RestingBpm() {
            @Override
            public void setAvg(int average) {
                avgerageText.setText(average+"");
            }
        });*/

    }


    /**
     * 切换Fragment时候 重置按钮图片
     */
    private void restartBotton() {
//        HeartrateDay.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.mipmap.d_off_72px, 0, 0);
//        HeartrateWeek.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.mipmap.w_off_72px, 0, 0);
//        HeartrateMonth.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.mipmap.m_off_72px, 0, 0);
    }

    @Override
    protected void getIntentforActivity() {
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void initListeners() {
    }

    public void setAvgText(int avging) {
        if (avging==0){
            avgerageText.setText("--");
        }else {
            avgerageText.setText(avging + "");
        }
    }

}

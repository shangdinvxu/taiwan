package com.linkloving.taiwan.logic.UI.main;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.android.bluetoothlegatt.BLEHandler;
import com.example.android.bluetoothlegatt.BLEProvider;
import com.example.android.bluetoothlegatt.proltrol.dto.LPDeviceInfo;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.linkloving.band.dto.DaySynopic;
import com.linkloving.band.dto.SportRecord;
import com.linkloving.taiwan.BleService;
import com.linkloving.taiwan.CommParams;
import com.linkloving.taiwan.IntentFactory;
import com.linkloving.taiwan.MyApplication;
import com.linkloving.taiwan.R;
import com.linkloving.taiwan.basic.AppManager;
import com.linkloving.taiwan.basic.CustomProgressBar;
import com.linkloving.taiwan.db.sport.UserDeviceRecord;
import com.linkloving.taiwan.db.summary.DaySynopicTable;
import com.linkloving.taiwan.db.weight.UserWeight;
import com.linkloving.taiwan.db.weight.WeightTable;
import com.linkloving.taiwan.http.basic.CallServer;
import com.linkloving.taiwan.http.basic.HttpCallback;
import com.linkloving.taiwan.http.basic.NoHttpRuquestFactory;
import com.linkloving.taiwan.http.data.DataFromServer;
import com.linkloving.taiwan.logic.UI.HeartRate.DayView.BarChartView;
import com.linkloving.taiwan.logic.UI.HeartRate.GreendaoUtils;
import com.linkloving.taiwan.logic.UI.HeartRate.HeartRateActivity;
import com.linkloving.taiwan.logic.UI.customerservice.serviceItem.Feedback;
import com.linkloving.taiwan.logic.UI.device.DeviceActivity;
import com.linkloving.taiwan.logic.UI.device.FirmwareDTO;
import com.linkloving.taiwan.logic.UI.main.boundwatch.BoundActivity;
import com.linkloving.taiwan.logic.UI.main.bundband.Band3ListActivity;
import com.linkloving.taiwan.logic.UI.main.bundband.BandListActivity;
import com.linkloving.taiwan.logic.UI.main.datachatactivity.WeightActivity;
import com.linkloving.taiwan.logic.UI.main.materialmenu.Left_viewVO;
import com.linkloving.taiwan.logic.UI.main.materialmenu.MenuNewAdapter;
import com.linkloving.taiwan.logic.UI.main.materialmenu.MenuVO;
import com.linkloving.taiwan.logic.UI.main.update.UpdateClientAsyncTask;
import com.linkloving.taiwan.logic.dto.SportRecordUploadDTO;
import com.linkloving.taiwan.logic.dto.UserEntity;
import com.linkloving.taiwan.notify.NotificationCollectorService;
import com.linkloving.taiwan.prefrences.PreferencesToolkits;
import com.linkloving.taiwan.prefrences.devicebean.LocalInfoVO;
import com.linkloving.taiwan.prefrences.devicebean.ModelInfo;
import com.linkloving.taiwan.utils.CommonUtils;
import com.linkloving.taiwan.utils.SwitchUnit;
import com.linkloving.taiwan.utils.ToolKits;
import com.linkloving.taiwan.utils.UnitTookits;
import com.linkloving.taiwan.utils.logUtils.MyLog;
import com.linkloving.taiwan.utils.manager.AsyncTaskManger;
import com.linkloving.taiwan.utils.sportUtils.SportDataHelper;
import com.linkloving.taiwan.utils.sportUtils.TimeUtils;
import com.linkloving.utils.TimeZoneHelper;
import com.linkloving.utils._Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.yolanda.nohttp.Response;
import com.zhy.autolayout.AutoLayoutActivity;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import Trace.GreenDao.DaoMaster;
import Trace.GreenDao.heartrate;

public class PortalActivity extends AutoLayoutActivity implements MenuNewAdapter.OnRecyclerViewListener, View.OnClickListener {

    private SimpleDateFormat sdf = new SimpleDateFormat(ToolKits.DATE_FORMAT_YYYY_MM_DD);
    private static final String TAG = PortalActivity.class.getSimpleName();
    private static final int REQUSET_FOR_PERSONAL = 1;
    private static final int LOW_BATTERY = 3;
    private static final int JUMP_FRIEND_TAG_TWO = 2;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private RecyclerView menu_RecyclerView;
    private MenuNewAdapter menuAdapter;
    //头像  电量
    private ImageView user_head, device_img;
    //昵称
    private TextView user_name;
    private TextView text_Battery, text_Wallet, text_Step, text_Distance, text_Cal, text_Run, text_Sleep, text_Weight,text_heart,
            text_Battery_Progress, text_Wallet_Progress, text_Step_Progress, text_Distance_Progress, text_Cal_Progress, text_Run_Progress, text_Sleep_Progress, text_Weight_Progress;
    private CustomProgressBar Battery_ProgressBar,Wallet_ProgressBar, Step_ProgressBar,
            Distance_ProgressBar, Cal_ProgressBar, Run_ProgressBar, Sleep_ProgressBar, Weight_ProgressBar;
    private LinearLayout date, linear_unbund, linear_Step, linear_Distance, linear_Cal,linear_heartrate, linear_Run, linear_Sleep, linear_Battery, linear_wallet, linear_Weight;
    private TextView time;
    //下拉刷新
    private PullToRefreshScrollView mScrollView;
    private Button btnleft, btnright;
    private ProgressDialog progressDialog;
    //** 地理位置
//    private LocationClient mLocationClient;
//    public MyLocationListener mMyLocationListener;
    private String User_avatar_file_name;
    private UserEntity userEntity;
    /******
     * 服务类传来的广播
     ******/
    private Blereciver blereciver;
    private BLEHandler.BLEProviderObserverAdapter bleProviderObserver;
    private BLEProvider provider;
    //获取钱包详情专用
    private LPDeviceInfo deviceInfo = new LPDeviceInfo();
    String startDateString;
    String endDateString;
    String timeNow;
    boolean isRunning = false;
    //目标值
    private int money_goal, step_goal, distace_goal, cal_goal, runtime_goal;
    private float sleeptime_goal, weight_goal;

    /**
     * 下拉同步ui的超时复位延迟执行handler （防止意外情况下，一直处于“同步中”的状态）
     */
    private Handler mScrollViewRefreshingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mScrollView.isRefreshing())
                mScrollView.onRefreshComplete();
            super.handleMessage(msg);
        }
    };

    /**
     * 当前正在运行中的数据加载异步线程(放全局的目的是尽量控制当前只有一个在运行，防止用户恶意切换导致OOM)
     */
    private AsyncTask<Object, Object, DaySynopic> currentDataAsync = null;
    private ArrayList<MenuVO> listwithoutHR;
    private ArrayList<MenuVO> listwithHR;
    private ArrayList<MenuVO> list;
    private AlertDialog.Builder builder;
    private GreendaoUtils greendaoUtils;

//    private Observer obsForBerforeConnect = new Observer() {
//        @Override
//        public void update(Observable observable, Object data) {
//            if((Boolean) data){
//                MyLog.e(TAG, "开始连接");
//            }
//        }
//    };

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
//        if(provider.getBleProviderObserver()!=null)
//            provider.setBleProviderObserver(null);
//        }
    }

    @Override
    protected void onPostResume() {
        MyLog.e(TAG, "onPostResume()了");
        super.onPostResume();
        isRunning = true;
        provider = BleService.getInstance(PortalActivity.this).getCurrentHandlerProvider();
        provider.setBleProviderObserver(bleProviderObserver);

        UserEntity userEntity = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider();

        if (userEntity == null || userEntity.getDeviceEntity() == null || userEntity.getUserBase() == null)
            return;
        MyLog.e(TAG, "u.getDeviceEntity().getDevice_type():" + userEntity.getDeviceEntity().getDevice_type());
        //判断下要隐藏哪些
        refreshVISIBLE();
        //运动目标重置一下
        initGoal();
        getInfoFromDB();
        if(!CommonUtils.isStringEmpty(userEntity.getDeviceEntity().getLast_sync_device_id())){
            if(provider.isConnectedAndDiscovered())
                BleService.getInstance(PortalActivity.this).syncAllDeviceInfoAuto(PortalActivity.this, false, null);
        }

        if (userEntity.getUserBase().getUser_avatar_file_name() == null){
            MyLog.e(TAG, "u.getUserBase().getUser_avatar_file_name()是空的........");
            return;
        }

        if (!userEntity.getUserBase().getUser_avatar_file_name().equals(User_avatar_file_name) || !userEntity.getUserBase().getNickname().equals(userEntity.getUserBase().getNickname())) {
            refreshHeadView();
        }
        //刷新企业logo
        refreshEntHead();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        unregisterReceiver(blereciver);
        provider.setBleProviderObserver(null);
        AppManager.getAppManager().removeActivity(this);
        // 如果有未执行完成的AsyncTask则强制退出之，否则线程执行时会空指针异常哦！！！
        AsyncTaskManger.getAsyncTaskManger().finishAllAsyncTask();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portal_main);
        AppManager.getAppManager().addActivity(this);
        //主界面开始运行
        isRunning = true;

//        心率工具
        DaoMaster.DevOpenHelper heartrateHelper = new DaoMaster.DevOpenHelper(PortalActivity.this, "heartrate", null);
        SQLiteDatabase readableDatabase = heartrateHelper.getReadableDatabase();
        greendaoUtils = new GreendaoUtils(PortalActivity.this, readableDatabase);

        userEntity = MyApplication.getInstance(this).getLocalUserInfoProvider();
        provider = BleService.getInstance(this).getCurrentHandlerProvider();
        bleProviderObserver = new BLEProviderObserverAdapterImpl();
        provider.setBleProviderObserver(bleProviderObserver);
        // 系统到本界面中，应该已经完成准备好了，开启在网络连上事件时自动启动同步线程的开关吧
        MyApplication.getInstance(this).setPreparedForOfflineSyncToServer(true);
        //用户打开系统消息权限  //**跳到系统设置界面
        if (userEntity.getDeviceEntity().getDevice_type() == MyApplication.DEVICE_WATCH && !ToolKits.isEnabled(PortalActivity.this)) {
            startActivity(new Intent(NotificationCollectorService.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }
        //初始化百度地图
        initLocation();
        //初始化蓝牙广播
        initReciver();
        //初始化目标
        initGoal();
        //初始化UI
        initView();
        initListener();
        refreshVISIBLE();
//        getServerInfo();
        //开始定位
//        mLocationClient.start();
        // 刷新电量UI
        refreshBatteryUI();
        //自动装载体重数据
        autoInsertWeight();
        //开始时间
        startDateString = TimeUtils.getstartDateTime(0, new Date());
        //结束时间
        endDateString = new SimpleDateFormat(ToolKits.DATE_FORMAT_YYYY_MM_DD).format(new Date());
        //检查绑定
        String s = userEntity.getDeviceEntity().getLast_sync_device_id();
//        if (CommonUtils.isStringEmpty(s))
////            showBundDialog();
//        else{
//            provider.setCurrentDeviceMac(s);
//        }
        UserWeight userWeight = WeightTable.queryWeightByDay(this,Integer.toString(MyApplication.getInstance(this).getLocalUserInfoProvider().getUser_id()) , null);
        MyLog.e("userWeight",userWeight+"");

        //自动下拉刷新
        mScrollView.autoRefresh();
        mScrollViewRefreshingHandler.post(new Runnable() {
            @Override
            public void run() {
                Message ms = new Message();
                mScrollViewRefreshingHandler.sendMessageDelayed(ms, 10000);
            }
        });

        inCheckShowHR();
        builder = new AlertDialog.Builder(PortalActivity.this);

    }

    private void inCheckShowHR() {
        userEntity = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider();
        String s = userEntity.getDeviceEntity().getLast_sync_device_id();
        if (CommonUtils.isStringEmpty(s)){
            setAdapter();
        }else {
            setAdapterwithHR();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        inCheckShowHR();
        /**用户性别*/
        if (userEntity!=null) {
            int user_sex = userEntity.getUserBase().getUser_sex();
            if (user_sex == 0) {
                //女生
                user_head.setImageResource(R.mipmap.default_avatar_m);
            } else {
                //男生
                user_head.setImageResource(R.mipmap.default_avatar);
            }
        }

    }

    private void autoInsertWeight() {
        SimpleDateFormat sdf = new SimpleDateFormat(ToolKits.DATE_FORMAT_YYYY_MM_DD);
        String nowdateStr = sdf.format(new Date()); //今天的日期 2016-05-04
//        List<UserWeight> weightlist = new ArrayList<>();
//        weightlist.add(new UserWeight("2016-05-01",MyApplication.getInstance(this).getLocalUserInfoProvider().getUser_id()+"","60.0"));
//        WeightTable.saveToSqliteAsync(this,weightlist,MyApplication.getInstance(this).getLocalUserInfoProvider().getUser_id()+"");

        //去查询数据库最后一条
        UserWeight userWeight = WeightTable.queryWeightByDay(this,Integer.toString(MyApplication.getInstance(this).getLocalUserInfoProvider().getUser_id()), null);

        if (userWeight == null) {
            Log.e(TAG, "userWeight是null");
            //查询是null的时候代表一条记录也没有
            return;
        }
        String weight = userWeight.getWeight();
        String lastdateStr = userWeight.getTime();
        if (lastdateStr.equals(nowdateStr)) {
            //最后一天是今天 就什么都不做了
            return;
        }
        Date lastdate = ToolKits.stringToDate(lastdateStr, ToolKits.DATE_FORMAT_YYYY_MM_DD);
        //今天和最后一条记录差距的天数
        int i = ToolKits.getindexfromDate(lastdate, new Date());
        List<String> list = ToolKits.getlistDate(lastdate, i);
        List<UserWeight> weightlist = new ArrayList<>();
        for (String date : list) {
            Log.e(TAG, "data是:" + date);
            weightlist.add(new UserWeight(date, MyApplication.getInstance(this).getLocalUserInfoProvider().getUser_id() + "", weight));
        }
        WeightTable.saveToSqliteAsync(this, weightlist, MyApplication.getInstance(this).getLocalUserInfoProvider().getUser_id() + "");
    }


    private void initLocation() {
      /*  mLocationClient = new LocationClient(PortalActivity.this.getApplicationContext());
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("gcj02");// 返回的定位结果是百度经纬度，默认值gcj02 bd09ll bd09
        option.setScanSpan(1000);// 设置发起定位请求的间隔时间为1000ms
        option.setIsNeedAddress(true); // 返回地址
        mLocationClient.setLocOption(option);*/
}

    private void initView() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.general_loading));
        //修改日期左右的按钮
        btnleft = (Button) findViewById(R.id.leftBtn);
        btnright = (Button) findViewById(R.id.rightBtn);
        mScrollView = (PullToRefreshScrollView) findViewById(R.id.mainScrollView);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        menu_RecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        user_head = (ImageView) findViewById(R.id.user_head);
        user_name = (TextView) findViewById(R.id.user_name);
        time = (TextView) findViewById(R.id.text_time);
        timeNow = new SimpleDateFormat(ToolKits.DATE_FORMAT_YYYY_MM_DD).format(new Date());
        time.setText(timeNow);
        date = (LinearLayout) findViewById(R.id.linear_date);
        setSupportActionBar(toolbar);
        //隐藏title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        menu_RecyclerView.setLayoutManager(layoutManager);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.END);
        //无阴影
        drawer.setScrimColor(Color.TRANSPARENT);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //侧边栏适配器
        inCheckShowHR();
        refreshHeadView();
        refreshEntHead();

        linear_unbund = (LinearLayout) findViewById(R.id.linear_unbund);
        //各个项
        linear_wallet = (LinearLayout) findViewById(R.id.linear_wallet);
        device_img = (ImageView) findViewById(R.id.device_img);
        linear_Step = (LinearLayout) findViewById(R.id.linear_step);
        linear_Sleep = (LinearLayout) findViewById(R.id.linear_sleep);
        linear_Cal = (LinearLayout) findViewById(R.id.linear_cal);
        linear_heartrate = (LinearLayout) findViewById(R.id.linear_heartrate);
        linear_Run = (LinearLayout) findViewById(R.id.linear_run);
        linear_Distance = (LinearLayout) findViewById(R.id.linear_distance);
        linear_Battery = (LinearLayout) findViewById(R.id.linear_battery);
        linear_Weight = (LinearLayout) findViewById(R.id.linear_weight);
        linear_heartrate = (LinearLayout) findViewById(R.id.linear_heartrate);
//        linear_Weight.setVisibility(View.GONE);
        //提示词
        text_Battery = (TextView) findViewById(R.id.text_battery);
        text_Wallet = (TextView) findViewById(R.id.text_wallet);
        text_Step = (TextView) findViewById(R.id.text_step);
        text_Distance = (TextView) findViewById(R.id.text_distance);
        text_Cal = (TextView) findViewById(R.id.text_cal);
        text_Run = (TextView) findViewById(R.id.text_run);
        text_Sleep = (TextView) findViewById(R.id.text_sleep);
        text_Weight = (TextView) findViewById(R.id.text_weight);
        text_heart = (TextView) findViewById(R.id.text_heart);
        //进度条
        Battery_ProgressBar = (CustomProgressBar) findViewById(R.id.progressBar_battery);//progressBar_battery
        Wallet_ProgressBar = (CustomProgressBar) findViewById(R.id.progressBar_wallet);
        Step_ProgressBar = (CustomProgressBar) findViewById(R.id.progressBar_step);
        Distance_ProgressBar = (CustomProgressBar) findViewById(R.id.progressBar_distance);
        Cal_ProgressBar = (CustomProgressBar) findViewById(R.id.progressBar_cal);
        Run_ProgressBar = (CustomProgressBar) findViewById(R.id.progressBar_run);
        Sleep_ProgressBar = (CustomProgressBar) findViewById(R.id.progressBar_sleep);
        Weight_ProgressBar = (CustomProgressBar) findViewById(R.id.progressBar_weight);
        //百分比
        text_Battery_Progress = (TextView) findViewById(R.id.percent_battery);
        text_Wallet_Progress = (TextView) findViewById(R.id.percent_wallet);
        text_Step_Progress = (TextView) findViewById(R.id.percent_step);
        text_Distance_Progress = (TextView) findViewById(R.id.percent_distance);
        text_Cal_Progress = (TextView) findViewById(R.id.percent_cal);
        text_Run_Progress = (TextView) findViewById(R.id.percent_run);
        text_Sleep_Progress = (TextView) findViewById(R.id.percent_sleep);
        text_Weight_Progress = (TextView) findViewById(R.id.percent_weight);
    }

    private void initListener() {
        mScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                //刷新同步数据
                String s = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity().getLast_sync_device_id();
                if (CommonUtils.isStringEmpty(s)) {
                   //
                    showBundDialog();
                    mScrollView.onRefreshComplete();
                } else {
                    // 启动超时处理handler
                    // 进入扫描和连接处理过程
                    provider.setCurrentDeviceMac(s);
                    //开始同步
                    BleService.getInstance(PortalActivity.this).syncAllDeviceInfoAuto(PortalActivity.this, false, null);
                    // 启动超时处理handler
                    mScrollViewRefreshingHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Message ms = new Message();
                            mScrollViewRefreshingHandler.sendMessageDelayed(ms, 10000);
                        }
                    });
                }
            }
        });

        btnleft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                点击按钮 时间往后一天  刷新界面
                timeNow = ToolKits.getSpecifiedDayBefore(timeNow);
                time.setText(timeNow);
                Date date = ToolKits.stringToDate(timeNow, ToolKits.DATE_FORMAT_YYYY_MM_DD);
                startDateString = TimeUtils.getstartDateTime(0, date);
                endDateString = new SimpleDateFormat(ToolKits.DATE_FORMAT_YYYY_MM_DD).format(date);
                //查询数据
                getInfoFromDB();
            }
        });
        btnright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击按钮  弹框提示明天还未来临
                if (timeNow.equals(sdf.format(new Date()))) {
                    Toast.makeText(PortalActivity.this, getString(R.string.ranking_wait_tomorrow), Toast.LENGTH_LONG).show();
                } else {
                    timeNow = ToolKits.getSpecifiedDayAfter(timeNow);
                    time.setText(timeNow);
                    Date date = ToolKits.stringToDate(timeNow, ToolKits.DATE_FORMAT_YYYY_MM_DD);
                    startDateString = TimeUtils.getstartDateTime(0, date);
                    endDateString = new SimpleDateFormat(ToolKits.DATE_FORMAT_YYYY_MM_DD).format(date);
                    //查询数据
                    getInfoFromDB();
                }
            }
        });
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar;// 用来装日期的
                calendar = Calendar.getInstance();
                DatePickerDialog dialog1;
                dialog1 = new DatePickerDialog(PortalActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        Date date = new Date(calendar.getTimeInMillis());
                        endDateString = new SimpleDateFormat(ToolKits.DATE_FORMAT_YYYY_MM_DD).format(date);
                        if (ToolKits.compareDate(date, new Date())) {
                            Toast.makeText(PortalActivity.this, getString(R.string.ranking_wait_tomorrow), Toast.LENGTH_LONG).show();
                        } else {
                            time.setText(endDateString);
                            timeNow = endDateString;
                            MyLog.e(TAG, "riqi" + startDateString + "   " + endDateString);
                            getInfoFromDB();
                        }
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dialog1.show();
            }
        });

        user_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //进入个人信息.详细页面
                Intent intent = IntentFactory.create_PersonalInfo_Activity_Intent(PortalActivity.this);
                startActivityForResult(intent, REQUSET_FOR_PERSONAL);
            }
        });
        linear_unbund.setOnClickListener(this);
        linear_wallet.setOnClickListener(this);
        linear_Run.setOnClickListener(this);
        linear_Cal.setOnClickListener(this);
        linear_Step.setOnClickListener(this);
        linear_Sleep.setOnClickListener(this);
        linear_Distance.setOnClickListener(this);
        linear_Battery.setOnClickListener(this);
        linear_Weight.setOnClickListener(this);
        linear_heartrate.setOnClickListener(this);

    }

    //获取目标值
    private void initGoal() {
        money_goal = Integer.parseInt(PreferencesToolkits.getGoalInfo(this, PreferencesToolkits.KEY_GOAL_MONEY));
        step_goal = Integer.parseInt(PreferencesToolkits.getGoalInfo(this, PreferencesToolkits.KEY_GOAL_STEP));
        distace_goal = Integer.parseInt(PreferencesToolkits.getGoalInfo(this, PreferencesToolkits.KEY_GOAL_DISTANCE));
        cal_goal = Integer.parseInt(PreferencesToolkits.getGoalInfo(this, PreferencesToolkits.KEY_GOAL_CAL));
        runtime_goal = Integer.parseInt(PreferencesToolkits.getGoalInfo(this, PreferencesToolkits.KEY_GOAL_RUN));

        sleeptime_goal = Float.parseFloat(PreferencesToolkits.getGoalInfo(this, PreferencesToolkits.KEY_GOAL_SLEEP));
        weight_goal = Float.parseFloat(PreferencesToolkits.getGoalInfo(this, PreferencesToolkits.KEY_GOAL_WEIGHT));
        //此时是kg
//        if(PreferencesToolkits.getLocalSettingUnitInfo(this)!=ToolKits.UNIT_GONG){ //公制
//            weight_goal = ToolKits.calcKG2LB((int)weight_goal);
//        }
    }

    private void refreshVISIBLE() {
        if (CommonUtils.isStringEmpty(MyApplication.getInstance(this).getLocalUserInfoProvider().getDeviceEntity().getLast_sync_device_id())) {
            linear_unbund.setVisibility(View.VISIBLE); //未绑定提示出现
            linear_Battery.setVisibility(View.GONE);
            linear_wallet.setVisibility(View.GONE);
            BleService.getInstance(this).releaseBLE();
            linear_heartrate.setVisibility(View.GONE);
        } else {
            //刷新电量
            refreshBatteryUI();
            linear_heartrate.setVisibility(View.GONE);
            //绑定时候
            linear_unbund.setVisibility(View.GONE);   //未绑定提示消失
            linear_Battery.setVisibility(View.VISIBLE);
            //手环的时候显示手环 手表显示手表
            UserEntity userEntity = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider();
            if (userEntity.getDeviceEntity().getDevice_type() == MyApplication.DEVICE_WATCH) {
                device_img.setImageDrawable(getResources().getDrawable(R.mipmap.device_watch));
            } else if (userEntity.getDeviceEntity().getDevice_type() == MyApplication.DEVICE_BAND) {
                device_img.setImageDrawable(getResources().getDrawable(R.mipmap.band_logo));
            }else if (userEntity.getDeviceEntity().getDevice_type() == MyApplication.DEVICE_BAND3) {
                device_img.setImageDrawable(getResources().getDrawable(R.mipmap.band3_logo));
            }
            if(CommonUtils.isStringEmpty(userEntity.getDeviceEntity().getModel_name())){
                linear_wallet.setVisibility(View.GONE);
                return;
            }
//            ModelInfo modelInfo = PreferencesToolkits.getInfoBymodelName(PortalActivity.this,userEntity.getDeviceEntity().getModel_name());
//            if(modelInfo.getFiscard()==0){ //不支持金融卡
                linear_wallet.setVisibility(View.GONE);
//            }else{
//                linear_wallet.setVisibility(View.VISIBLE);
//            }
        }

    }

    private String getOneDayHeartrate(){
        Date parse = null;
        try {
            parse = sdf.parse(timeNow);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parse);
        calendar.set(Calendar.HOUR,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        long dayStart = calendar.getTime().getTime();
        MyLog.e("开始时间："+calendar.getTime());
        calendar.set(Calendar.HOUR,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        calendar.set(Calendar.MILLISECOND,999);
        long dayEnd = calendar.getTime().getTime();
        MyLog.e("结束时间："+calendar.getTime());
        List<heartrate> heartrates = greendaoUtils.searchOneDay(dayStart, dayEnd);
        ArrayList<BarChartView.BarChartItemBean> list = new ArrayList<>();
        int rest = 0 ;
        int avg = 0 ;
        for (heartrate record : heartrates){
            BarChartView.BarChartItemBean barChartItemBean = new BarChartView.BarChartItemBean
                    (record.getStartTime(), record.getMax(), record.getAvg());
            list.add(barChartItemBean);
            rest = rest+record.getMax();
            avg = avg+record.getAvg();
        }
        int resting,avging = 0;
        String result = "" ;
        if (list.size()==0){
            resting = 0 ;
            avging = 0 ;
        }else{
            resting = rest / list.size();
            avging = avg/list.size();
        }
        result = avging +" Avg.bpm" ;
        if (avging==0){
//            UserEntity localUserInfoProvider = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider();
//            String birthdate = localUserInfoProvider.getUserBase().getBirthdate();
//            String[] split = birthdate.split("-");
//            int i = Integer.parseInt(split[0]);
//            int year = Calendar.getInstance().get(Calendar.YEAR);
//            int restings = (int) ((220 - (year - i)) * 0.4);
//            result = restings +" Resting.bpm";
            result = "--"+" Avg.bpm";
        }
        return result;
    }

    private void getServerInfo() {
        /**
         * 查询是否有反馈信息
         */
        CallServer.getRequestInstance().add(PortalActivity.this, false, CommParams.HTTP_FEEDBACK_QUERY, NoHttpRuquestFactory.queryfeedbackMsg(userEntity.getUser_id() + ""), httpCallback);

        /**
         * 获取消息未读数
         */
        CallServer.getRequestInstance().add(PortalActivity.this, false, CommParams.HTTP_UNREAD_QUERY, NoHttpRuquestFactory.Query_Unread_Request(userEntity.getUser_id() + ""), httpCallback);
    }


    private void initReciver() {
        blereciver = new Blereciver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BleService.BLE_SYN_SUCCESS);
        filter.addAction(BleService.BLE_STATE_SUCCESS);
        registerReceiver(blereciver, filter);
    }

    private class Blereciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BleService.BLE_SYN_SUCCESS) && isRunning) {
                MyLog.e(TAG, "onReceive BleService.BLE_SYN_SUCCESS");
                //目前什么也没做
            } else if (intent.getAction().equals(BleService.BLE_STATE_SUCCESS)) {
                //硬件更新提示 一天一次
//                if((System.currentTimeMillis()/1000)-PreferencesToolkits.gettime(getActivity())>86400){
//                    new UntreatedAsyncTask().execute();
//                    PreferencesToolkits.savetime(getActivity(), (System.currentTimeMillis()/1000));
//                }
            }
//            else if (intent.getAction().equals(BleService.REFRESH_VIEW)){
//                Log.e(TAG, "刷新企业logo");
//                //企业图标
//                refreshEntImgShow(MyApplication.getInstance(getActivity()).getLocalUserInfoProvider());
//                refreshEntNoticeShow(MyApplication.getInstance(getActivity()).getLocalUserInfoProvider());
//                //绑定&解绑 刷新完毕
//            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linear_unbund:
                if (MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider() == null ||
                        CommonUtils.isStringEmpty(MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity().getLast_sync_device_id())) {
                    showBundDialog();
                }
                break;

            case R.id.linear_step:
                Intent intent1 = IntentFactory.cteate_StepDataActivityIntent(PortalActivity.this);
                intent1.putExtra("time", time.getText());
                startActivity(intent1);
                break;
            case R.id.linear_distance:
                Intent intent2 = IntentFactory.cteate_DiatanceDataActivityIntent(PortalActivity.this);
                intent2.putExtra("time", time.getText());
                startActivity(intent2);
                //startActivity(IntentFactory.cteate_DiatanceDataActivityIntent(PortalActivity.this));
                break;
            case R.id.linear_cal:
                Intent intent3 = IntentFactory.cteate_CalDataActivityIntent(PortalActivity.this);
                intent3.putExtra("time", time.getText());
                startActivity(intent3);
                //  startActivity(IntentFactory.cteate_CalDataActivityIntent(PortalActivity.this));
                break;
            case R.id.linear_run:

                break;
            case R.id.linear_sleep:

                Intent intent4 = IntentFactory.cteate_SleepDataActivityIntent(PortalActivity.this);
                intent4.putExtra("time", time.getText());
                startActivity(intent4);
                break;
            case R.id.linear_battery:
                if (userEntity == null || CommonUtils.isStringEmpty(MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity().getLast_sync_device_id())) {
                    showBundDialog();
                } else {
                    startActivity(IntentFactory.star_DeviceActivityIntent(PortalActivity.this,0));
                }
                break;
            case R.id.linear_wallet:
                if (userEntity == null || CommonUtils.isStringEmpty(MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity().getLast_sync_device_id())) {
                    showBundDialog();
                } else {
                    if (CommonUtils.isStringEmpty(MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity().getCard_number())) {
                        return;
                    }
                    if (MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity().getCard_number().equals("NO_PAY")) {

                        Toast.makeText(PortalActivity.this, R.string.pay_no_function, Toast.LENGTH_LONG).show();

                    } else {
                        if (provider.isConnectedAndDiscovered()) {
                            startActivity(IntentFactory.start_WalletActivityIntent(PortalActivity.this));
                        } else {
                            Toast.makeText(PortalActivity.this, getString(R.string.pay_no_connect), Toast.LENGTH_LONG).show();
                        }
                    }
                }
                break;
            //点击体重
            case R.id.linear_weight:
                Intent intent5=new Intent(PortalActivity.this, WeightActivity.class);
                intent5.putExtra("time", time.getText());
                startActivity(intent5);
                break;
            //点击心率
            case R.id.linear_heartrate:
                IntentFactory.startHeartRateActivity(PortalActivity.this);
        }
    }

  /*  *//**
     * 实现GPS定位回调监听。
     *//*
    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (PortalActivity.this != null) {
//                if (null != location && location.getLocType() != BDLocation.TypeServerError) {
//
//                }
//                Toast.makeText(PortalActivity.this,"当前经纬度："+location.getLatitude() +"=========="+location.getLongitude(),Toast.LENGTH_LONG).show();
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                String city = location.getCity();
                UserEntity userEntity = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider();
                if (userEntity.getUserBase() == null) {
                    return;
                }
                userEntity.getUserBase().setLongitude(longitude + "");
                userEntity.getUserBase().setLatitude(latitude + "");
                //台湾离线版本取消定位
//                CallServer.getRequestInstance().add(PortalActivity.this, false, CommParams.HTTP_SUBMIT_BAIDU, NoHttpRuquestFactory.submit_RegisterationToServer_Modify(userEntity.getUserBase()), httpCallback);
                MyLog.i("百度定位", "MyLocationListener" + longitude + ">>>>" + latitude + "========city:" + city);
                mLocationClient.stop();
            } else {
                mLocationClient.stop();
            }
        }*/
//    }

    /**
     * 刷新用户头像和昵称
     */
    private void refreshHeadView() {

        MyLog.i(TAG,"刷新头像和昵称,等数据");
        //图像以后设置
        UserEntity u = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider();
        if (u == null) {
            MyLog.i(TAG,"获得的UserEntity是空的");
            return;
        }
        MyLog.i(TAG, "获得的UserEntity的名字=" + u.getUserBase().getNickname());
        user_name.setText(u.getUserBase().getNickname());



        User_avatar_file_name = u.getUserBase().getUser_avatar_file_name();
        if (User_avatar_file_name != null) {
            String url = NoHttpRuquestFactory.getUserAvatarDownloadURL(PortalActivity.this, u.getUser_id() + "", u.getUserBase().getUser_avatar_file_name(), true);
            User_avatar_file_name = u.getUserBase().getUser_avatar_file_name();
            DisplayImageOptions options;
            options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                    .cacheOnDisc(true)//设置下载的图片是否缓存在SD卡中
                    .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                    .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示
                    .showImageOnFail(R.mipmap.default_avatar)//加载失败显示图片
                    .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                    .resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                    .build();//构建完成

            ImageLoader.getInstance().displayImage(url, user_head, options, new ImageLoadingListener() {

                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    ImageView mhead = (ImageView) view;
                    mhead.setImageBitmap(loadedImage);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
        }

    }

    // 刷新企业图标
    private  void refreshEntHead(){
        //刷新企业图标
        //企业定制的时候需要动态更换
        if(!CommonUtils.isStringEmpty(MyApplication.getInstance(this).getLocalUserInfoProvider().getEntEntity().getEnt_id())){
            String imageUrl=NoHttpRuquestFactory.getEntAvatarDownloadURL(MyApplication.getInstance(this).getLocalUserInfoProvider().getEntEntity().getPortal_logo_file_name());
            ImageLoader.getInstance().loadImage(imageUrl,new SimpleImageLoadingListener(){
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    MyLog.i(TAG, imageUri);
                   Resources res = getResources();
//                    Bitmap bmp = BitmapFactory.decodeResource(res, R.mipmap.logo);
                    Drawable drawable =new BitmapDrawable(res,loadedImage);
                    toolbar.setLogo(drawable);
                }
            });
        }else {
            MyLog.i(TAG,"获得的getEntEntity是空的");
            Resources res = getResources();
            Bitmap bmp = BitmapFactory.decodeResource(res, R.mipmap.logo);
            Drawable drawable =new BitmapDrawable(res,bmp);
            toolbar.setLogo(drawable);
        }
    }

    /**
     * 侧滑栏适配器
     */
    private void setAdapter() {
        listwithoutHR = new ArrayList<MenuVO>();
        for (int i = 0; i < Left_viewVO.menuIcon.length; i++) {
            MenuVO vo = new MenuVO();
            vo.setImgID(Left_viewVO.menuIcon[i]);
            vo.setTextID(Left_viewVO.menuText[i]);
            vo.setOrderId(Left_viewVO.menuID[i]);
            listwithoutHR.add(vo);
        }
        menuAdapter = new MenuNewAdapter(this, listwithoutHR);
        menuAdapter.setOnRecyclerViewListener(this);
        menu_RecyclerView.setAdapter(menuAdapter);
    }

    private void setAdapterwithHR() {
        listwithHR = new ArrayList<MenuVO>();
        for (int i = 0; i < Left_viewVO.menuIconwithHR.length; i++) {
            MenuVO vo = new MenuVO();
            vo.setImgID(Left_viewVO.menuIconwithHR[i]);
            vo.setTextID(Left_viewVO.menuTextwithHR[i]);
            vo.setOrderId(Left_viewVO.menuIDwithHR[i]);
            listwithHR.add(vo);
        }
        menuAdapter = new MenuNewAdapter(this, listwithHR);
        menuAdapter.setOnRecyclerViewListener(this);
        menu_RecyclerView.setAdapter(menuAdapter);


    }






        @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.portal, menu);
        return true;
    }

    /**
     * toolbar右上角的点击选项
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        int num = MyApplication.getInstance(this).getCommentNum();
        if (id == R.id.action_settings) {
            MyLog.e(TAG, "点击了设置");

//            BleService.getInstance(PortalActivity.this).getCurrentHandlerProvider().SetDeviceTime(PortalActivity.this);
//            if(num > 0){
            item.setTitle(ToolKits.getUnreadString(num));
            //跳转到评论页面
//                Intent intent =new Intent(PortalActivity.this,FriendActivity.class);
//                startActivity(intent);
            IntentFactory.start_FriendActivity(PortalActivity.this, JUMP_FRIEND_TAG_TWO);
//            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //侧边栏点击事件 可以在这里复写 暂时没用到
    @Override
    public void onItemClick(int position) {

    }

    //刷新手表电量  + 获取存在数据库的数据
    private void getInfoFromDB() {
        //子线程去计算汇总数据
        MyLog.e(TAG, "====================开始执行异步任务====================");
        AsyncTask<Object, Object, DaySynopic> dataasyncTask = new AsyncTask<Object, Object, DaySynopic>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
//                if (progressDialog != null && !progressDialog.isShowing())
//                    progressDialog.show();
            }

            @Override
            protected DaySynopic doInBackground(Object... params) {
                DaySynopic mDaySynopic = null;

                if (timeNow.equals(sdf.format(new Date()))) {
                    ArrayList<DaySynopic> mDaySynopicArrayList = new ArrayList<DaySynopic>();
                    MyLog.e(TAG, "endDateString:" + endDateString);
                    //今天的话 无条件去汇总查询
                    mDaySynopic = SportDataHelper.offlineReadMultiDaySleepDataToServer(PortalActivity.this, endDateString, endDateString);
                    if (mDaySynopic.getTime_zone() == null) {
                        return null;
                    }
                    MyLog.e(TAG, "daySynopic:" + mDaySynopic.toString());
                    mDaySynopicArrayList.add(mDaySynopic);
                    DaySynopicTable.saveToSqliteAsync(PortalActivity.this, mDaySynopicArrayList, userEntity.getUser_id() + "");
                    /****************今天的步数给到 方便OAD完成后回填步数 的变量里面去****************/
                    //今天的步数给到 方便OAD完成后回填步数 的变量里面去
                    //走路 步数
                    int walkStep = (int) (CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getWork_step()), 0));
                    //跑步 步数
                    int runStep = (int) (CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getRun_step()), 0));
                    int step = walkStep + runStep;
                    MyApplication.getInstance(PortalActivity.this).setOld_step(step);
                    /****************今天的步数给到 方便OAD完成后回填步数 的变量里面去****************/
                } else {
                    ArrayList<DaySynopic> mDaySynopicArrayList = DaySynopicTable.findDaySynopicRange(PortalActivity.this, userEntity.getUser_id() + "", endDateString, endDateString, String.valueOf(TimeZoneHelper.getTimeZoneOffsetMinute()));
                    MyLog.e(TAG, "mDaySynopicArrayList:" + mDaySynopicArrayList.toString());
                    //在判断一次,如果得到集合是空,我就去明细表里去查询数据.进行汇总
                    if (mDaySynopicArrayList == null || mDaySynopicArrayList.size() == 0) {
                        mDaySynopic = SportDataHelper.offlineReadMultiDaySleepDataToServer(PortalActivity.this, endDateString, endDateString);
                        MyLog.e(TAG, "daySynopic:" + mDaySynopic.toString());
                        DaySynopicTable.saveToSqliteAsync(PortalActivity.this, mDaySynopicArrayList, userEntity.getUser_id() + "");
                    } else {
                        mDaySynopic = mDaySynopicArrayList.get(0);
                    }
                }
                return mDaySynopic;
            }

            @Override
            protected void onPostExecute(DaySynopic mDaySynopic) {
                super.onPostExecute(mDaySynopic);

                LocalInfoVO localvo = PreferencesToolkits.getLocalDeviceInfo(PortalActivity.this);
                String money = localvo.getMoney();
                //=============计算基础卡路里=====START========//
                int cal_base = 0;
                if (timeNow.equals(sdf.format(new Date()))) {
                    int hour = Integer.parseInt(new SimpleDateFormat("HH").format(new Date()));
                    int minute = Integer.parseInt(new SimpleDateFormat("mm").format(new Date()));
                    cal_base = (int) ((hour * 60 + minute) * 1.15);//当前时间今天的卡路里

                } else {
                    cal_base = 1656;
                }
                //查询体重,获得体重的集合
                List<UserWeight> list=WeightTable.queryWeights(PortalActivity.this,userEntity.getUser_id()+"",endDateString,endDateString);
                double weight ;

                if(list!=null&&list.size()>0){
                    weight=CommonUtils.getScaledDoubleValue(Double.valueOf(list.get(0).getWeight()), 1);
//                    if(PreferencesToolkits.getLocalSettingUnitInfo(PortalActivity.this)!=ToolKits.UNIT_GONG){ //公制
//                        weight = ToolKits.calcKG2LB((float)weight);
//                    }
                }else {
                    weight=0;
                }
                if (mDaySynopic == null) {
                    MyLog.e(TAG, "mDaySynopic空的");
                    refreshView(0, 0, 0, 0, cal_base, money, weight);
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    AsyncTaskManger.getAsyncTaskManger().removeAsyncTask(this);
                    return;
                }
                //走路 步数
                int walkStep = (int) (CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getWork_step()), 0));
                //跑步 步数
                int runStep = (int) (CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getRun_step()), 0));
                int step = walkStep + runStep;
                MyApplication.getInstance(PortalActivity.this).setOld_step(step);
//daySynopic:[data_date=2016-04-14,data_date2=null,time_zone=480,record_id=null,user_id=null,run_duration=1.0,run_step=68.0,run_distance=98.0
// ,create_time=null,work_duration=178.0,work_step=6965.0,work_distance=5074.0,sleepMinute=2.0916666984558105,deepSleepMiute=1.25 gotoBedTime=1460645100 getupTime=1460657160]
                //走路 里程
                int walkDistance = (int) (CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getWork_distance()), 0));
                //跑步 里程
                int runDistance = (int) (CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getRun_distance()), 0));
                int distance = walkDistance + runDistance;
                MyApplication.getInstance(PortalActivity.this).setOld_distance(distance);

                //浅睡 小时
                double qianleephour = CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getSleepMinute()), 1);
                //深睡 小时
                double deepleephour = CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getDeepSleepMiute()), 1);

                double sleeptime = CommonUtils.getScaledDoubleValue(qianleephour + deepleephour, 1);
                //走路 分钟
                double walktime = CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getWork_duration()), 1);
                //跑步 分钟
                double runtime = CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getRun_duration()), 1);

                double worktime = CommonUtils.getScaledDoubleValue(walktime + runtime, 1);

                int runcal = _Utils.calculateCalories(Double.parseDouble(mDaySynopic.getRun_distance()) / (Double.parseDouble(mDaySynopic.getRun_duration())), (int) runtime * 60, userEntity.getUserBase().getUser_weight());

                int walkcal = _Utils.calculateCalories(Double.parseDouble(mDaySynopic.getWork_distance()) / (Double.parseDouble(mDaySynopic.getWork_duration())), (int) walktime * 60, userEntity.getUserBase().getUser_weight());

                int calValue = runcal + walkcal;
                MyApplication.getInstance(PortalActivity.this).setOld_calories(calValue);

//                double speed=(distance)/(worktime*60);

//                int weight1=userEntity.getUserBase().getUser_weight();

//                Log.i(TAG,"distance="+distance+"speed="+speed+"worktime"+worktime*60+"getUser_weight="+userEntity.getUserBase().getUser_weight());

//                double count= CountCalUtil.calculateCalories(distance, (int) worktime * 60, weight1);
//
//                Log.i(TAG,"卡路里"+calValue+"体重="+userEntity.getUserBase().getUser_weight()+"count="+count);
                // 计算卡路里

//                Log.i(TAG,"卡路里"+calValue+"体重="+userEntity.getUserBase().getUser_weight()+"count="+count);
//                //将数据显示在控件上
                refreshView(step, distance, runtime, sleeptime, cal_base + calValue, money, weight);

                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                AsyncTaskManger.getAsyncTaskManger().removeAsyncTask(this);
                //// TODO: 2017/4/1
                UserEntity userEntity = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider();
                linear_heartrate.setVisibility(View.GONE);
                if (userEntity.getDeviceEntity().getDevice_type() == MyApplication.DEVICE_BAND3) {
                    linear_heartrate.setVisibility(View.VISIBLE);
                    String oneDayHeartrate = getOneDayHeartrate();
                    text_heart.setText(oneDayHeartrate);
                }
            }
        };
        // 确保当前只有一个AsyncTask在运行，否则用户恶心切换会OOM
        if (currentDataAsync != null)
            AsyncTaskManger.getAsyncTaskManger().removeAsyncTask(currentDataAsync, true);

        AsyncTaskManger.getAsyncTaskManger().addAsyncTask(currentDataAsync = dataasyncTask);
        dataasyncTask.execute();
    }

    /**
     * 刷新条目明细 （数值，进度条，百分比）
     *
     * @param step
     * @param distance
     * @param runtime
     * @param sleeptime
     * @param calValue
     * @param money
     */
    private void refreshView(int step, int distance, double runtime, double sleeptime, int calValue, String money, double weight) {
        MyLog.e(TAG, step + " " + distance + " " + runtime + " " + sleeptime + " " + weight);
        MyLog.e(TAG, step_goal + " " + distace_goal + " " + runtime_goal + " " + sleeptime_goal + " " + weight_goal);
        //提示词
        text_Wallet.setText(getResources().getString(R.string.menu_pay_yuan) + money);
        double distance1 = (double) distance / 1000;
        if (SwitchUnit.getLocalUnit(PortalActivity.this) == ToolKits.UNIT_GONG) {
            BigDecimal   b   =   new BigDecimal(distance1);
            double   distanceDouble   =   b.setScale(3,   BigDecimal.ROUND_HALF_UP).doubleValue();
            text_Distance.setText(distanceDouble + getResources().getString(R.string.unit_km_metric));
            text_Weight.setText(weight + getResources().getString(R.string.unit_kilogramme));
        } else {
            BigDecimal   b   =   new BigDecimal((double) UnitTookits.MChangetoMIRate(distance)/1000);
            double   distanceDouble   =   b.setScale(3,   BigDecimal.ROUND_HALF_UP).doubleValue();
            text_Distance.setText(distanceDouble + getResources().getString(R.string.unit_mile));
            text_Weight.setText(weight + getResources().getString(R.string.unit_pound));

        }
        text_Step.setText(step + getResources().getString(R.string.unit_step));

        //  ext_Cal= (TextView) findViewById(R.id.text_cal);
        text_Run.setText(runtime + getResources().getString(R.string.unit_minute));
        text_Sleep.setText(sleeptime + getResources().getString(R.string.unit_short_hour));
        text_Cal.setText(calValue + getResources().getString(R.string.unit_cal));

        MyLog.e(TAG, "money:" + money);
//            MyLog.e(TAG,"Float.parseFloat(money):"+ToolKits.stringtofloat(money));
        //进度条
        Wallet_ProgressBar.setCurProgress((int) (Math.floor(Float.parseFloat(money) * 100 * 1.0f / money_goal)));
        Step_ProgressBar.setCurProgress((int) Math.floor(step * 100 * 1.0f / step_goal));
        Distance_ProgressBar.setCurProgress((int) Math.floor(distance * 100 * 1.0f / distace_goal));
        Run_ProgressBar.setCurProgress((int) Math.floor(runtime * 100 * 1.0f / runtime_goal));
        Sleep_ProgressBar.setCurProgress((int) Math.floor(sleeptime * 100 * 1.0f / sleeptime_goal));
        Cal_ProgressBar.setCurProgress((int) Math.floor(calValue * 100 * 1.0f / cal_goal));
        Weight_ProgressBar.setCurProgress((int) Math.floor(weight * 100 * 1.0f / weight_goal));
        //进度条旁边的百分比
        text_Wallet_Progress.setText((int) (Math.floor(Float.parseFloat(money) * 100 * 1.0f / money_goal)) + "%");
        text_Step_Progress.setText((int) Math.floor(step * 100 * 1.0f / step_goal) + "%");
        text_Distance_Progress.setText((int) Math.floor(distance * 100 * 1.0f / distace_goal) + "%");
        text_Run_Progress.setText((int) Math.floor(runtime * 100 * 1.0f / runtime_goal) + "%");
        text_Sleep_Progress.setText((int) Math.floor(sleeptime * 100 * 1.0f / sleeptime_goal) + "%");
        text_Cal_Progress.setText((int) Math.floor(calValue * 100 * 1.0f / cal_goal) + "%");
        text_Weight_Progress.setText((int) Math.floor(weight * 100 * 1.0f / weight_goal) + "%");

        refreshBatteryUI();
    }

    /**
     * 单独刷新电量
     */
    private void refreshBatteryUI() {
        if (!CommonUtils.isStringEmpty(MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity().getLast_sync_device_id())) {
            if (provider.isConnectedAndDiscovered()) {  //蓝牙连接上的
                LocalInfoVO LocalInfoVO = PreferencesToolkits.getLocalDeviceInfo(PortalActivity.this);
                if (!LocalInfoVO.userId.equals("-1")) {
                    int battery = LocalInfoVO.getBattery() >= 100 ? 100 : LocalInfoVO.getBattery() ;
                    MyLog.e(TAG, "LocalInfoVO电量:" + LocalInfoVO.getBattery());
                    if (battery < LOW_BATTERY) {
                        //电量低于30的时候 弹出低电量警告框
                        AlertDialog dialog_battery = new AlertDialog.Builder(PortalActivity.this)
                                .setTitle(ToolKits.getStringbyId(PortalActivity.this, R.string.portal_main_battery_low))
                                .setMessage(ToolKits.getStringbyId(PortalActivity.this, R.string.portal_main_battery_low_msg))
                                .setPositiveButton(ToolKits.getStringbyId(PortalActivity.this, R.string.general_ok),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).create();
                        dialog_battery.show();
                    }
                    text_Battery.setText("Power "+ battery + "%");//根据电量显示不同的文字提示
                    Battery_ProgressBar.setCurProgress((int) (Math.floor(battery * 100 * 1.0f / 100)));
                    text_Battery_Progress.setText((int) (Math.floor(battery * 100 * 1.0f / 100)) + "%");
                }
            } else if (provider.isConnecting()) { //正在连接
                refreshBattery(PortalActivity.this.getString(R.string.portal_main_state_connecting));
//                Battery_ProgressBar.setIndeterminate(true);
            } else {  //蓝牙未连接上
                refreshBattery(PortalActivity.this.getString(R.string.portal_main_state_unconnect));
//                Battery_ProgressBar.setIndeterminate(false);
            }

        } else {
            //未绑定
            refreshBattery(PortalActivity.this.getString(R.string.portal_main_state_connecting));
//            Battery_ProgressBar.setIndeterminate(true);
        }
    }

    /**
     * 单独刷新电量子方法
     *
     * @param msg
     */
    private void refreshBattery(String msg) {
        //提示词
        text_Battery.setText("Power "+msg );//根据电量显示不同的文字提示
//        Battery_ProgressBar.setCurProgress((int) (Math.floor(0 * 100 * 1.0f / 100)));
//        text_Battery_Progress.setText((int) (Math.floor(0 * 100 * 1.0f / 100)) + "%"); // 0%
    }

    /**
     * 单独刷新钱包子方法
     */
    private void refreshMoneyView(){
        LocalInfoVO localvo = PreferencesToolkits.getLocalDeviceInfo(PortalActivity.this);
        String money = localvo.getMoney();
        //提示词
        text_Wallet.setText(getResources().getString(R.string.menu_pay_yuan) + money);
        //进度条
        Wallet_ProgressBar.setCurProgress((int) (Math.floor(Float.parseFloat(money) * 100 * 1.0f / money_goal)));
        //进度条旁边的百分比
        text_Wallet_Progress.setText((int) (Math.floor(Float.parseFloat(money) * 100 * 1.0f / money_goal)) + "%");
    }

    /**
     * 网络请求的返回
     */
    HttpCallback<String> httpCallback = new HttpCallback<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            String result = response.get();
            MyLog.e(TAG, "response.get()=" + response.get());
            if (response.get()==null){
                return;
            }
            DataFromServer dataFromServer = JSON.parseObject(response.get(), DataFromServer.class);
            String value = dataFromServer.getReturnValue().toString();
            if (dataFromServer.getErrorCode() == 1) {
                switch (what) {
                    case CommParams.HTTP_FEEDBACK_QUERY:
                        MyLog.e(TAG, "value=" + value + "CommonUtils.getIntValue(value)" + CommonUtils.getIntValue(value));
                        if (CommonUtils.getIntValue(value) > 0) {
                            AlertDialog dialog = new AlertDialog.Builder(PortalActivity.this)
                                    .setTitle(ToolKits.getStringbyId(PortalActivity.this, R.string.main_about_adv_feedback))
                                    .setMessage(ToolKits.getStringbyId(PortalActivity.this, R.string.portal_main_new_feedback_msg))
                                    .setPositiveButton(ToolKits.getStringbyId(PortalActivity.this, R.string.general_look),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    IntentFactory.start_CustomerService_ActivityIntent(PortalActivity.this, Feedback.PAGE_INDEX_THREE);
                                                }
                                            })
                                    .setNegativeButton(ToolKits.getStringbyId(PortalActivity.this, R.string.general_cancel), null)
                                    .create();
                            dialog.show();
                        }
                        break;

                    case CommParams.HTTP_UNREAD_QUERY:
                        if (CommonUtils.getIntValue(value) > 0) {
                            //未读消息
                            MyApplication.getInstance(PortalActivity.this).setCommentNum(Integer.parseInt(value));
                        }
                        break;

                    case CommParams.HTTP_SUBMIT_BAIDU:
                        MyLog.e(TAG, "经纬度上传成功...");
                        UserEntity userAuthedInfo = new Gson().fromJson(value, UserEntity.class);
                        MyApplication.getInstance(PortalActivity.this).setLocalUserInfoProvider(userAuthedInfo);
                        refreshVISIBLE();
                        // 后台启动客户端版本检查和更新线程
                        String updateeime=PreferencesToolkits.getUpdateTime(PortalActivity.this);
                        if(updateeime.equals(new SimpleDateFormat(ToolKits.DATE_FORMAT_YYYY_MM_DD).format(new Date()))){
                            //如果是今天就不在提示更新了
                        }else {
                            new UpdateClientAsyncTask(PortalActivity.this) {
                                @Override
                                protected void relogin() {

                                }
                            }.execute();
                        }
                        break;
                    case CommParams.HTTP_UPDATA_CARDNUMBER:
                        MyLog.e(TAG, "卡号上传成功..." );
                        //不做处理
                        break;

                    case CommParams.HTTP_UPDATA_DEVICEID:
                        MyLog.e(TAG, "deviceId上传成功..." );
                        UserEntity userEntity = new Gson().fromJson(value, UserEntity.class);
                        MyApplication.getInstance(PortalActivity.this).setLocalUserInfoProvider(userEntity);
//                        CallServer.getRequestInstance().add(BleService.this, false, CommParams.HTTP_DOWN_USERENETTY, HttpHelper.createUserEntityRequest(userEntity.getUser_id() + ""), httpCallback);
                        break;

                    case CommParams.HTTP_UPDATA_MODELNAME:
                        MyLog.e(TAG, "modelname上传成功...: "+value);
                        //获取了服务器的设备信息 并且保存到本地
                        String model_name = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity().getModel_name();
                        ModelInfo modelInfo=JSONObject.parseObject(response.get(), ModelInfo.class);
                        PreferencesToolkits.saveInfoBymodelName(PortalActivity.this,model_name,modelInfo);
                        //开始处理页面是否显示
                        refreshVISIBLE();
                        break;

                    default:
                        break;
                }

            }

        }

        @Override
        public void onFailed(int what, String url, Object tag, CharSequence message, int responseCode, long networkMillis) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        inCheckShowHR();
        if (requestCode == BleService.REQUEST_ENABLE_BT) {
            switch (resultCode) {
                case Activity.RESULT_CANCELED: //用户取消打开蓝牙
                    BleService.setNEED_SCAN(false);
                    break;

                case Activity.RESULT_OK:       //用户打开蓝牙
                    Log.e(TAG, "//用户打开蓝牙");
                    BleService.setNEED_SCAN(true);
                    provider.scanForConnnecteAndDiscovery();
                    break;
                default:
                    break;
            }
            return;
        } else if (requestCode == CommParams.REQUEST_CODE_BOUND_BAND && resultCode == Activity.RESULT_OK) {
            MyLog.e(TAG, "手环绑定成功");
//            provider.setBleProviderObserver(bleProviderObserver);
//            provider.getAllDeviceInfoNew(this);
        } else if (requestCode == CommParams.REQUEST_CODE_BOUND_WATCH && resultCode == Activity.RESULT_OK) {
//            if(provider.getBleProviderObserver()==null){
//                MyLog.e(TAG, "provider.getBleProviderObserver()==null的");
//                provider.setBleProviderObserver(bleProviderObserver);
//            }
//            provider.getAllDeviceInfoNew(this);
            if (!ToolKits.isEnabled(PortalActivity.this))
                //**跳到系统设置界面
                startActivity(new Intent(NotificationCollectorService.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }


    }

    /**
     * 提示绑定的弹出框
     */
    private void showBundDialog() {
        // 您还未绑定 请您绑定一个设备
        LayoutInflater inflater = getLayoutInflater();
        View layoutbund = inflater.inflate(R.layout.modify_sex_dialog, (ViewGroup) findViewById(R.id.linear_modify_sex));
        final RadioButton hrBand = (RadioButton) layoutbund.findViewById(R.id.Hrband);
        hrBand.setTextSize(10);
        hrBand.setText(R.string.bound_link_HR_band);
        final RadioButton band = (RadioButton) layoutbund.findViewById(R.id.rb_left);
        band.setTextSize(10);
        band.setText(getString(R.string.bound_link_band));
        final RadioButton watch = (RadioButton) layoutbund.findViewById(R.id.rb_right);
        watch.setTextSize(10);
        watch.setText(getString(R.string.bound_link_watch));
        AlertDialog dialog = new AlertDialog.Builder(PortalActivity.this)
                .setTitle(ToolKits.getStringbyId(PortalActivity.this, R.string.portal_main_unbound))
                .setMessage(ToolKits.getStringbyId(PortalActivity.this, R.string.portal_main_unbound_msg))
                .setView(layoutbund)
                .setPositiveButton(ToolKits.getStringbyId(PortalActivity.this, R.string.general_ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (band.isChecked()) {
                                    dialog.dismiss();
                                    //跳转到手环绑定界面
                                    startActivityForResult(IntentFactory.startActivityBundBand(PortalActivity.this), CommParams.REQUEST_CODE_BOUND_BAND);
                                } else if(watch.isChecked()){
                                    //跳转到手表绑定界面
                                    startActivityForResult(new Intent(PortalActivity.this, BoundActivity.class), CommParams.REQUEST_CODE_BOUND_WATCH);
//                                    startActivity(new Intent(PortalActivity.this, BoundActivity.class));
                                    //请求绑定
                                    dialog.dismiss();
                                }else if(hrBand.isChecked()) {
                                    //跳转到心率手环绑定界面
                                   startActivityForResult(new Intent(PortalActivity.this, Band3ListActivity.class),20);
                                    dialog.dismiss();
                                }
                            }
                        })
                .setNegativeButton(ToolKits.getStringbyId(PortalActivity.this, R.string.general_cancel), null)
                .create();
        dialog.show();
    }

    /**
     * 蓝牙观察者实现类.
     */
    private class BLEProviderObserverAdapterImpl extends BLEHandler.BLEProviderObserverAdapter {

        @Override
        protected Activity getActivity() {
            return PortalActivity.this;
        }

        /**********用户没打开蓝牙*********/
        @Override
        public void updateFor_handleNotEnableMsg() {
            //用户未打开蓝牙
            Log.i(TAG, "updateFor_handleNotEnableMsg");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            getActivity().startActivityForResult(enableBtIntent, BleService.REQUEST_ENABLE_BT);
        }

        @Override
        public void updateFor_handleUserErrorMsg(int id) {
            MyLog.e(TAG, "updateFor_handleConnectSuccessMsg");
        }

        /**********BLE连接中*********/
        @Override
        public void updateFor_handleConnecting() {
            //正在连接
            MyLog.e(TAG, "updateFor_handleConnecting");
            refreshBatteryUI();
        }

        /**********扫描BLE设备TimeOut*********/
        @Override
        public void updateFor_handleScanTimeOutMsg() {
            MyLog.e(TAG, "updateFor_handleScanTimeOutMsg");
        }

        /**********BLE连接失败*********/
        @Override
        public void updateFor_handleConnectFailedMsg() {
            //连接失败
            MyLog.e(TAG, "updateFor_handleConnectFailedMsg");
        }

        /**********BLE连接成功*********/
        @Override
        public void updateFor_handleConnectSuccessMsg() {
            //连接成功
            MyLog.e(TAG, "updateFor_handleConnectSuccessMsg");
        }
        /**********BLE断开连接*********/
        @Override
        public void updateFor_handleConnectLostMsg() {
            MyLog.e(TAG, "updateFor_handleConnectLostMsg");
            //蓝牙断开的显示
            refreshBatteryUI();
        }

        /**********0X13命令返回*********/
        @Override
        public void updateFor_notifyFor0x13ExecSucess_D(LPDeviceInfo latestDeviceInfo) {
            MyLog.e(TAG, "updateFor_notifyFor0x13ExecSucess_D");
            if (latestDeviceInfo != null && latestDeviceInfo.recoderStatus==5) {
                new android.app.AlertDialog.Builder(PortalActivity.this)
//                        .setTitle(R.string.portal_main_gobound)
                        .setMessage(R.string.portal_main_mustbund)
                        //
                        .setPositiveButton(R.string.general_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                provider.unBoundDevice(PortalActivity.this);
                                UserEntity userEntity = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider();
                                //设备号置空
                                userEntity.getDeviceEntity().setLast_sync_device_id(null);
                                //设备类型置空
                                userEntity.getDeviceEntity().setDevice_type(0);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                provider.release();
                                provider.setCurrentDeviceMac(null);
                                provider.setmBluetoothDevice(null);
                                provider.resetDefaultState();
                            }
                        }).create().show();
            }


            if (latestDeviceInfo!=null&&latestDeviceInfo.recoderStatus==66){
                if (!CommonUtils.isStringEmpty(MyApplication.getInstance(PortalActivity.this)
                        .getLocalUserInfoProvider().getDeviceEntity().getLast_sync_device_id())){
                    if (builder!=null&&!builder.create().isShowing()) {
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                provider.unBoundDevice(PortalActivity.this);
                                try {
                                    Thread.sleep(1000);
                                    BleService.getInstance(PortalActivity.this).releaseBLE();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
//                                IntentFactory.start_Bluetooth(PortalActivity.this);
                            }
                        }).setMessage(getString(R.string.Need_before))
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                    }
                }else {
                    provider.unBoundDevice(PortalActivity.this);
                }
            }
//            //保存localvo
//            PreferencesToolkits.updateLocalDeviceInfo(PortalActivity.this, latestDeviceInfo);
        }

        @Override
        public void updateFor_notifyForDeviceUnboundSucess_D() {
            super.updateFor_notifyForDeviceUnboundSucess_D();
            Log.i("BandListActivity", "清除设备信息/解绑成功");
            refreshVISIBLE();
        }

        @Override
        public void updatefor_notifyforsendGoalSuccess() {

        }

        @Override
        public void updatefor_notifyforsendGoalFailed() {

        }


        /**********剩余同步运动条目*********/
        @Override
        public void updateFor_SportDataProcess(Integer obj) {
            super.updateFor_SportDataProcess(obj);
            MyLog.e(TAG, "updateFor_SportDataProcess");
            if(mScrollView.isRefreshing()){
                String second_txt = MessageFormat.format(getString(R.string.refresh_data), obj);
                mScrollView.getHeaderLayout().getmHeaderText().setText(second_txt);
            }
        }

        /**********运动记录读取完成*********/
        @Override
        public void updateFor_handleDataEnd() {
            MyLog.e(TAG, " updateFor_handleDataEnd ");
            //把数据库未同步到server的数据提交上去
            if (ToolKits.isNetworkConnected(PortalActivity.this)) {
                new AsyncTask<Object, Object, SportRecordUploadDTO>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @Override
                    protected SportRecordUploadDTO doInBackground(Object... params) {
                        // 看看数据库中有多少未同步（到服务端的数据）
                        final ArrayList<SportRecord> up_List = UserDeviceRecord.findHistoryWitchNoSync(PortalActivity.this, MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getUser_id() + "");
                        MyLog.e(TAG, "【NEW离线数据同步】一共查询出" +up_List.size()+"条数据");
                        //有数据才去算
                        if (up_List.size() > 0) {
                            SportRecordUploadDTO sportRecordUploadDTO = new SportRecordUploadDTO();
                            final String startTime = up_List.get(0).getStart_time();
                            final String endTime = up_List.get(up_List.size() - 1).getStart_time();
                            sportRecordUploadDTO.setDevice_id("1");
                            sportRecordUploadDTO.setUtc_time("1");
                            sportRecordUploadDTO.setOffset(TimeZoneHelper.getTimeZoneOffsetMinute() + "");
                            sportRecordUploadDTO.setUser_id(MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getUser_id());
                            sportRecordUploadDTO.setList(up_List);
                            long sychedNum = UserDeviceRecord.updateForSynced(PortalActivity.this, MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getUser_id() + "", startTime, endTime);
                            MyLog.d(TAG, "【NEW离线数据同步】本次共有" + sychedNum + "条运动数据已被标识为\"已同步\"！[" + startTime + "~" + endTime + "]");
//                            CallServer.getRequestInstance().add(PortalActivity.this, false, CommParams.HTTP_SUBMIT_DATA, HttpHelper.sportDataSubmitServer(sportRecordUploadDTO), new HttpCallback<String>() {
//                                @Override
//                                public void onSucceed(int what, Response<String> response) {
//                                    MyLog.e(TAG, "【NEW离线数据同步】服务端返回" +response.get());
//                                    DataFromServer dataFromServer =JSON.parseObject(response.get(), DataFromServer.class);
//                                    if (!ToolKits.isJSONNullObj(response.get())){
//                                        if (dataFromServer.getErrorCode()==1) {
//                                            long sychedNum = UserDeviceRecord.updateForSynced(PortalActivity.this, MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getUser_id() + "", startTime, endTime);
//                                            MyLog.d(TAG, "【NEW离线数据同步】本次共有" + sychedNum + "条运动数据已被标识为\"已同步\"！[" + startTime + "~" + endTime + "]");
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void onFailed(int what, String url, Object tag, CharSequence message, int responseCode, long networkMillis) {
//
//                                }
//                            });
                            return sportRecordUploadDTO;
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(SportRecordUploadDTO sportRecordUploadDTO) {
                        super.onPostExecute(sportRecordUploadDTO);
                    }
                }.execute();
            }
        }

        /**********消息提醒设置成功*********/
        @Override
        public void updateFor_notify() {
            super.updateFor_notify();
            MyLog.e(TAG, "消息提醒设置成功！");
        }

        @Override
        public void updateFor_notifyForModelName(LPDeviceInfo latestDeviceInfo) {
            super.updateFor_notifyForModelName(latestDeviceInfo);
            if(latestDeviceInfo.modelName==null){
                //没读取出来
                return;
            }
//            ModelInfo modelInfo = PreferencesToolkits.getInfoBymodelName(PortalActivity.this,MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity().getModel_name());
//            if(modelInfo==null){
//                //去服务器获取显示页面的bean
//                CallServer.getRequestInstance().add(PortalActivity.this, false,CommParams.HTTP_UPDATA_MODELNAME, NoHttpRuquestFactory.createModelRequest(MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getUser_id(),latestDeviceInfo.modelName), httpCallback);
//            }else{
//                refreshVISIBLE();
//            }



            if((System.currentTimeMillis()/1000)-PreferencesToolkits.getOADUpdateTime(getActivity())>86400)
            {
                // 查询是否要更新固件
                final LocalInfoVO vo = PreferencesToolkits.getLocalDeviceInfo(PortalActivity.this);
                FirmwareDTO firmwareDTO = new FirmwareDTO();
                int deviceType = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity().getDevice_type();
                if(deviceType ==MyApplication.DEVICE_BAND || deviceType ==MyApplication.DEVICE_BAND - 1){
                    deviceType = 1;
                }else{
                    deviceType = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity().getDevice_type();
                }
                firmwareDTO.setDevice_type(deviceType);
                firmwareDTO.setFirmware_type(DeviceActivity.DEVICE_VERSION_TYPE);
                int version_int = ToolKits.makeShort(vo.version_byte[1], vo.version_byte[0]);
                firmwareDTO.setVersion_int(version_int + "");
                firmwareDTO.setModel_name(latestDeviceInfo.modelName);
                if(MyApplication.getInstance(PortalActivity.this).isLocalDeviceNetworkOk()){
                    //请求网络
                    CallServer.getRequestInstance().add(PortalActivity.this, false, CommParams.HTTP_OAD, NoHttpRuquestFactory.create_OAD_Request(firmwareDTO), new HttpCallback<String>() {
                        @Override
                        public void onSucceed(int what, Response<String> response) {
                            DataFromServer dataFromServer = JSON.parseObject(response.get(), DataFromServer.class);
                            String value = dataFromServer.getReturnValue().toString();
                            if(!CommonUtils.isStringEmpty(response.get())) {
                                if (dataFromServer.getErrorCode() != 10020) {
                                    JSONObject object = JSON.parseObject(value);
                                    String version_code = object.getString("version_code");
                                    if (Integer.parseInt(version_code, 16) > Integer.parseInt(vo.version, 16)) {
                                        PreferencesToolkits.setOADUpdateTime(PortalActivity.this);
                                        AlertDialog dialog = new AlertDialog.Builder(PortalActivity.this)
                                                .setTitle(ToolKits.getStringbyId(PortalActivity.this, R.string.general_tip))
                                                .setMessage(ToolKits.getStringbyId(PortalActivity.this, R.string.bracelet_oad_Portal))
                                                .setPositiveButton(ToolKits.getStringbyId(PortalActivity.this, R.string.general_ok),
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                startActivity(IntentFactory.star_DeviceActivityIntent(PortalActivity.this,DeviceActivity.DEVICE_UPDATE));
                                                            }
                                                        })
                                                .setNegativeButton(ToolKits.getStringbyId(PortalActivity.this, R.string.general_cancel), null)
                                                .create();
                                        dialog.show();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailed(int what, String url, Object tag, CharSequence message, int responseCode, long networkMillis) {

                        }
                    });
                }
            }

        }

        /**********闹钟提醒设置成功*********/
        @Override
        public void updateFor_notifyForSetClockSucess() {
            super.updateFor_notifyForSetClockSucess();
            MyLog.e(TAG, "updateFor_notifyForSetClockSucess！");
        }

        /**********久坐提醒设置成功*********/
        @Override
        public void updateFor_notifyForLongSitSucess() {
            super.updateFor_notifyForLongSitSucess();
            MyLog.e(TAG, "updateFor_notifyForLongSitSucess！");
        }

        /**********身体信息(激活设备)设置成功*********/
        @Override
        public void updateFor_notifyForSetBodySucess() {
            MyLog.e(TAG, "updateFor_notifyForSetBodySucess");
            refreshBatteryUI();
        }

        /**********设置时间失败*********/
        @Override
        public void updateFor_handleSetTimeFail() {
            MyLog.e(TAG, "updateFor_handleSetTimeFail");
            super.updateFor_handleSetTimeFail();
        }

        /**********设置时间成功*********/
        @Override
        public void updateFor_handleSetTime() {
            MyLog.e(TAG, "updateFor_handleSetTime");
            mScrollView.getHeaderLayout().getmHeaderText().setText(getString(R.string.refresh_time));
            getInfoFromDB();
        }
//        @Override
//        public void updateFor_notifyForDeviceFullSyncSucess_D(LPDeviceInfo deviceInfo) {
//            PreferencesToolkits.updateLocalDeviceInfo(PortalActivity.this, deviceInfo);
//            MyLog.e(TAG, "updateFor_notifyForDeviceFullSyncSucess_D");
//        }

        /**********获取设备ID*********/
        @Override
        public void updateFor_getDeviceId(String obj) {
            super.updateFor_getDeviceId(obj);
            MyLog.e(TAG, "读到的deviceid:" + obj);
            //如果读到的卡号 并且本地还没有设置
            if(MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity()==null){
                return;
            }
            if(MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getEntEntity()==null || !CommonUtils.isStringEmpty(MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getEntEntity().getEnt_name())){
                return;
            }
            if (MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity().getLast_sync_device_id2()==null || !MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity().getLast_sync_device_id2().equals(obj) ) {
                /***本地***/
                MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity().setLast_sync_device_id2(obj);
                /***云端***/
//                CallServer.getRequestInstance().add(PortalActivity.this, false, CommParams.HTTP_UPDATA_DEVICEID,
//                        HttpHelper.createUpDeviceIdRequest(MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getUser_id() + "", provider.getCurrentDeviceMac(), obj), httpCallback);
            }
        }

        /**********卡号读取成功*********/
        @Override
        public void updateFor_CardNumber(String cardId) {
            MyLog.e(TAG, "updateFor_CardNumber："+cardId);
            super.updateFor_CardNumber(cardId);
            /*****************拿到卡号后存储过程START*****************/
            UserEntity userEntity = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider();
            if(userEntity.getDeviceEntity()==null){
                return;
            }
            if (userEntity.getDeviceEntity().getCard_number()==null || userEntity.getDeviceEntity().getCard_number().equals("") || !userEntity.getDeviceEntity().getCard_number().equals(cardId)){
                MyLog.e(TAG, "卡号不是空 开始报存卡号:" + cardId);
                userEntity.getDeviceEntity().setCard_number(cardId);
                /***本地***/
                MyApplication.getInstance(PortalActivity.this).setLocalUserInfoProvider(userEntity);
                /***云端***/
                //拿到卡号存到服务器去
//                CallServer.getRequestInstance().add(PortalActivity.this, false, CommParams.HTTP_UPDATA_CARDNUMBER, HttpHelper.createUpCardNumberRequest(userEntity.getUser_id() + "", cardId), httpCallback);
                /******************拿到卡号后存储过程OVER*******************/
            }
            mScrollView.onRefreshComplete();
        }


        /**********上电成功*********/
        @Override
        public void updateFor_OpenSmc(boolean isSuccess) {
            super.updateFor_OpenSmc(isSuccess);
            MyLog.e(TAG, "开卡成功！");
            if (isSuccess) {
                provider.AIDSmartCard(PortalActivity.this, deviceInfo);
            }else{
                MyLog.e(TAG, "开卡失败--------------------------------------------");
            }
        }

        /**********AID*********/
        @Override
        public void updateFor_AIDSmc(boolean isSuccess) {
            super.updateFor_AIDSmc(isSuccess);
            if (isSuccess) {
                //读余额
                provider.PINSmartCard(PortalActivity.this, deviceInfo);
            }
        }

        /**********校验PIN*********/
        @Override
        public void updateFor_checkPINSucess_D() {
            super.updateFor_checkPINSucess_D();
            provider.readCardBalance(PortalActivity.this, deviceInfo);
        }

        /**********余额读取成功*********/
        @Override
        public void updateFor_GetSmcBalance(Integer obj) {
            super.updateFor_GetSmcBalance(obj);
            MyLog.e(TAG, "updateFor_GetSmcBalance：");
//            String balance = ToolKits.stringtofloat(obj+"")+"";
//            MyLog.e(TAG,"读出来的余额是:"+balance);
            provider.closeSmartCard(PortalActivity.this);
            //把余额保存到本地 方便主界面显示
            LocalInfoVO localvo = PreferencesToolkits.getLocalDeviceInfo(PortalActivity.this);
            localvo.setMoney(ToolKits.inttoStringMoney(obj));
            PreferencesToolkits.setLocalDeviceInfoVo(PortalActivity.this, localvo);
            //此时调用是为了刷新金额
            refreshMoneyView();
        }
    }

    private void getMoneyfromDevice() {
        UserEntity userEntity = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider();
        if (userEntity.getDeviceEntity() == null || userEntity.getDeviceEntity().getCard_number() == null) {
            return;
        } else {
            String card = userEntity.getDeviceEntity().getCard_number();
            if (card.startsWith(LPDeviceInfo.SUZHOU_)) {
                MyLog.e(TAG, "刷新===页面数据===和===钱包===");
                deviceInfo.customer = LPDeviceInfo.SUZHOU_;   //苏州
                    if (provider.isConnectedAndDiscovered()) {
                        provider.closeSmartCard(PortalActivity.this);
                        // 首先清空集合
                    provider.openSmartCard(PortalActivity.this);
                }
            }else if(card.startsWith(LPDeviceInfo.LIUZHOU_4) || card.startsWith(LPDeviceInfo.LIUZHOU_5)){
                deviceInfo.customer = LPDeviceInfo.LIUZHOU_4;   //柳州
                if (provider.isConnectedAndDiscovered()) {
                    provider.closeSmartCard(PortalActivity.this);
                    // 首先清空集合
                    provider.openSmartCard(PortalActivity.this);
                }
            }
        }
    }


}

package com.linkloving.taiwan.logic.UI.device;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.android.bluetoothlegatt.BLEHandler;
import com.example.android.bluetoothlegatt.BLEProvider;
import com.example.android.bluetoothlegatt.proltrol.LepaoProtocalImpl;
import com.example.android.bluetoothlegatt.proltrol.dto.LPDeviceInfo;
import com.linkloving.taiwan.BleService;
import com.linkloving.taiwan.CommParams;
import com.linkloving.taiwan.IntentFactory;
import com.linkloving.taiwan.MyApplication;
import com.linkloving.taiwan.R;
import com.linkloving.taiwan.RetrofitUtils.Bean.CheckFirmwareVersionReponse;
import com.linkloving.taiwan.RetrofitUtils.FirmwareRetrofitClient;
import com.linkloving.taiwan.RetrofitUtils.RetrofitApi.OADApi;
import com.linkloving.taiwan.basic.toolbar.ToolBarActivity;
import com.linkloving.taiwan.http.basic.CallServer;
import com.linkloving.taiwan.http.basic.HttpCallback;
import com.linkloving.taiwan.http.basic.NoHttpRuquestFactory;
import com.linkloving.taiwan.http.data.DataFromServer;
import com.linkloving.taiwan.logic.UI.OAD.DfuService;
import com.linkloving.taiwan.logic.UI.main.PortalActivity;
import com.linkloving.taiwan.logic.UI.main.bundband.Band3ListActivity;
import com.linkloving.taiwan.logic.dto.UserEntity;
import com.linkloving.taiwan.notify.NotificationCollectorService;
import com.linkloving.taiwan.prefrences.PreferencesToolkits;
import com.linkloving.taiwan.prefrences.devicebean.LocalInfoVO;
import com.linkloving.taiwan.utils.CommonUtils;
import com.linkloving.taiwan.utils.MyToast;
import com.linkloving.taiwan.utils.ToolKits;
import com.linkloving.taiwan.utils.logUtils.MyLog;
import com.yolanda.nohttp.Headers;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.Response;
import com.yolanda.nohttp.StringRequest;
import com.yolanda.nohttp.download.DownloadListener;
import com.yolanda.nohttp.download.DownloadRequest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.OnClick;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class DeviceActivity extends ToolBarActivity implements View.OnClickListener {
    private static final String TAG = DeviceActivity.class.getSimpleName();
    private LinearLayout activity_own_alarm, activity_own_incoming_tel, activity_own_longsit, activity_own_control,
            activity_own_power,activity_own_find, activity_own_update, activity_own_delete;
    private ImageView device_img;
    private TextView DeviceMac;
    private TextView boundDeviceVerson;
    private TextView syncDeviceTime;
    private Button mButton;
    //蓝牙提供者
    private BLEProvider provider;
    LocalInfoVO vo;
    BLEHandler.BLEProviderObserverAdapter observerAdapter = null;
    private boolean canoad = false;
    private boolean RINGING;
    private int RINGCOUNT = 10;
    private int send_index = 0;
    /**
     * 控制dialog
     */
    private boolean click_oad = false; //以前出现过切换到此页面也出现dialog的情况
    private byte[] data;
    public final static int DEVICE_TYPE_WATCH = 3;
    public final static int DEVICE_VERSION_TYPE = DEVICE_TYPE_WATCH - 1;
    public final static int DEVICE_UPDATE = 666;
    private ProgressDialog dialog_syn = null;//同步
    private String file_name_OAD;
    private ProgressDialog progressDialog;
    private ProgressDialog dialog;//网络获取版本信息
    private ProgressDialog dialog_connect;//下载进度
    private int persent;
    UserEntity userEntity;
//    private Blereciver blereciver;
    boolean isRunning = false;
    private URI uri = null;
    private File file;
    private ProgressBar progressbar;
    private TextView progressInt;
    private AlertDialog downloadingProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        userEntity=MyApplication.getInstance(DeviceActivity.this).getLocalUserInfoProvider();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        ButterKnife.inject(this);
        observerAdapter = new BLEProviderObserverAdapterImpl();
        vo = PreferencesToolkits.getLocalDeviceInfo(DeviceActivity.this);
        provider = BleService.getInstance(this).getCurrentHandlerProvider();
        provider.setBleProviderObserver(observerAdapter);
        dialog_syn = new ProgressDialog(DeviceActivity.this);
        dialog_syn.setMessage(getString(R.string.bracelet_oad_syn));
        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.getting_version_information));
        dialog_connect=new ProgressDialog(this);

    }

//    private class Blereciver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(BleService.BLE_SYN_SUCCESS) && ToolKits.isApplicationBroughtToBackground(DeviceActivity.this)) {
//                MyLog.e(TAG, "onReceive BleService.BLE_SYN_SUCCESS");
//                if (click_oad) {
//                    if (ToolKits.isNetworkConnected(DeviceActivity.this)) {
//                        // MyToast.showLong(DeviceActivity.this, "同步完成开始OAD");
//                        //同步完成开始OAD
//                        //开始OAD的AsyncTask,请求网络.获取版本信息与当前的版本信息比较
////                        beforeOAD();
//                    } else {
//                        //没有连接网络
//                        MyToast.showLong(DeviceActivity.this, getString(R.string.bracelet_oad_failed_msg));
//                    }
//                    click_oad = false;
//                    if (dialog_syn != null || dialog_syn.isShowing())
//                        dialog_syn.dismiss();
//                }
//            }
//        }
//    }

    @Override
    protected void getIntentforActivity() {
    }

    @Override
    protected void initView() {
        SetBarTitleText(getString(R.string.watch_setting));
     /*   provider = BleService.getInstance(this).getCurrentHandlerProvider();
        bleProviderObserver = new BLEProviderObserverAdapterImpl();
        provider.setBleProviderObserver(bleProviderObserver);*/
        String v = ToolKits.getStringbyId(DeviceActivity.this, R.string.bracelet_version);
        vo = PreferencesToolkits.getLocalDeviceInfo(DeviceActivity.this);
        vo.getVersion();
        device_img = (ImageView) findViewById(R.id.device_img);
        activity_own_alarm = (LinearLayout) findViewById(R.id.activity_own_alarm);
        activity_own_incoming_tel = (LinearLayout) findViewById(R.id.activity_own_incoming_tel);
        activity_own_longsit = (LinearLayout) findViewById(R.id.activity_own_longsit);
        activity_own_control = (LinearLayout) findViewById(R.id.activity_own_control);
        activity_own_power = (LinearLayout) findViewById(R.id.activity_own_power);
        activity_own_find = (LinearLayout) findViewById(R.id.activity_own_find);
        if(MyApplication.getInstance(this).getLocalUserInfoProvider().getDeviceEntity().getDevice_type() == MyApplication.DEVICE_WATCH)
            activity_own_find.setVisibility(View.GONE);
        activity_own_update = (LinearLayout) findViewById(R.id.activity_own_update);
        activity_own_delete = (LinearLayout) findViewById(R.id.activity_own_delete);
        if(MyApplication.getInstance(this).getLocalUserInfoProvider().getDeviceEntity().getDevice_type()==MyApplication.DEVICE_BAND){
            //手环用户 隐藏来电 勿扰 电源
            activity_own_incoming_tel.setVisibility(View.GONE);
            activity_own_control.setVisibility(View.GONE);
            activity_own_power.setVisibility(View.GONE);
            device_img.setImageDrawable(getResources().getDrawable(R.mipmap.band_logo));
        }else if(MyApplication.getInstance(this).getLocalUserInfoProvider().getDeviceEntity().getDevice_type()==MyApplication.DEVICE_BAND3){
            device_img.setImageDrawable(getResources().getDrawable(R.mipmap.band3_logo));
        }else {
            device_img.setImageDrawable(getResources().getDrawable(R.mipmap.device_watch));
        }
        DeviceMac = (TextView) findViewById(R.id.tv_Mac);
        boundDeviceVerson = (TextView) findViewById(R.id.tv_verson);
        syncDeviceTime = (TextView) findViewById(R.id.tv_sync);
        DeviceMac.setText("MAC:" + MyApplication.getInstance(this).getLocalUserInfoProvider().getDeviceEntity().getLast_sync_device_id());
        if (!com.linkloving.taiwan.utils.CommonUtils.isStringEmpty(vo.version))
            boundDeviceVerson.setText(getString(R.string.brace_version) + vo.version);
        else
            boundDeviceVerson.setText(MessageFormat.format(v, "Unknow"));
        if (vo.syncTime > 0) {
            syncDeviceTime.setText(new SimpleDateFormat(ToolKits.getStringbyId(DeviceActivity.this, R.string.bracelet_sync_format)).format(new Date(vo.syncTime)) + "");
        } else {
            syncDeviceTime.setText(ToolKits.getStringbyId(DeviceActivity.this, R.string.bracelet_sync_none));
        }
        MyLog.e(TAG, "同步时间：" + new SimpleDateFormat(ToolKits.getStringbyId(DeviceActivity.this, R.string.bracelet_sync_format)).format(new Date(0)));

    }


    @Override
    public void onResume() {
        isRunning = true;
        if (provider.getBleProviderObserver() == null) {
            provider.setBleProviderObserver(observerAdapter);
        }
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }




    @Override
    protected void initListeners() {
        activity_own_alarm.setOnClickListener(this);
        activity_own_incoming_tel.setOnClickListener(this);
        activity_own_longsit.setOnClickListener(this);
        activity_own_control.setOnClickListener(this);
        activity_own_power.setOnClickListener(this);
        activity_own_find.setOnClickListener(this);
        activity_own_update.setOnClickListener(this);
        activity_own_delete.setOnClickListener(this);

        int type = getIntent().getIntExtra("type",0);
        if(type == DEVICE_UPDATE){
            activity_own_update.performClick();
        }
    }

    @OnClick(R.id.device_img)
    void toSendEmail(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(DeviceActivity.this).inflate(R.layout.alert_dialog_edittext, null);
        final EditText password = (EditText) dialogView.findViewById(R.id.edit_screet);
        builder.setTitle("请输入密码").setView(dialogView)
                .setNegativeButton("取消", null);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (password.getText().toString().equals("wisfit")) {
                    toSendEmail();
                } else if (password.getText().toString().length() == 0) {
                    Toast.makeText(DeviceActivity.this, "Please enter code", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(DeviceActivity.this, "Wrong code", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    private void toSendEmail() {
        GetDBInfo.getDBInfoToFile(DeviceActivity.this,"HeartRate.txt");
    }

    @OnClick(R.id.textView3)
    void sendException(View view){
        GetDBInfo.toSendEmail(DeviceActivity.this,GetDBInfo.getDiskCacheDir(DeviceActivity.this)+"/"+"except.txt");
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (provider.getBleProviderObserver() == null) {
            provider.setBleProviderObserver(observerAdapter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning= false;
//        unregisterReceiver(blereciver);
        provider.setBleProviderObserver(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_own_alarm:
                startActivity(IntentFactory.start_AlarmActivityIntent(DeviceActivity.this));
                break;
            case R.id.activity_own_incoming_tel:
                IntentFactory.start_IncomingTel(DeviceActivity.this);
                break;
            case R.id.activity_own_longsit:
                IntentFactory.start_LongSitActivityIntent(DeviceActivity.this);
                break;
            case R.id.activity_own_control:
//                勿扰模式
                IntentFactory.start_HandUpActivity(DeviceActivity.this);
                break;
            case R.id.activity_own_power:
                IntentFactory.start_PowerActivity(DeviceActivity.this);
                break;
            case R.id.activity_own_find:
                if(!RINGING){
                    RINGING = true ;
                    final Timer timer = new Timer(); // 每分钟更新一下蓝牙状态
                    timer.schedule(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            provider.SetBandRing(DeviceActivity.this);
                            send_index++;
                            if(send_index == RINGCOUNT ){
                                send_index = 0;
                                timer.cancel();
                                RINGING = false;
                            }
                        }
                    }, 0, 1000);
                }

                break;

            case R.id.activity_own_update:
                final int device_type = MyApplication.getInstance(DeviceActivity.this).getLocalUserInfoProvider().getDeviceEntity().getDevice_type();
                AlertDialog dialog = new AlertDialog.Builder(DeviceActivity.this)
                        .setTitle(getString(R.string.update_firmware))
                        .setMessage(getString(R.string.update_firmware)+"?")
                        .setPositiveButton(ToolKits.getStringbyId(DeviceActivity.this, R.string.general_yes),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if (ToolKits.isNetworkConnected(DeviceActivity.this)) {
                                            if (vo.battery < 3) {
                                                MyToast.showLong(DeviceActivity.this, getString(R.string.bracelet_oad_failed_battery));
                                            } else {
                                                if (provider.isConnectedAndDiscovered()) {
                                                    if (device_type==MyApplication.DEVICE_BAND3){
//                                                        downloadZip();
                                                        BleService.getInstance(DeviceActivity.this).syncAllDeviceInfo(DeviceActivity.this);
                                                        dialog_connect = new ProgressDialog(DeviceActivity.this);
                                                        dialog_connect.show();
                                                    }else {
                                                        click_oad = true;
                                                        dialog_syn.show();
                                                        //会在updateFor_handleSetTime()回调中开始真正的空中升级流程
                                                    BleService.getInstance(DeviceActivity.this).syncAllDeviceInfo(DeviceActivity.this);
                                                    }
                                                } else {
                                                    //没有连接蓝牙
                                                    MyToast.showLong(DeviceActivity.this, getString(R.string.pay_no_connect));
                                                }
                                            }
                                        } else {
                                            new AlertDialog.Builder(DeviceActivity.this).setMessage(R.string.net_work_error)
                                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                                        ToolKits.setNetworkMethod(DeviceActivity.this);
                                                        }
                                                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                        }


                                    }
                                })
                        .setNegativeButton(ToolKits.getStringbyId(DeviceActivity.this, R.string.general_no), null)
                        .create();
                dialog.show();
                break;
            case R.id.activity_own_delete:
                    AlertDialog unbundDialog = new AlertDialog.Builder(DeviceActivity.this)
                            .setTitle(ToolKits.getStringbyId(DeviceActivity.this, R.string.bracelet_unbound))
                            .setMessage(ToolKits.getStringbyId(DeviceActivity.this, R.string.bracelet_unbound_msg))
                            .setPositiveButton(ToolKits.getStringbyId(DeviceActivity.this, R.string.general_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //请求解绑设备
                                        if (provider.isConnectedAndDiscovered()) {
                                            //连接才去下这个指令
                                            provider.unBoundDevice(DeviceActivity.this);
                                        }else {
                                            BleService.getInstance(DeviceActivity.this).releaseBLE();
                                        }
                                        if (userEntity.getDeviceEntity().getDevice_type() == MyApplication.DEVICE_WATCH && ToolKits.isEnabled(DeviceActivity.this)) {
                                            startActivity(new Intent(NotificationCollectorService.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                                            ToolKits.showCommonTosat(DeviceActivity.this, true, ToolKits.getStringbyId(DeviceActivity.this, R.string.unbound_success_msg), Toast.LENGTH_LONG);
                                        }else{
                                            ToolKits.showCommonTosat(DeviceActivity.this, true, ToolKits.getStringbyId(DeviceActivity.this, R.string.unbound_success), Toast.LENGTH_LONG);
                                        }
                                        UserEntity userEntity = MyApplication.getInstance(DeviceActivity.this).getLocalUserInfoProvider();
                                        //设备号置空
                                        userEntity.getDeviceEntity().setLast_sync_device_id(null);
                                        //设备类型置空
                                        userEntity.getDeviceEntity().setDevice_type(0);
                                        MyLog.e(TAG, "====HTTP_UNBUND====userEntity=" + userEntity.toString());
                                        //*******模拟断开   不管有没有连接 先执行这个再说
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                        finish();
                                }
                            })
                            .setNegativeButton(ToolKits.getStringbyId(DeviceActivity.this, R.string.general_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    unbundDialog.show();
                    break;

        }
    }


    private void downloadZip(String modelname) {
        if (modelname==null) return;
        dialog = new ProgressDialog(DeviceActivity.this);
        dialog.setMessage(getString(R.string.getting_version_information));
        vo = PreferencesToolkits.getLocalDeviceInfo(DeviceActivity.this);
        int version_int = ToolKits.makeShort(vo.version_byte[1], vo.version_byte[0]);
        CallServer.getRequestInstance().add(DeviceActivity.this, false,
                CommParams.HTTP_OAD, NoHttpRuquestFactory.creat_New_OAD_Request(modelname
                        ,version_int), newHttpCallback);
    }


    private HttpCallback<String> newHttpCallback = new HttpCallback<String>() {
        @Override
        public void onFailed(int what, String url, Object tag, CharSequence message, int responseCode, long networkMillis) {
            MyLog.e(TAG,"failed________"+message.toString());
            dialog.dismiss();
        }

        @Override
        public void onSucceed(int what, Response<String> response) {
            dialog.dismiss();
            MyLog.e(TAG + "devicefragment", response.toString() );
            if (response.get()!=null&&!response.get().isEmpty()) {
                try {
                    CheckFirmwareVersionReponse checkVersionReponse = JSONObject.parseObject(response.get(), CheckFirmwareVersionReponse.class);
                    if(checkVersionReponse.getModel_name()==null){
                        MyToast.show(DeviceActivity.this, getString(R.string.bracelet_oad_version_top), Toast.LENGTH_LONG);
                    }else {
//                        powerManager = (PowerManager) activity.getSystemService(Service.POWER_SERVICE);
//                        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Lock");
//                        //是否需计算锁的数量
//                        wakeLock.setReferenceCounted(false);
//                        //请求屏幕常亮，onResume()方法中执行
//                        wakeLock.acquire();
                        downLoadByRetrofit(checkVersionReponse.getModel_name(),
                                checkVersionReponse.getFile_name(),Integer.parseInt(checkVersionReponse.getVersion_code()) ,
                                "downloaddyh08.zip",false);
                    }
                }catch (Exception e){
                    dialog.dismiss();
                    MyToast.show(DeviceActivity.this, getString(R.string.bracelet_oad_version_top), Toast.LENGTH_LONG);
                }
            } else {
                dialog.dismiss();
                MyToast.show(DeviceActivity.this,  getString(R.string.bracelet_oad_version_top), Toast.LENGTH_LONG);
            }
        }
    };

    public void downLoadByRetrofit(String model_name, String file_name, int version_int, final String saveFileName, final boolean OADDirect) {
//        String message = getString(R.string.general_uploadingnewfirmware);
        String message = getString(R.string.downloading);
        dialog_connect = new ProgressDialog(DeviceActivity.this);
        dialog_connect.setMessage(message);
        dialog_connect.setCancelable(false);
        dialog_connect.show();
        OADApi oadApi = FirmwareRetrofitClient.getInstance().create(OADApi.class);
        HashMap<String, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("model_name", model_name);
        objectObjectHashMap.put("file_name", file_name);
        String versionString = version_int + "";
        if (versionString.length()%2==1){
            versionString = "0"+versionString ;
        }
        objectObjectHashMap.put("version_code", versionString);

        Call<ResponseBody> responseBodyCall = oadApi.download_file("close",objectObjectHashMap);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                MyLog.e(TAG+"length", response.body().byteStream() + "");
                try {
                    InputStream is = response.body().byteStream();
                    file = getTempFile(DeviceActivity.this, saveFileName);
                    uri = file.toURI();
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = bis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();
                    fos.close();
                    bis.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.e(TAG, "下载完成" + saveFileName);
                dialog_connect.dismiss();
                onUploadClicked();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                MyLog.e(TAG, t.toString());
                dialog_connect.dismiss();
                Toast.makeText(DeviceActivity.this, getString(R.string.bracelet_down_file_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onUploadClicked() {
        MyLog.e(TAG, "onUploadClicked执行了");
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceActivity.this);
        View view = LayoutInflater.from(DeviceActivity.this).inflate(R.layout.progress_dialog, null);
        progressbar = (ProgressBar) view.findViewById(R.id.progressbar);
        progressInt = (TextView) view.findViewById(R.id.progressInt);
        builder.setView(view);
        downloadingProgressDialog = builder.create();
        downloadingProgressDialog.setCancelable(false);
        downloadingProgressDialog.show();
        DfuServiceInitiator starter = new DfuServiceInitiator(userEntity.getDeviceEntity().getLast_sync_device_id())
                .setDeviceName("DYH_01")
                .setKeepBond(false)
                .setForceDfu(false)
                .setPacketsReceiptNotificationsEnabled(true)
                .setPacketsReceiptNotificationsValue(12);
        starter.setZip(file.getPath());
        starter.start(this, DfuService.class);
    }

    //固件更新
    private final DfuProgressListenerAdapter mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            progressbar.setProgress(percent);
            progressInt.setText(percent+"%");
            MyLog.e(TAG, "mDfuProgressListener" + percent + "----");
        }

        @Override
        public void onDfuCompleted(String deviceAddress) {
            super.onDfuCompleted(deviceAddress);
            MyLog.e(TAG, "mDfuProgressListener" + "---onDfuCompleted-");
            downloadingProgressDialog.dismiss();
            Toast.makeText(DeviceActivity.this,getString(R.string.user_info_update_success),Toast.LENGTH_SHORT).show();
            provider.connect();
        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            super.onError(deviceAddress, error, errorType, message);
            MyLog.e(TAG, "mDfuProgressListener" + "--onError--");
            downloadingProgressDialog.dismiss();
            Toast.makeText(DeviceActivity.this,getString(R.string.user_info_update_failure),Toast.LENGTH_SHORT).show();
        }
    };


    public File getTempFile(Context context, String name) {
        File file = null;
        try {
            file = new File(context.getCacheDir(),name);
        } catch (Exception e) {
            // Error while creating file
        }
        return file;
    }


    /**
     * 蓝牙观察者实现类.
     */
    private class BLEProviderObserverAdapterImpl extends BLEHandler.BLEProviderObserverAdapter {
        @Override
        protected Activity getActivity() {
            return DeviceActivity.this;
        }

        /************************
         * OAD头发送成功
         *************************/
        @Override
        public void updateFor_notifyCanOAD_D() {
            //禁止 消息提醒 和 定时器扫描  的全局变量
            BleService.getInstance(DeviceActivity.this).setCANCLE_ANCS(true);
            if (provider.isConnectedAndDiscovered()) {
                canoad = true;
                provider.OADDevice(DeviceActivity.this, data);
            }

        }

        @Override
        public void updateFor_notifyForDeviceUnboundSucess_D() {
            super.updateFor_notifyForDeviceUnboundSucess_D();
            BleService.getInstance(DeviceActivity.this).releaseBLE();
        }

        /************************OAD头发送成功*************************/

        /************************
         * OAD失败
         *****************************/
        @Override
        public void updateFor_notifyNOTCanOAD_D() {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            handler.removeCallbacks(runnable);
            MyLog.e(TAG, "OAD 失败");
            //Toast.makeText(DeviceActivity.this, "OAD 升级失败！", Toast.LENGTH_SHORT).show();
          /*  progessWidget.msg.setText( "OAD 升级失败！");
            progessWidget.syncFinish(false);*/
            canoad = false;
            //取消禁止 消息提醒 和 定时器扫描 的全局变量
            BleService.getInstance(DeviceActivity.this).setCANCLE_ANCS(false);//取消禁止 消息提醒 和 定时器扫描 的全局变量NCS(false);
        }
        /************************OAD失败****************************/
        /************************
         * OAD成功
         ****************************/
        @SuppressWarnings("static-access")
        @Override
        public void updateFor_notifyOADSuccess_D() {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            handler.removeCallbacks(runnable);
            //取消禁止 消息提醒 和 定时器扫描 的全局变量
            BleService.getInstance(DeviceActivity.this).setCANCLE_ANCS(false);
            MyLog.e(TAG, "OAD升级成功!");
          /*  mLepaoProtocalImpl.OAD_percent=(data.length-17)/16;
            progessWidget.syncFinish(true);*/
            canoad = false;
            //成功后跳转到主界面 START
            Intent intent = new Intent();
            intent.setClass(DeviceActivity.this, PortalActivity.class);
            startActivity(intent);
            finish();
            //成功后跳转到主界面 OVER
        }
        /************************OAD成功****************************/
        /*************************
         * TEST(烧制flash)
         *******************/
        @Override
        public void updateFor_FlashHeadSucess() {
            provider.flashbody(getActivity(), data);
        }
        /*************************TEST(烧制flash)*******************/
        /**
         * 点击固件更新按钮---->需要走同步流程---->在同步完运动数据后--->设置时间
         * 所以在设置时间的回调里面开始真正的OAD流程
         */
        @Override
        public void updateFor_handleSetTime() {
            super.updateFor_handleSetTime();
            if (click_oad) {
                if (ToolKits.isNetworkConnected(DeviceActivity.this)) {
                    // MyToast.showLong(DeviceActivity.this, "同步完成开始OAD");
                    //同步完成开始OAD
                    //开始OAD的AsyncTask,请求网络.获取版本信息与当前的版本信息比较
                    beforeOAD();
                } else {
                    //没有连接网络
                    MyToast.showLong(DeviceActivity.this, getString(R.string.bracelet_oad_failed_msg));
                }
                click_oad = false;
                if (dialog_syn != null || dialog_syn.isShowing())
                    dialog_syn.dismiss();
            }
        }


        @Override
        public void updateFor_notifyForModelName(LPDeviceInfo latestDeviceInfo) {
            super.updateFor_notifyForModelName(latestDeviceInfo);
            if (dialog_connect!=null&&dialog_connect.isShowing()) {
                dialog_connect.dismiss();
            }
            downloadZip(latestDeviceInfo.modelName);
        }

        @Override
        public void updatefor_notifyforsendGoalSuccess() {

        }

        @Override
        public void updatefor_notifyforsendGoalFailed() {

        }

        @Override
        public void updateFor_handleSendDataError() {
            super.updateFor_handleSendDataError();
            //取消禁止 消息提醒 和 定时器扫描 的全局变量
            BleService.getInstance(DeviceActivity.this).setCANCLE_ANCS(false);
            if (dialog_syn != null || dialog_syn.isShowing())
                dialog_syn.dismiss();
            if (dialog_connect!=null&&dialog_connect.isShowing()) {
                dialog_connect.dismiss();
            }
        }

        /**************************
         * 连接失败
         ***********************/
        //在OAD过程中的话    就直接在界面上面显示失败
        @Override
        public void updateFor_handleConnectLostMsg() {
            super.updateFor_handleConnectLostMsg();
            MyLog.e(TAG, "handleConnectLostMsg()!" + canoad);
            if (canoad) {
                provider.clearProess();
                MyToast.showLong(DeviceActivity.this, getString(R.string.bracelet_oad_reason));
              /*  progessWidget.msg.setText(ToolKits.getStringbyId(DeviceActivity.this, R.string.bracelet_oad_failed));
                progessWidget.syncFinish(false);*/
                canoad = false;
            }
            if (dialog_connect!=null&&dialog_connect.isShowing()) {
                dialog_connect.dismiss();
            }
        }
        /**************************连接失败***********************/
    }

    private void beforeOAD() {
        dialog.show();
        FirmwareDTO firmwareDTO = new FirmwareDTO();
        int deviceType = MyApplication.getInstance(this).getLocalUserInfoProvider().getDeviceEntity().getDevice_type();
        if(deviceType ==MyApplication.DEVICE_BAND || deviceType ==MyApplication.DEVICE_BAND - 1){
            deviceType = 1;
        }else{
            deviceType = MyApplication.getInstance(this).getLocalUserInfoProvider().getDeviceEntity().getDevice_type();
        }
        firmwareDTO.setDevice_type(deviceType);
        firmwareDTO.setFirmware_type(DEVICE_VERSION_TYPE);
        int version_int = ToolKits.makeShort(vo.version_byte[1], vo.version_byte[0]);
        MyLog.e(TAG,"vo.version="+vo.version);
        firmwareDTO.setVersion_int(version_int + "");
        MyLog.e(TAG,"vo.getModelname()="+vo.getModelname());
        firmwareDTO.setModel_name(vo.getModelname());
        MyLog.e(TAG, "device_type:" + DEVICE_TYPE_WATCH + "====firmware_type===:" + DEVICE_VERSION_TYPE + "===version_int===:" + version_int);
        if(MyApplication.getInstance(this).isLocalDeviceNetworkOk()){
            //请求网络
            CallServer.getRequestInstance().add(DeviceActivity.this, false, CommParams.HTTP_OAD, NoHttpRuquestFactory.create_OAD_Request(firmwareDTO), httpCallback);
        }else
            MyToast.show(this,getString(R.string.main_more_sycn_fail),Toast.LENGTH_LONG);
    }



    private HttpCallback<String> httpCallback = new HttpCallback<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            dialog.dismiss();
            DataFromServer dataFromServer = JSON.parseObject(response.get(), DataFromServer.class);

            String value = dataFromServer.getReturnValue().toString();
            MyLog.e(TAG, "respone.get()" + response.get());
            switch (what) {
                case CommParams.HTTP_OAD:
                    if(!CommonUtils.isStringEmpty(response.get())) {
                        MyLog.e(TAG, "dataFromServer.getErrorCode()：" + dataFromServer.getErrorCode());
                        if (dataFromServer.getErrorCode() == 10020) {

//                                Toast.makeText(DeviceActivity.this, getString(R.string.main_more_version_check_nofind), Toast.LENGTH_LONG).show();
                            MyToast.show(DeviceActivity.this, getString(R.string.main_more_version_check_nofind), Toast.LENGTH_LONG);
                        } else {
                            JSONObject object = JSON.parseObject(value);
                            file_name_OAD = object.getString("file_name");
                            String version_code = object.getString("version_code");
                            MyLog.e(TAG, "===value====" + value + "====" + response.get() + "===file_name==" + file_name_OAD + "==version_code==" + version_code);
                            if (!CommonUtils.isStringEmptyPrefer(file_name_OAD) && value instanceof String && !ToolKits.isJSONNullObj(file_name_OAD)) {
                                MyLog.e(TAG, "json:" + value + "===vo.version==" + vo.version);
                                if (Integer.parseInt(version_code, 16) <= Integer.parseInt(vo.version, 16)) {
//                                        Toast.makeText(DeviceActivity.this, getString(R.string.main_more_version_check_is_latest), Toast.LENGTH_LONG).show();
                                    MyToast.show(DeviceActivity.this, getString(R.string.bracelet_oad_version_top), Toast.LENGTH_LONG);
                                } else {
                                    //从服务器获取了比当前版本更高的bin,开始OAD
                                    String url = CommParams.AVATAR_DOWNLOAD_CONTROLLER_URL_ROOT_NEW+"firmware?fileName="+file_name_OAD;
                                    String filename = file_name_OAD;
                                    downLoad(url, filename);
                                }
                            }
                        }
                    }
                    break;
                case CommParams.HTTP_UNBUND:
                    MyLog.e(TAG, "====HTTP_UNBUND====response=" + response.get());
                    //不做处理
                    if (dataFromServer.getErrorCode() == 1) {
                        if (provider.isConnectedAndDiscovered()) {
                            //连接才去下这个指令
                            provider.unBoundDevice(DeviceActivity.this);
                        }
                        if (userEntity.getDeviceEntity().getDevice_type() == MyApplication.DEVICE_WATCH && ToolKits.isEnabled(DeviceActivity.this)) {
                            startActivity(new Intent(NotificationCollectorService.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                            ToolKits.showCommonTosat(DeviceActivity.this, true, ToolKits.getStringbyId(DeviceActivity.this, R.string.unbound_success_msg), Toast.LENGTH_LONG);
                        }else{
                            ToolKits.showCommonTosat(DeviceActivity.this, true, ToolKits.getStringbyId(DeviceActivity.this, R.string.unbound_success), Toast.LENGTH_LONG);
                        }
                        UserEntity userEntity = MyApplication.getInstance(DeviceActivity.this).getLocalUserInfoProvider();
                        //设备号置空
                        userEntity.getDeviceEntity().setLast_sync_device_id(null);
                        //设备类型置空
                        userEntity.getDeviceEntity().setDevice_type(0);
                        MyLog.e(TAG, "====HTTP_UNBUND====userEntity=" + userEntity.toString());
                        //*******模拟断开   不管有没有连接 先执行这个再说
                        BleService.getInstance(DeviceActivity.this).releaseBLE();
                        finish();
                    } else if (dataFromServer.getErrorCode() == 10004) {
                        MyToast.show(DeviceActivity.this, DeviceActivity.this.getString(R.string.debug_device_unbound_failed), Toast.LENGTH_LONG);
                    }
                    break;
            }
//            if (dataFromServer.getErrorCode() == 10020) {
//                MyToast.show(DeviceActivity.this, "查找不到对应的固件", Toast.LENGTH_LONG);
//            } else {
//                if (!CommonUtils.isStringEmpty(value)) {
//                    JSONObject object = JSON.parseObject(value);
//                    file_name_OAD = object.getString("file_name");
//                    String version_code = object.getString("version_code");
//                    MyLog.e(TAG, "===value====" + value + "====" + response.get() + "===file_name==" + file_name_OAD + "==version_code==" + version_code);
//                    if (!CommonUtils.isStringEmptyPrefer(file_name_OAD) && value instanceof String && !ToolKits.isJSONNullObj(file_name_OAD)) {
//                        if (what == 10) {
//                            MyLog.e(TAG, "json:" + value + "===vo.version==" + vo.version);
//                            if (Integer.parseInt(version_code, 16) <= Integer.parseInt(vo.version, 16)) {
//
////                        String url = CommParams.AVATAR_DOWNLOAD_CONTROLLER_URL_ROOT_NEW+"firmware" + file_name_OAD;
////                        String filename = file_name_OAD;
////                        downLoad(url, filename);
//                                MyToast.show(DeviceActivity.this, "当前固件版本已更新到最高版本！", Toast.LENGTH_LONG);
//                            } else {
//                                //从服务器获取了比当前版本更高的bin,开始OAD
//                                String url = CommParams.AVATAR_DOWNLOAD_CONTROLLER_URL_ROOT_NEW + "firmware" + file_name_OAD;
//                                String filename = file_name_OAD;
//                                downLoad(url, filename);
//                            }
//                        } else {
//                            MyToast.showLong(DeviceActivity.this, getString(R.string.bracelet_oad_version_top));
//                        }
//                    }
//                }
//            }
//                if (what == CommParams.HTTP_UNBUND) {
//                    MyLog.e(TAG, "====HTTP_UNBUND====response=" + response.get());
//                    //不做处理
//                    if (dataFromServer.getErrorCode() == 1) {
//                        if (provider.isConnectedAndDiscovered()) {
//                            //连接才去下这个指令
//                            provider.unBoundDevice(DeviceActivity.this);
//                        }
//
//                        UserEntity userEntity = MyApplication.getInstance(DeviceActivity.this).getLocalUserInfoProvider();
//                        userEntity.getDeviceEntity().setLast_sync_device_id(null);
//                        //*******模拟断开   不管有没有连接 先执行这个再说
//                        BleService.getInstance(DeviceActivity.this).getCurrentHandlerProvider().release();
//                        BleService.getInstance(DeviceActivity.this).getCurrentHandlerProvider().setCurrentDeviceMac(null);
//                        BleService.getInstance(DeviceActivity.this).getCurrentHandlerProvider().setmBluetoothDevice(null);
//                        MyToast.show(DeviceActivity.this, DeviceActivity.this.getString(R.string.unbound_success), Toast.LENGTH_LONG);
//                    } else if (dataFromServer.getErrorCode() == 10004) {
//                        MyToast.show(DeviceActivity.this, DeviceActivity.this.getString(R.string.debug_device_unbound_failed), Toast.LENGTH_LONG);
//                    }
//                }
//                        break;
        }

        @Override
        public void onFailed(int what, String url, Object tag, CharSequence message, int responseCode, long networkMillis) {
            dialog.dismiss();

        }
    };

    private void downLoad(String url, String filename) {

        DownloadRequest downloadRequest = NoHttp.createDownloadRequest(url, getOADFileSavedDir(DeviceActivity.this), filename, true, false);

        CallServer.getDownloadInstance().add(0, downloadRequest, downloadListener);
        MyLog.e(TAG, "=====url===" + url);
    }

    private DownloadListener downloadListener = new DownloadListener() {




     /*   @Override
        public void onDownloadError(int i, int i1, CharSequence charSequence) {
            dialog_connect.dismiss();
            Toast.makeText(DeviceActivity.this, getString(R.string.bracelet_down_file_fail), Toast.LENGTH_SHORT).show();
        }
*/
        @Override
        public void onDownloadError(int what, Exception exception) {
            dialog_connect.dismiss();
            Toast.makeText(DeviceActivity.this, getString(R.string.bracelet_down_file_fail), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStart(int what, boolean resume, long preLenght, Headers header, long count) {
            Log.i(TAG, "下载开始");

            String title = getString(R.string.general_uploading);

            String message =getString(R.string.general_uploading);

            dialog_connect = ProgressDialog.show(DeviceActivity.this, title, message);

            dialog_connect.show();

        }

        @Override
        public void onProgress(int what, int progress, long downCount) {
            Log.i(TAG, "正在下载");

        }

        @Override
        public void onFinish(int what, String filePath) {
            dialog_connect.dismiss();
            Log.i(TAG, "下载完成" + filePath);
            //下载完成,显示开始写入固件
            File file = new File(filePath);
            showDialog(file.getName(), true, null);
        }


        @Override
        public void onCancel(int what) {
            Log.i(TAG, "下载取消");
        }

    };

    private ProgressDialog onCreateDialog() {

        //this表示该对话框是针对当前Activity的
        ProgressDialog progressDialog = new ProgressDialog(DeviceActivity.this);
        //设置最大值为100
        progressDialog.setMax(100);
        //设置进度条风格STYLE_HORIZONTAL
        progressDialog.setProgressStyle(
                ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(getString(R.string.being_written));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;

    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case 0x333:
                    progressDialog.incrementProgressBy(msg.arg1 - persent);

                    persent = msg.arg1;

                    postDelayed(runnable, 2000);

                    break;
            }
        }

        ;
    };
    private LepaoProtocalImpl mLepaoProtocalImpl = new LepaoProtocalImpl();

    Runnable runnable = new Runnable() {
        @SuppressWarnings("static-access")
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = 0x333;
            msg.arg1 = mLepaoProtocalImpl.getOAD_percent();
            Log.i(TAG, "progress=" + msg.arg1 + "");
            handler.sendMessage(msg);
        }
    };


    private void showDialog(final String filename, final boolean fromInternet, final byte[] data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceActivity.this);
        builder.setMessage(getString(R.string.start_upgrading));
        builder.setTitle("OAD");
        builder.setPositiveButton(getString(R.string.queren), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //开始执行写入
                dialog.dismiss();
                beginWrite(filename);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    //获取文件下载的路径
    public static String getOADFileSavedDir(Context context)

    {
        String dir = null;
        File sysExternalStorageDirectory = Environment.getExternalStorageDirectory();
        if (sysExternalStorageDirectory != null && sysExternalStorageDirectory.exists()) {
            dir = sysExternalStorageDirectory.getAbsolutePath() + "/.rtring/OAD";
        }
        Log.i(TAG, "路径" + dir);
        return dir;
    }

    private void beginWrite(String filename) {
        try {
            String filePath = getOADFileSavedDir(DeviceActivity.this) + "/" + filename;
            MyLog.i(TAG, "写入文件的路径" + filePath);
            File file = new File(filePath);
            long fileSize = file.length();
            Log.i(TAG, "file size..." + fileSize);
            if (fileSize > Integer.MAX_VALUE)
                Log.i(TAG, "file too big...");
            FileInputStream fi = new FileInputStream(file);
            byte[] buffer = new byte[(int) fileSize];
            int offset = 0;
            int numRead = 0;
            while (offset < buffer.length
                    && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += numRead;
            }
            //确保所有数据均被读取
            if (offset != buffer.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
            fi.close();
            if (provider.isConnectedAndDiscovered()) {
                Log.i(TAG, "buffer size..." + buffer.length);
                data = buffer;
                canoad = true;
                progressDialog = onCreateDialog();
                progressDialog.incrementProgressBy(-progressDialog.getProgress());
                progressDialog.setMax(((data.length - 17) / 16));
                progressDialog.show();
                handler.post(runnable);
                provider.OADDeviceHead(DeviceActivity.this, data);
                // 第一次传文件头过去之后的流程继续在蓝牙callback中
                handler.post(runnable);
            } else {
                //  WidgetUtils.showToastLong(OwnBraceletActivity.this, ToolKits.getStringbyId(OwnBraceletActivity.this, R.string.pay_no_connect), ToastType.ERROR);

            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

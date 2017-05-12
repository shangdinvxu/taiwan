package com.linkloving.taiwan.logic.UI.main.bundband;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.android.bluetoothlegatt.BLEHandler;
import com.example.android.bluetoothlegatt.BLEListHandler;
import com.example.android.bluetoothlegatt.BLEListProvider;
import com.example.android.bluetoothlegatt.BLEProvider;
import com.example.android.bluetoothlegatt.proltrol.dto.LPDeviceInfo;
import com.linkloving.taiwan.BleService;
import com.linkloving.taiwan.CommParams;
import com.linkloving.taiwan.MyApplication;
import com.linkloving.taiwan.R;
import com.linkloving.taiwan.basic.toolbar.ToolBarActivity;
import com.linkloving.taiwan.http.basic.CallServer;
import com.linkloving.taiwan.http.basic.HttpCallback;
import com.linkloving.taiwan.http.basic.NoHttpRuquestFactory;
import com.linkloving.taiwan.http.data.DataFromServer;
import com.linkloving.taiwan.logic.UI.main.materialmenu.CommonAdapter;
import com.linkloving.taiwan.logic.dto.UserEntity;
import com.linkloving.taiwan.prefrences.PreferencesToolkits;
import com.linkloving.taiwan.prefrences.devicebean.ModelInfo;
import com.linkloving.taiwan.utils.MyToast;
import com.linkloving.taiwan.utils.ToolKits;
import com.linkloving.taiwan.utils.logUtils.MyLog;
import com.yolanda.nohttp.Response;

import java.security.Provider;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zkx on 2016/4/13.
 */
public class Band3ListActivity extends ToolBarActivity {

    public static final String TAG = Band3ListActivity.class.getSimpleName();
    private BLEListProvider listProvider;
    private BLEProvider provider;
    private BLEListHandler handler;

    public String modelName = null;

    public static final int sendcount_MAX = 15;
    private int sendcount = 0;
    public static final int sendcount_time = 2000;

    private ListView mListView;
    private List<DeviceVO> macList = new ArrayList<DeviceVO>();
    private macListAdapter mAdapter;

    private AlertDialog dialog_bound;
    private ProgressDialog progressDialog;
    private Button boundBtn;
    String BoundFailMSG_SHOUHUAN;
    public static final int RESULT_OTHER = 1000;
    public static final int RESULT_BACK = 999;
    public static final int RESULT_FAIL = 998;
    public static final int RESULT_NOCHARGE = 997;
    public static final int RESULT_DISCONNECT = 996;

    public static final int REFRESH_BUTTON = 0x123;

    private int button_txt_count = 40;
    private Object[] button_txt = {button_txt_count};
    private Timer timer;
    private BLEHandler.BLEProviderObserverAdapter observerAdapter = null;
    private android.support.v7.app.AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blelist);
        progressDialog = new ProgressDialog(this);
        observerAdapter = new BLEProviderObserver();
        provider = BleService.getInstance(this).getCurrentHandlerProvider();
        provider.setBleProviderObserver(observerAdapter);
        handler = new BLEListHandler(Band3ListActivity.this) {
            @Override
            protected void handleData(BluetoothDevice device) {
                for (DeviceVO v : macList) {
                    if (v.mac.equals(device.getAddress()))
                        return;
                }
                Log.e(TAG, device.getAddress() + "获取的地址是多少？" + device.getName());
                DeviceVO vo = new DeviceVO();
                vo.mac = device.getAddress();
                vo.name = device.getName();
                vo.bledevice = device;
                macList.add(vo);
                mAdapter.notifyDataSetChanged();
            }
        };
        listProvider = new BLEListProvider(this, handler);
        mAdapter = new macListAdapter(this, macList);
        initView();
        listProvider.scanDeviceList();
        builder = new android.support.v7.app.AlertDialog.Builder(Band3ListActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        listProvider.stopScan();
        // 及时清除此Observer，以便在重新登陆时，正在运行中的蓝牙处理不会因此activity的回收而使回调产生空指针等异常
        provider = BleService.getInstance(this).getCurrentHandlerProvider();
        if (provider != null)
            provider.setBleProviderObserver(null);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (provider != null) {
            if (provider.getBleProviderObserver() == null) {
                provider.setBleProviderObserver(observerAdapter);
            }
        }
    }

    @Override
    protected void getIntentforActivity() {

    }

    @Override
    protected void initView() {
        HideButtonRight(false);
        SetBarTitleText(getString(R.string.bound_ble_list));
        Button btn = getRightButton();
        ViewGroup.LayoutParams layoutParams = btn.getLayoutParams();
        layoutParams.width = 200;
        layoutParams.height = 200;
        btn.setLayoutParams(layoutParams);
        btn.setText(getString(R.string.bound_ble_refresh));

        btn.setTextColor(getResources().getColor(R.color.white));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                macList.clear();
                mAdapter.notifyDataSetChanged();
                listProvider.scanDeviceList();
            }
        });
        mListView = (ListView) findViewById(R.id.ble_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
                provider.setCurrentDeviceMac(macList.get(index).mac);
                provider.setmBluetoothDevice(macList.get(index).bledevice);
                provider.connect_mac(macList.get(index).mac);
                if (progressDialog != null && !progressDialog.isShowing()) {
                    progressDialog.setMessage(getString(R.string.portal_main_state_connecting));
                    progressDialog.show();
                }
            }
        });

        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.activity_bound_band3, (LinearLayout) findViewById(R.id.layout_bundband));
        boundBtn = (Button) layout.findViewById(R.id.btncancle);
        boundBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog_bound.dismiss();
                if (timer != null)
                    timer.cancel();
                provider.clearProess();
                BleService.getInstance(Band3ListActivity.this).releaseBLE();
                setResult(RESULT_BACK);
                finish();
            }
        });
        button_txt[0] = button_txt_count;
        dialog_bound = new AlertDialog.Builder(Band3ListActivity.this)
                .setView(layout)
                .setTitle(R.string.portal_main_isbounding)
                .setOnKeyListener(new DialogInterface.OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                })
                .setCancelable(false).create();
    }

    @Override
    protected void initListeners() {

    }

    Runnable butttonRunnable = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = REFRESH_BUTTON;
            boundhandler.sendMessage(msg);
        }

        ;
    };


    Runnable boundRunnable = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = 0x333;
            boundhandler.sendMessage(msg);
        }

        ;
    };

    Handler boundhandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x333:
                    provider.requestbound_recy(Band3ListActivity.this);
                    break;
                case REFRESH_BUTTON:
                    button_txt[0] = button_txt_count;
                    Log.e(TAG, button_txt_count + "");
                    String second_txt = MessageFormat.format(getString(R.string.bound_scan_sqr), button_txt);
                    boundBtn.setText(second_txt);
                    if (button_txt_count == 0) {
                        if (dialog_bound != null && dialog_bound.isShowing()) {
                            if (timer != null)
                                timer.cancel();
                            dialog_bound.dismiss();
                        }
                        BleService.getInstance(Band3ListActivity.this).releaseBLE();
                        setResult(RESULT_FAIL);
                        finish();
                    }
                    break;
            }
        }

        ;
    };

    private class BLEProviderObserver extends BLEHandler.BLEProviderObserverAdapter {

        @Override
        protected Activity getActivity() {
            return Band3ListActivity.this;
        }

        @Override
        public void updateFor_handleNotEnableMsg() {
            super.updateFor_handleNotEnableMsg();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            Band3ListActivity.this.startActivityForResult(enableBtIntent, BleService.REQUEST_ENABLE_BT);
        }

        @Override
        public void updateFor_handleSendDataError() {
            super.updateFor_handleSendDataError();
            if (dialog_bound != null && dialog_bound.isShowing()) {
                if (timer != null)
                    timer.cancel();
                dialog_bound.dismiss();
            }
        }

        @Override
        public void updateFor_handleConnectLostMsg() {
            Log.i("BandListActivity", "断开连接");
            listProvider.stopScan();
            if (dialog_bound != null && dialog_bound.isShowing()) {
                if (timer != null)
                    timer.cancel();
                dialog_bound.dismiss();
            }
//            provider.connect_mac(provider.getCurrentDeviceMac());
//
            provider.clearProess();
            provider.setCurrentDeviceMac(null);
            provider.setmBluetoothDevice(null);
            provider.resetDefaultState();

            setResult(RESULT_FAIL);
            finish();
        }

        @Override
        public void updateFor_handleConnectFailedMsg() {
            super.updateFor_handleConnectFailedMsg();

            if (dialog_bound != null && dialog_bound.isShowing()) {
                if (timer != null)
                    timer.cancel();
                dialog_bound.dismiss();
            }
            provider.release();
            provider.setCurrentDeviceMac(null);
            provider.setmBluetoothDevice(null);
            provider.resetDefaultState();
            setResult(RESULT_FAIL);
            finish();
        }

        @Override
        public void updateFor_handleConnectSuccessMsg() {
            listProvider.stopScan();
            Log.i("BandListActivity", "连接成功");
            try {
                new Thread().sleep(BleService.TIMEOUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BleService.getInstance(Band3ListActivity.this).syncAllDeviceInfo(Band3ListActivity.this);
//
        }

        @Override
        public void updateFor_notifyFor0x13ExecSucess_D(LPDeviceInfo latestDeviceInfo) {
            super.updateFor_notifyFor0x13ExecSucess_D(latestDeviceInfo);
            if (latestDeviceInfo != null && latestDeviceInfo.recoderStatus == 5) {
                Log.i("BandListActivity", "用户非法");
                new AlertDialog.Builder(Band3ListActivity.this)
                        .setTitle(R.string.portal_main_gobound)
                        .setMessage(R.string.portal_main_mustbund)
                        //
                        .setPositiveButton(R.string.general_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                provider.unBoundDevice(Band3ListActivity.this);
                                try {
                                    Thread.sleep(1000);
                                    BleService.getInstance(Band3ListActivity.this).releaseBLE();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (dialog_bound != null && dialog_bound.isShowing()) {
                                    if (timer != null)
                                        timer.cancel();
                                    dialog_bound.dismiss();
                                }
                                provider.release();
                                provider.setCurrentDeviceMac(null);
                                provider.setmBluetoothDevice(null);
                                provider.resetDefaultState();
                                setResult(RESULT_FAIL);
                                finish();
                            }
                        }).create().show();
            } else if (latestDeviceInfo != null && latestDeviceInfo.recoderStatus == 6) {
                Log.e("BluetoothActivity", "设备未授权");
                Toast.makeText(Band3ListActivity.this, "设备未授权", Toast.LENGTH_SHORT).show();
                provider.release();
                provider.setCurrentDeviceMac(null);
                provider.setmBluetoothDevice(null);
                provider.resetDefaultState();
                provider.clearProess();
                finish();
            } else if (latestDeviceInfo != null && latestDeviceInfo.recoderStatus == 66) {
                if (builder != null && !builder.create().isShowing()) {
                    builder.setPositiveButton(R.string.general_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            provider.unBoundDevice(Band3ListActivity.this);
                            provider.connect();
//                            try {
//                                Thread.sleep(1000);
//                                BleService.getInstance(Band3ListActivity.this).releaseBLE();
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
                        }
                    }).setMessage(getString(R.string.Need_format))
                            .setNegativeButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    provider.disConnect();
                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
            } else {
                provider.requestbound_fit(Band3ListActivity.this);
            }

        }


        //非法用户
        @Override
        public void notifyForInvaildUser() {
            super.notifyForInvaildUser();
            Log.i("BandListActivity", "非法用户");
            new AlertDialog.Builder(Band3ListActivity.this)
                    .setTitle(R.string.portal_main_gobound)
                    .setMessage(R.string.portal_main_mustbund)
                    //
                    .setPositiveButton(R.string.general_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            provider.unBoundDevice(Band3ListActivity.this);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (dialog_bound != null && dialog_bound.isShowing()) {
                                if (timer != null)
                                    timer.cancel();
                                dialog_bound.dismiss();
                            }
                            provider.release();
                            provider.setCurrentDeviceMac(null);
                            provider.setmBluetoothDevice(null);
                            provider.resetDefaultState();
                            setResult(RESULT_FAIL);
                            finish();
                        }
                    }).create().show();
        }

        @Override
        public void updateFor_notifyForDeviceUnboundSucess_D() {
            super.updateFor_notifyForDeviceUnboundSucess_D();
            Log.i("BandListActivity", "清除设备信息/解绑成功");

        }

        @Override
        public void updateFor_BoundNoCharge() {
            super.updateFor_BoundNoCharge();
            Log.e("BLEListActivity", "updateFor_BoundNoCharge");
            if (dialog_bound != null && dialog_bound.isShowing()) {
                if (timer != null)
                    timer.cancel();
                dialog_bound.dismiss();
            }
            setResult(RESULT_NOCHARGE);
            finish();
        }

        @Override
        public void updateFor_BoundContinue() {
            super.updateFor_BoundContinue();

//            if(dialog_bound!=null && !dialog_bound.isShowing() )
//                dialog_bound.show();
//            if (dialog_bound != null && dialog_bound.isShowing()) {
//                    if(timer==null){
//                        timer = new Timer(); // 每1s更新一下
//                        timer.schedule(new TimerTask() {
//                            @Override
//                            public void run() {
            boundhandler.post(butttonRunnable);
//                                button_txt_count--;
//                                MyLog.e(TAG, "Timer开始了");
//                                if (button_txt_count < 0) {
//                                    timer.cancel();
//                                }
//                            }
//                        }, 0, 1000);
//                    }
//            }

            if (sendcount < sendcount_MAX) {
                boundhandler.postDelayed(boundRunnable, sendcount_time);
                sendcount++;
            } else {
                Log.e("BandListActivity", "已经发送超出15次");
                provider.clearProess();
                BleService.getInstance(Band3ListActivity.this).releaseBLE();
                setResult(RESULT_FAIL);
                finish();
            }
        }

        @Override
        public void updateFor_BoundSucess() {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            provider.SetDeviceTime(Band3ListActivity.this);
            if (dialog_bound != null && dialog_bound.isShowing()) {
                if (timer != null)
                    timer.cancel();
                dialog_bound.dismiss();
            }
            //获取成功
            startBound();
        }

//        @Override
//        public void updateFor_handleSetTime() {
//            provider.getModelName(BandListActivity.this);
//        }

        @Override
        public void updateFor_notifyForModelName(LPDeviceInfo latestDeviceInfo) {
            if (latestDeviceInfo == null) {
                //未获取成功  重新获取
                provider.getModelName(Band3ListActivity.this);
            } else {
                modelName = latestDeviceInfo.modelName;
                if (dialog_bound != null && dialog_bound.isShowing()) {
                    if (timer != null)
                        timer.cancel();
                    dialog_bound.dismiss();
                }
                //获取成功
                startBound();
            }
        }

        /**
         * 通知：设备绑定信息同步到服务端完成
         */
        @Override
        public void updateFor_boundInfoSyncToServerFinish(Object resultFromServer) {
            if (resultFromServer != null) {
                if (((String) resultFromServer).equals("1")) {
                    Log.e(TAG, "绑定成功！");
//                    provider.getModelName(BandListActivity.this);
                    MyApplication.getInstance(Band3ListActivity.this).getLocalUserInfoProvider().getDeviceEntity().setLast_sync_device_id(provider.getCurrentDeviceMac());
                    MyApplication.getInstance(Band3ListActivity.this).getLocalUserInfoProvider().getDeviceEntity().setDevice_type(MyApplication.DEVICE_BAND3);
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    ToolKits.showCommonTosat(Band3ListActivity.this, true, ToolKits.getStringbyId(Band3ListActivity.this, R.string.portal_main_bound_success), Toast.LENGTH_LONG);
                    setResult(RESULT_OK);
                    finish();
                } else if (((String) resultFromServer).equals("10024")) {
                    MyLog.e(TAG, "========绑定失败！========");
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    new android.support.v7.app.AlertDialog.Builder(Band3ListActivity.this)
                            .setTitle(getActivity().getResources().getString(R.string.general_prompt))
                            .setMessage(MessageFormat.format(ToolKits.getStringbyId(getActivity(), R.string.portal_main_has_bound_other), BoundFailMSG_SHOUHUAN))
                            .setPositiveButton(getActivity().getResources().getString(R.string.general_ok), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog_, int which) {
                                    dialog_.dismiss();
                                    BleService.getInstance(getActivity()).releaseBLE();
                                    setResult(RESULT_FAIL);
                                    finish();
                                }
                            })
                            .show();
                }
            } else {

                Log.e(TAG, "boundAsyncTask result is null!!!!!!!!!!!!!!!!!");
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                BleService.getInstance(getActivity()).releaseBLE();
            }
        }


        @Override
        public void updateFor_BoundFail() {
            Log.e(TAG, "boundAsyncTask 绑定失败");
            // BandListActivity.this.notifyAll();
            if (dialog_bound != null && dialog_bound.isShowing()) {
                if (timer != null)
                    timer.cancel();
                dialog_bound.dismiss();
            }
            provider.clearProess();
//            provider.unBoundDevice(BandListActivity.this);
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            setResult(RESULT_FAIL);
            finish();
        }

    }

    private void startBound() {
        // 绑定设备时必须保证首先从服务端取来标准UTC时间，以便给设备校时(要看看网络是否连接)
        MyLog.e(TAG, "startBound()");
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.setMessage(getString(R.string.general_submitting));
            progressDialog.show();
        }
//        if (ToolKits.isNetworkConnected(BandListActivity.this)) {
        if (true) {
            UserEntity ue = MyApplication.getInstance(getApplicationContext()).getLocalUserInfoProvider();
            String user_id = ue.getUser_id() + "";
            if (ue != null && provider != null && provider.getCurrentDeviceMac() != null) {
                // 将用户id 和 MAC地址交到服务端进行匹配
//                submitBoundMACToServer(user_id, provider.getCurrentDeviceMac());
                //***********************************台湾离线版本**********************************************************//
                UserEntity userEntity = MyApplication.getInstance(Band3ListActivity.this).getLocalUserInfoProvider();
                userEntity.getDeviceEntity().setLast_sync_device_id(provider.getCurrentDeviceMac());
                userEntity.getDeviceEntity().setDevice_type(MyApplication.DEVICE_BAND3);
                userEntity.getDeviceEntity().setModel_name(modelName);
                MyApplication.getInstance(Band3ListActivity.this).setLocalUserInfoProvider(userEntity);
                if (observerAdapter != null)
                    observerAdapter.updateFor_boundInfoSyncToServerFinish("1");
                //****************************************台湾离线版本****************************************************//
            } else {
                BleService.getInstance(Band3ListActivity.this).releaseBLE();
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                setResult(RESULT_FAIL);
                finish();
            }
        } else {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            BleService.getInstance(Band3ListActivity.this).releaseBLE(); // 没有网络去绑定设备
            // 就断开连接
        }
    }

    private void submitBoundMACToServer(String user_id, String Mac) {
        if (MyApplication.getInstance(this).isLocalDeviceNetworkOk()) {
            String last_sync_device_id2 = MyApplication.getInstance(Band3ListActivity.this).getLocalUserInfoProvider().getDeviceEntity().getLast_sync_device_id2();
            CallServer.getRequestInstance().add(getApplicationContext(), false, CommParams.HTTP_BOUND, NoHttpRuquestFactory.submitBoundMACToServer(user_id, Mac, MyApplication.DEVICE_BAND, modelName), HttpCallback);
            MyLog.e(TAG, "=====user_id=======" + user_id + "==Mac==" + Mac + "===last_sync_device_id2===" + last_sync_device_id2);
        } else {
            MyToast.show(Band3ListActivity.this, getString(R.string.main_more_sycn_fail), Toast.LENGTH_LONG);
        }


    }

    private HttpCallback<String> HttpCallback = new HttpCallback<String>() {

        @Override
        public void onSucceed(int what, Response<String> response) {
            //处理服务端返回的登陆结果信息.
            String result = response.get();
            DataFromServer dataFromServer = JSONObject.parseObject(response.get(), DataFromServer.class);
            String value = dataFromServer.getReturnValue().toString();
            if (dataFromServer.getErrorCode() == 1) {
                //获取了服务器的设备信息 并且保存到本地
                ModelInfo modelInfo = JSONObject.parseObject(response.get(), ModelInfo.class);
                PreferencesToolkits.saveInfoBymodelName(Band3ListActivity.this, modelName, modelInfo);
                MyApplication.getInstance(getApplicationContext()).getLocalUserInfoProvider().getDeviceEntity().setModel_name(modelName);
                MyApplication.getInstance(getApplicationContext()).getLocalUserInfoProvider().getDeviceEntity().setLast_sync_device_id(provider.getCurrentDeviceMac());
                MyApplication.getInstance(getApplicationContext()).getLocalUserInfoProvider().getDeviceEntity().setDevice_type(MyApplication.DEVICE_BAND3);
                if (observerAdapter != null)
                    observerAdapter.updateFor_boundInfoSyncToServerFinish(dataFromServer.getErrorCode() + "");
            }
            if (dataFromServer.getErrorCode() == 10024) {
//                设备已被绑定，弹框提示绑定的用户
                JSONObject object = JSON.parseObject(value);
                BoundFailMSG_SHOUHUAN = object.getString("nickname");
                MyLog.e(TAG, "HttpCallback=====result=======" + result + "===BoundFailMSG====" + BoundFailMSG_SHOUHUAN);
                if (observerAdapter != null)
                    observerAdapter.updateFor_boundInfoSyncToServerFinish(dataFromServer.getErrorCode() + "");
            }
        }

        @Override
        public void onFailed(int what, String url, Object tag, CharSequence message, int responseCode, long networkMillis) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BleService.REQUEST_ENABLE_BT) {
            switch (resultCode) {

                case Activity.RESULT_CANCELED: //用户取消打开蓝牙
                    Log.e("BLEListActivity", "用户取消打开蓝牙");
                    break;

                case Activity.RESULT_OK:       //用户打开蓝牙
                    listProvider.scanDeviceList();
                    /**
                     * 全局的变量 防止在这里关闭蓝牙导致连接上后 后台循环扫描的定时器不走
                     */
                    BleService.setNEED_SCAN(true);
                    Log.e("BLEListActivity", "/用户打开蓝牙");

                    break;

                default:
                    break;
            }
            return;
        }

    }

    class DeviceVO {
        public String mac;
        public String name;
        public BluetoothDevice bledevice;

    }

    class macListAdapter extends CommonAdapter<DeviceVO> {
        public class ViewHolder {
            public TextView mac;
        }

        ViewHolder holder;

        public macListAdapter(Context context, List<DeviceVO> list) {
            super(context, list);
        }

        @Override
        protected View noConvertView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.list_item_ble_list, parent, false);
            holder = new ViewHolder();
            holder.mac = (TextView) convertView.findViewById(R.id.activity_sport_data_detail_sleepSumView);
            convertView.setTag(holder);
            return convertView;
        }

        @Override
        protected View hasConvertView(int position, View convertView, ViewGroup parent) {
            holder = (ViewHolder) convertView.getTag();
            return convertView;
        }

        @Override
        protected View initConvertView(int position, View convertView, ViewGroup parent) {
//            String adress = list.get(position).mac;
//            String[] split = adress.split(":");
//            String mac =split[1]+""+split[0];
            holder.mac.setText(list.get(position).name);
            return convertView;
        }

    }

    public static String removeCharAt(String s, int pos) {
        return s.substring(0, pos) + s.substring(pos + 1);
    }
}

package com.linkloving.taiwan.logic.UI.device.power;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bluetoothlegatt.BLEHandler;
import com.example.android.bluetoothlegatt.BLEProvider;
import com.linkloving.taiwan.BleService;
import com.linkloving.taiwan.MyApplication;
import com.linkloving.taiwan.R;
import com.linkloving.taiwan.basic.toolbar.ToolBarActivity;
import com.linkloving.taiwan.logic.dto.UserEntity;
import com.linkloving.taiwan.prefrences.LocalUserSettingsToolkits;
import com.linkloving.taiwan.prefrences.devicebean.DeviceSetting;
import com.linkloving.taiwan.utils.DeviceInfoHelper;
import com.linkloving.taiwan.utils.MyToast;
import com.linkloving.taiwan.utils.logUtils.MyLog;

public class PowerActivity extends ToolBarActivity implements View.OnClickListener {
    private static final String TAG = PowerActivity.class.getSimpleName();

    LinearLayout linearLayout_pm1,linearLayout_pm2,linearLayout_pm3;

    TextView pm1content_1,pm1content_2,pm2content_1,pm2content_2,pm2content_3,pm3content_1,pm3content_2,pm3content_3,pm1,pm2,pm3;

    ImageView imageView_pm1,imageView_pm2,imageView_pm1_underline,imageView_pm2_underline,imageView_pm3_underline;
    private int CURRENT_STATUS;
    DeviceSetting localSeting;
    //蓝牙观察者
    private BLEHandler.BLEProviderObserverAdapter bleProviderObserver;
    //蓝牙提供者
    private BLEProvider provider;
    UserEntity userEntity;
    Button BLE_Button;
    private LinearLayout Synchronize_device;

    View view;
    Toast toast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_power);
        provider = BleService.getInstance(this).getCurrentHandlerProvider();
        bleProviderObserver = new BLEProviderObserverAdapterImpl();
        provider.setBleProviderObserver(bleProviderObserver);
        userEntity = MyApplication.getInstance(this).getLocalUserInfoProvider();
        Change();
    }
    @Override
    protected void getIntentforActivity() {

    }

    @Override
    protected void initView() {
        SetBarTitleText(getString((R.string.power_setting)));
        view= LayoutInflater.from(this).inflate(R.layout.toast_synchronize, null);
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        int result = getResources().getDimensionPixelSize(resourceId);
        //得到Toast对象
        toast = new Toast(PowerActivity.this);
        //设置Toast对象的位置，3个参数分别为位置，X轴偏移，Y轴偏
        toast.setGravity(Gravity.TOP, 0, result+50);
        //设置Toast对象的显示时间
        toast.setDuration(Toast.LENGTH_LONG);
        //设置Toast对象所要展示的视图
        toast.setView(view);
        linearLayout_pm1= (LinearLayout) findViewById(R.id.linearLayout_pm1);
        linearLayout_pm2= (LinearLayout) findViewById(R.id.linearLayout_pm2);
        linearLayout_pm3= (LinearLayout) findViewById(R.id.linearLayout_pm3);

        pm1= (TextView) findViewById(R.id.text_pm1);
        pm2= (TextView) findViewById(R.id.text_pm2);
        pm3= (TextView) findViewById(R.id.text_pm3);

        pm1content_1= (TextView) findViewById(R.id.text_pm1_content_1);
        pm1content_2= (TextView) findViewById(R.id.text_pm1_content_2);

        pm2content_1= (TextView) findViewById(R.id.text_pm2_content_1);
        pm2content_2= (TextView) findViewById(R.id.text_pm2_content_2);
        pm2content_3= (TextView) findViewById(R.id.text_pm2_content_3);

        pm3content_1= (TextView) findViewById(R.id.text_pm3_content_1);
        pm3content_2= (TextView) findViewById(R.id.text_pm3_content_2);
        pm3content_3= (TextView) findViewById(R.id.text_pm3_content_3);

        imageView_pm1= (ImageView) findViewById(R.id.imageView_pm1);
        imageView_pm2= (ImageView) findViewById(R.id.imageView_pm2);

        imageView_pm1_underline= (ImageView) findViewById(R.id.pm1_underline);
        imageView_pm2_underline= (ImageView) findViewById(R.id.pm2_underline);
        imageView_pm3_underline= (ImageView) findViewById(R.id.pm3_underline);

        BLE_Button= (Button) findViewById(R.id.power_btn_Synchronize_device);
        Synchronize_device= (LinearLayout) findViewById(R.id.power_Synchronize_device);


    }

    @Override
    protected void initListeners() {
        imageView_pm1.setOnClickListener(this);
        imageView_pm2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageView_pm1:
                if(CURRENT_STATUS==0||CURRENT_STATUS==2){
                    //设置状态为1
                    localSeting.setElectricity_value(1);
                    LocalUserSettingsToolkits.updateLocalSetting(PowerActivity.this, localSeting);

                    //判断蓝牙是否连接
                    if (provider.isConnectedAndDiscovered()) {
                        //同步到设备
                        provider.SetPower(PowerActivity.this, DeviceInfoHelper.fromUserEntity(PowerActivity.this, userEntity));
                        Synchronize_device.setVisibility(View.VISIBLE);
                        BLE_Button.setText(getString(R.string.synchronized_to_device));
                        toast.show();
                    }else{
               /* android.support.v7.app.AlertDialog dialog = new android.support.v7.app.AlertDialog.Builder(PowerActivity.this)
                        .setTitle(ToolKits.getStringbyId(PowerActivity.this, R.string.portal_main_unbound))
                        .setMessage(ToolKits.getStringbyId(PowerActivity.this, R.string.portal_main_unbound_msg))
                        .setPositiveButton(ToolKits.getStringbyId(PowerActivity.this, R.string.general_ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).create();
                dialog.show();*/
                    }

                }


                else if(CURRENT_STATUS==1){
                    // 设置状态为0
                    localSeting.setElectricity_value(0);
                    LocalUserSettingsToolkits.updateLocalSetting(PowerActivity.this,localSeting);
                    //判断蓝牙是否连接
                    if (provider.isConnectedAndDiscovered()) {
                        //同步到设备
                        provider.SetPower(PowerActivity.this, DeviceInfoHelper.fromUserEntity(PowerActivity.this, userEntity));
                        Synchronize_device.setVisibility(View.VISIBLE);
                        BLE_Button.setText(getString(R.string.synchronized_to_device));
                        toast.show();
                    }else{
               /* android.support.v7.app.AlertDialog dialog = new android.support.v7.app.AlertDialog.Builder(PowerActivity.this)
                        .setTitle(ToolKits.getStringbyId(PowerActivity.this, R.string.portal_main_unbound))
                        .setMessage(ToolKits.getStringbyId(PowerActivity.this, R.string.portal_main_unbound_msg))
                        .setPositiveButton(ToolKits.getStringbyId(PowerActivity.this, R.string.general_ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).create();
                dialog.show();*/
                        MyToast.showLong(PowerActivity.this, getString(R.string.pay_no_connect));
                    }
                }



                Change();

                break;
            case R.id.imageView_pm2:

                if(CURRENT_STATUS==0||CURRENT_STATUS==1) {
                    //设置状态为2
                    localSeting.setElectricity_value(2);

                    LocalUserSettingsToolkits.updateLocalSetting(PowerActivity.this,localSeting);
                    //判断蓝牙是否连接
                    if (provider.isConnectedAndDiscovered()) {
                        //同步到设备
                        provider.SetPower(PowerActivity.this, DeviceInfoHelper.fromUserEntity(PowerActivity.this, userEntity));
                        Synchronize_device.setVisibility(View.VISIBLE);
                        BLE_Button.setText(getString(R.string.synchronized_to_device));
                        toast.show();
                    }else{
                       /* android.support.v7.app.AlertDialog dialog = new android.support.v7.app.AlertDialog.Builder(PowerActivity.this)
                                .setTitle(ToolKits.getStringbyId(PowerActivity.this, R.string.portal_main_unbound))
                                .setMessage(ToolKits.getStringbyId(PowerActivity.this, R.string.portal_main_unbound_msg))
                                .setPositiveButton(ToolKits.getStringbyId(PowerActivity.this, R.string.general_ok),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }).create();
                        dialog.show();*/
                        MyToast.showLong(PowerActivity.this, getString(R.string.pay_no_connect));

                    }
                }
                else if(CURRENT_STATUS==2){
                    // 设置状态为0
                    localSeting.setElectricity_value(0);
                    LocalUserSettingsToolkits.updateLocalSetting(PowerActivity.this,localSeting);
//判断蓝牙是否连接
                    if (provider.isConnectedAndDiscovered()) {
                        //同步到设备
                        provider.SetPower(PowerActivity.this, DeviceInfoHelper.fromUserEntity(PowerActivity.this,userEntity));
                        Synchronize_device.setVisibility(View.VISIBLE);
                        BLE_Button.setText(getString(R.string.synchronized_to_device));
                        toast.show();
                    }else{
                      /*  android.support.v7.app.AlertDialog dialog = new android.support.v7.app.AlertDialog.Builder(PowerActivity.this)
                                .setTitle(ToolKits.getStringbyId(PowerActivity.this, R.string.portal_main_unbound))
                                .setMessage(ToolKits.getStringbyId(PowerActivity.this, R.string.portal_main_unbound_msg))
                                .setPositiveButton(ToolKits.getStringbyId(PowerActivity.this, R.string.general_ok),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }).create();
                        dialog.show();*/
                        MyToast.showLong(PowerActivity.this, getString(R.string.pay_no_connect));

                    }
                }

                Change();

                break;
        }

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (provider.getBleProviderObserver() == null) {
            provider.setBleProviderObserver(bleProviderObserver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        provider.setBleProviderObserver(null);
    }

    private void Change(){

        //每次获取本地的设置信息
        localSeting=LocalUserSettingsToolkits.getLocalSetting(PowerActivity.this,userEntity.getUser_id()+"");

        CURRENT_STATUS=localSeting.getElectricity_value();

        MyLog.i(TAG,"CURRENT_STATUS"+CURRENT_STATUS);

        switch (localSeting.getElectricity_value()){
            //pm1
            case 1:
                linearLayout_pm1.setBackgroundResource(R.drawable.alarm_setting_content_bg);
                imageView_pm1.setImageResource(R.mipmap.btn_on);
                pm1.setTextColor(Color.parseColor("#FF7700"));
                pm1content_1.setTextColor(Color.parseColor("#FF7700"));
                pm1content_2.setTextColor(Color.parseColor("#FF7700"));
                imageView_pm1_underline.setImageResource(R.mipmap.underline_on);
                imageView_pm1_underline.setVisibility(View.VISIBLE);
                pm1content_1.setVisibility(View.VISIBLE);
                pm1content_2.setVisibility(View.VISIBLE);
                linearLayout_pm2.setBackgroundResource(R.drawable.alarm_content_bg);
                imageView_pm2.setImageResource(R.mipmap.btn_off);
                pm2.setTextColor(Color.parseColor("#666666"));
                //隐藏提示内容
                imageView_pm2_underline.setVisibility(View.GONE);
                pm2content_1.setVisibility(View.GONE);
                pm2content_2.setVisibility(View.GONE);
                pm2content_3.setVisibility(View.GONE);
                break;
            //pm2
            case 2:
                linearLayout_pm2.setBackgroundResource(R.drawable.alarm_setting_content_bg);
                imageView_pm2.setImageResource(R.mipmap.btn_on);
                pm2.setTextColor(Color.parseColor("#FF7700"));
                pm2content_1.setTextColor(Color.parseColor("#FF7700"));
                pm2content_2.setTextColor(Color.parseColor("#FF7700"));
                pm2content_3.setTextColor(Color.parseColor("#FF7700"));
                imageView_pm2_underline.setImageResource(R.mipmap.underline_on);
                imageView_pm2_underline.setVisibility(View.VISIBLE);
                pm2content_1.setVisibility(View.VISIBLE);
                pm2content_2.setVisibility(View.VISIBLE);
                pm2content_3.setVisibility(View.VISIBLE);


                linearLayout_pm1.setBackgroundResource(R.drawable.alarm_content_bg);
                imageView_pm1.setImageResource(R.mipmap.btn_off);
                pm1.setTextColor(Color.parseColor("#666666"));
              /*  imageView_pm1_underline.setImageResource(R.mipmap.underline_set);
                pm1.setTextColor(Color.parseColor("#666666"));
                pm1content_1.setTextColor(Color.parseColor("#666666"));
                pm1content_2.setTextColor(Color.parseColor("#666666"));*/

                imageView_pm1_underline.setVisibility(View.GONE);
                pm1content_1.setVisibility(View.GONE);
                pm1content_2.setVisibility(View.GONE);

                break;
            //没选
            case 0:
                linearLayout_pm1.setBackgroundResource(R.drawable.alarm_content_bg);
                imageView_pm1.setImageResource(R.mipmap.btn_off);
                imageView_pm1_underline.setImageResource(R.mipmap.underline_set);
                pm1.setTextColor(Color.parseColor("#666666"));
             /*   pm1content_1.setTextColor(Color.parseColor("#666666"));
                pm1content_2.setTextColor(Color.parseColor("#666666"));*/

                imageView_pm1_underline.setVisibility(View.GONE);
                pm1content_1.setVisibility(View.GONE);
                pm1content_2.setVisibility(View.GONE);

                linearLayout_pm2.setBackgroundResource(R.drawable.alarm_content_bg);
                imageView_pm2.setImageResource(R.mipmap.btn_off);
                pm2.setTextColor(Color.parseColor("#666666"));

                imageView_pm2_underline.setVisibility(View.GONE);
                pm2content_1.setVisibility(View.GONE);
                pm2content_2.setVisibility(View.GONE);
                pm2content_3.setVisibility(View.GONE);
                break;
        }






    }
    /**
     * 蓝牙观察者实现类.
     */
    private class BLEProviderObserverAdapterImpl extends BLEHandler.BLEProviderObserverAdapter {

        @Override
        protected Activity getActivity() {
            return PowerActivity.this;
        }

        public BLEProviderObserverAdapterImpl() {
            super();
        }

        @Override
        public void updateFor_notifyForSetClockSucess() {
            super.updateFor_notifyForSetClockSucess();

        }

        @Override
        public void updateFor_notifyForSetClockFail() {
            super.updateFor_notifyForSetClockFail();
        }

    }


}

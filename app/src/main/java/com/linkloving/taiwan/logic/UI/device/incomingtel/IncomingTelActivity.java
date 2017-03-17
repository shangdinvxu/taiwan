package com.linkloving.taiwan.logic.UI.device.incomingtel;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.linkloving.taiwan.utils.LanguageHelper;
import com.linkloving.taiwan.utils.ToolKits;
import com.linkloving.taiwan.utils.logUtils.MyLog;

public class IncomingTelActivity extends ToolBarActivity{
    /**来电*/
    private LinearLayout callLayout;
    private ImageView callImg;
    private TextView callText;
    private CheckBox callSwitch;
    /**短信*/
    private LinearLayout mesgsaeLayout;
    private ImageView mesgsaeImg;
    private TextView mesgsaeText;
    private CheckBox mesgsaeSwitch;
    /**QQ*/
    private LinearLayout qQLayout;
    private ImageView qQImg;
    private TextView qQText;
    private CheckBox qQSwitch;
    /**微信*/
    private LinearLayout wXLayout;
    private ImageView wXImg;
    private TextView wXText;
    private CheckBox wXSwitch;
    /**连爱*/
    private LinearLayout linkLayout;
    private ImageView linkImg;
    private TextView linkText;
    private CheckBox linkSwitch;
    View view;
    Toast toast;

    private DeviceSetting deviceSetting;
    private byte[] send_data;
    private int notif_data;
    //蓝牙观察者
    private BLEHandler.BLEProviderObserverAdapter bleProviderObserver;
    //蓝牙提供者
    private BLEProvider provider;
    UserEntity userEntity;
    //    同步提醒
    Button BLE_Button;
    private LinearLayout Synchronize_device;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_notify);
        userEntity = MyApplication.getInstance(getApplicationContext()).getLocalUserInfoProvider();
        provider = BleService.getInstance(this).getCurrentHandlerProvider();
        bleProviderObserver = new BLEProviderObserverAdapterImpl();
        provider.setBleProviderObserver(bleProviderObserver);
        initData();
    }
    @Override
    protected void getIntentforActivity() {
    }

    @Override
    protected void initView() {
        SetBarTitleText(getString((R.string.xiaoxi_notify)));
        callLayout = (LinearLayout) findViewById(R.id.call_layout);
        callImg = (ImageView) findViewById(R.id.call_img);
        callText= (TextView) findViewById(R.id.call_text);
        callSwitch = (CheckBox) findViewById(R.id.call_switch);
        mesgsaeLayout = (LinearLayout) findViewById(R.id.mesgsae_layout);
        mesgsaeImg = (ImageView) findViewById(R.id.mesgsae_img);
        mesgsaeText= (TextView) findViewById(R.id.mesgsae_text);
        mesgsaeSwitch = (CheckBox) findViewById(R.id.mesgsae_switch);
        qQLayout = (LinearLayout) findViewById(R.id.QQ_layout);
        qQImg = (ImageView) findViewById(R.id.QQ_img);
        qQText= (TextView) findViewById(R.id.QQ_text);
        qQSwitch = (CheckBox) findViewById(R.id.QQ_switch);
        wXLayout = (LinearLayout) findViewById(R.id.WX_layout);
        wXImg = (ImageView) findViewById(R.id.WX_img);
        wXText= (TextView) findViewById(R.id.WX_text);
        wXSwitch = (CheckBox) findViewById(R.id.WX_switch);
        linkLayout = (LinearLayout) findViewById(R.id.Link_layout);
        linkImg = (ImageView) findViewById(R.id.Link_img);
        linkText= (TextView) findViewById(R.id.Link_text);
        linkSwitch = (CheckBox) findViewById(R.id.Link_switch);

        qQLayout.setVisibility(View.GONE);
        wXLayout.setVisibility(View.GONE);
        linkLayout.setVisibility(View.GONE);
        if(!LanguageHelper.isChinese_SimplifiedChinese()){
            //英文下不需要qq 和 微信
            qQLayout.setVisibility(View.GONE);
            wXLayout.setVisibility(View.GONE);
        }

        BLE_Button= (Button) findViewById(R.id.incoming_btn_Synchronize_device);
        Synchronize_device= (LinearLayout) findViewById(R.id.incoming_Synchronize_device);

        view= LayoutInflater.from(this).inflate(R.layout.toast_synchronize, null);
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        int result = getResources().getDimensionPixelSize(resourceId);
        //得到Toast对象
        toast = new Toast(IncomingTelActivity.this);
        //设置Toast对象的位置，3个参数分别为位置，X轴偏移，Y轴偏
        toast.setGravity(Gravity.TOP, 0, result+50);
        //设置Toast对象的显示时间
        toast.setDuration(Toast.LENGTH_LONG);
        //设置Toast对象所要展示的视图
        toast.setView(view);

    }

    private void initData() {
        //通过userid去获取对应的DeviceSetting
        deviceSetting = LocalUserSettingsToolkits.getLocalSetting(IncomingTelActivity.this,userEntity.getUser_id()+"");
        int ANCS_value = deviceSetting.getANCS_value();
        MyLog.e("IncomingTelActivity","ANCS_value:"+ANCS_value);
        /*********数据转成应用层变量START*********/
        String Ancs_str = Integer.toBinaryString(ANCS_value);
        char[] array = { 0, 0, 0, 0, 0 };
        char[] charr = Ancs_str.toCharArray();
        System.arraycopy(charr, 0, array, 5 - charr.length, charr.length); // 将字符串转换为字符数组
        /*********数据转成应用层变量OVER*********/

        if(array[4] == '1'){
            linkSwitch.setChecked(true);
            linkLayout.setBackgroundResource(R.drawable.alarm_setting_content_bg);
            linkText.setTextColor(getResources().getColor(R.color.orange));
            linkImg.setImageDrawable(getResources().getDrawable(R.mipmap.set_linkloving_on));
        }
        if(array[3] == '1'){
            callSwitch.setChecked(true);
            callLayout.setBackgroundResource(R.drawable.alarm_setting_content_bg);
            callText.setTextColor(getResources().getColor(R.color.orange));
            callImg.setImageDrawable(getResources().getDrawable(R.mipmap.set_phone_on));
        }
        if(array[2] == '1'){
            mesgsaeSwitch.setChecked(true);
            mesgsaeLayout.setBackgroundResource(R.drawable.alarm_setting_content_bg);
            mesgsaeText.setTextColor(getResources().getColor(R.color.orange));
            mesgsaeImg.setImageDrawable(getResources().getDrawable(R.mipmap.set_message_on));
        }
        if(array[1] == '1'){
            wXSwitch.setChecked(true);
            wXLayout.setBackgroundResource(R.drawable.alarm_setting_content_bg);
            wXText.setTextColor(getResources().getColor(R.color.orange));
            wXImg.setImageDrawable(getResources().getDrawable(R.mipmap.set_wechat_on));
        }
        if(array[0] == '1'){
            qQSwitch.setChecked(true);
            qQLayout.setBackgroundResource(R.drawable.alarm_setting_content_bg);
            qQText.setTextColor(getResources().getColor(R.color.orange));
            qQImg.setImageDrawable(getResources().getDrawable(R.mipmap.set_qq_on));
        }
    }

    private void setNotificition()
    {
        MyLog.e("IncomingTelActivity","执行了setNotificition");
        String notif_ = "" +(qQSwitch.isChecked() ? 1 : 0)+ (wXSwitch.isChecked() ? 1 : 0)
                + (mesgsaeSwitch.isChecked() ? 1 : 0) + (callSwitch.isChecked() ? 1 : 0)
                + (linkSwitch.isChecked() ? 1 : 0);
        notif_data = Integer.parseInt(notif_, 2);//报存到本地的
        send_data = intto2byte(notif_data); //发送到蓝牙的
        /**保存并且发送到蓝牙**/
        //报存本地
        deviceSetting.setANCS_value(notif_data);
        MyLog.e("IncomingTelActivity","notif_data:"+notif_data);
        LocalUserSettingsToolkits.updateLocalSetting(IncomingTelActivity.this,deviceSetting);
        //发送到蓝牙
        //判断蓝牙是否连接
        if (provider.isConnectedAndDiscovered()) {
            //同步到设备
            provider.setNotification(IncomingTelActivity.this, send_data);
            Synchronize_device.setVisibility(View.VISIBLE);
             BLE_Button.setText(getString(R.string.synchronized_to_device));
toast.show();
        }else{
            android.support.v7.app.AlertDialog dialog = new android.support.v7.app.AlertDialog.Builder(IncomingTelActivity.this)
                    .setTitle(ToolKits.getStringbyId(IncomingTelActivity.this, R.string.portal_main_unbound))
                    .setMessage(ToolKits.getStringbyId(IncomingTelActivity.this, R.string.portal_main_unbound_msg))
                    .setPositiveButton(ToolKits.getStringbyId(IncomingTelActivity.this, R.string.general_ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).create();
            dialog.show();
        }
        //这是发送到蓝牙的处理
    }

    @Override
    protected void initListeners() {
        //电话
        callSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callSwitch.isChecked()) {
                    callLayout.setBackgroundResource(R.drawable.alarm_setting_content_bg);
                    callText.setTextColor(getResources().getColor(R.color.orange));
                    callImg.setImageDrawable(getResources().getDrawable(R.mipmap.set_phone_on));
                    setNotificition();
                }else{
                    callLayout.setBackgroundResource(R.drawable.alarm_content_bg);
                    callText.setTextColor(getResources().getColor(R.color.gray_dark_for_text));
                    callImg.setImageDrawable(getResources().getDrawable(R.mipmap.set_phone_off));
                    setNotificition();
                }
            }
        });
        //短信
        mesgsaeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mesgsaeSwitch.isChecked()) {
                    mesgsaeLayout.setBackgroundResource(R.drawable.alarm_setting_content_bg);
                    mesgsaeText.setTextColor(getResources().getColor(R.color.orange));
                    mesgsaeImg.setImageDrawable(getResources().getDrawable(R.mipmap.set_message_on));
                    setNotificition();
                }else{
                    mesgsaeLayout.setBackgroundResource(R.drawable.alarm_content_bg);
                    mesgsaeText.setTextColor(getResources().getColor(R.color.gray_dark_for_text));
                    mesgsaeImg.setImageDrawable(getResources().getDrawable(R.mipmap.set_message_off));
                    setNotificition();
                }
            }
        });
        //qq
        qQSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (qQSwitch.isChecked()) {
                    qQLayout.setBackgroundResource(R.drawable.alarm_setting_content_bg);
                    qQText.setTextColor(getResources().getColor(R.color.orange));
                    qQImg.setImageDrawable(getResources().getDrawable(R.mipmap.set_qq_on));
                    setNotificition();
                }else{
                    qQLayout.setBackgroundResource(R.drawable.alarm_content_bg);
                    qQText.setTextColor(getResources().getColor(R.color.gray_dark_for_text));
                    qQImg.setImageDrawable(getResources().getDrawable(R.mipmap.set_qq_off));
                    setNotificition();
                }
            }
        });
        //wx
        wXSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wXSwitch.isChecked()) {
                    wXLayout.setBackgroundResource(R.drawable.alarm_setting_content_bg);
                    wXText.setTextColor(getResources().getColor(R.color.orange));
                    wXImg.setImageDrawable(getResources().getDrawable(R.mipmap.set_wechat_on));
                    setNotificition();
                }else{
                    wXLayout.setBackgroundResource(R.drawable.alarm_content_bg);
                    wXText.setTextColor(getResources().getColor(R.color.gray_dark_for_text));
                    wXImg.setImageDrawable(getResources().getDrawable(R.mipmap.set_wechact_off));
                    setNotificition();
                }
            }
        });
        //link
        linkSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (linkSwitch.isChecked()) {
                    MyLog.e("IncomingTelActivity","linkSwitch:");
                    linkLayout.setBackgroundResource(R.drawable.alarm_setting_content_bg);
                    linkText.setTextColor(getResources().getColor(R.color.orange));
                    linkImg.setImageDrawable(getResources().getDrawable(R.mipmap.set_linkloving_on));
                    setNotificition();
                }else{
                    linkLayout.setBackgroundResource(R.drawable.alarm_content_bg);
                    linkText.setTextColor(getResources().getColor(R.color.gray_dark_for_text));
                    linkImg.setImageDrawable(getResources().getDrawable(R.mipmap.set_linkloving_off));
                    setNotificition();
                }
            }
        });
    }

    /**int转byte*/
    public static byte[] intto2byte(int a)
    {
        byte[] m = new byte[2];
        m[0] = (byte) ((0xff & a));
        m[1] = (byte) (0xff & (a >> 8));
        return m;
    }
    /**
     * 蓝牙观察者实现类.
     */
    private class BLEProviderObserverAdapterImpl extends BLEHandler.BLEProviderObserverAdapter {

        @Override
        protected Activity getActivity() {
            return IncomingTelActivity.this;
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



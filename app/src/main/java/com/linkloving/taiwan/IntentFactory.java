package com.linkloving.taiwan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.linkloving.taiwan.logic.UI.HeartRate.HeartRateActivity;
import com.linkloving.taiwan.logic.UI.customerservice.CustomerServiceActivity;
import com.linkloving.taiwan.logic.UI.device.DeviceActivity;
import com.linkloving.taiwan.logic.UI.device.alarm.AlarmActivity;
import com.linkloving.taiwan.logic.UI.device.alarm.SetAlarmActivity;
import com.linkloving.taiwan.logic.UI.device.firmware.FirmwareUpdateActivity;
import com.linkloving.taiwan.logic.UI.device.handup.HandUpActivity;
import com.linkloving.taiwan.logic.UI.device.incomingtel.IncomingTelActivity;
import com.linkloving.taiwan.logic.UI.device.longsit.LongSitActivity;
import com.linkloving.taiwan.logic.UI.device.movedevice.MoveDeviceActivity;
import com.linkloving.taiwan.logic.UI.device.power.PowerActivity;
import com.linkloving.taiwan.logic.UI.friend.AttentionActivity;
import com.linkloving.taiwan.logic.UI.friend.CommentActivity;
import com.linkloving.taiwan.logic.UI.friend.FriendActivity;
import com.linkloving.taiwan.logic.UI.goal.SportGoalActivity;
import com.linkloving.taiwan.logic.UI.help.HelpActivity;
import com.linkloving.taiwan.logic.UI.launch.dto.UserRegisterDTO;
import com.linkloving.taiwan.logic.UI.launch.register.BodyActivity;
import com.linkloving.taiwan.logic.UI.launch.register.CommonWebActivity;
import com.linkloving.taiwan.logic.UI.launch.register.PassWordActivity;
import com.linkloving.taiwan.logic.UI.launch.register.RegisteredSuccessActivity;
import com.linkloving.taiwan.logic.UI.main.PortalActivity;
import com.linkloving.taiwan.logic.UI.main.bundband.BandListActivity;
import com.linkloving.taiwan.logic.UI.main.bundband.bundbandstep1;
import com.linkloving.taiwan.logic.UI.main.datachatactivity.CalDataActivity;
import com.linkloving.taiwan.logic.UI.main.datachatactivity.DistanceDataActivity;
import com.linkloving.taiwan.logic.UI.main.datachatactivity.SleepDataActivity;
import com.linkloving.taiwan.logic.UI.main.datachatactivity.StepDataActivity;
import com.linkloving.taiwan.logic.UI.main.pay.WalletActivity;
import com.linkloving.taiwan.logic.UI.more.MoreActivity;
import com.linkloving.taiwan.logic.UI.personal.FriendInfoActivity;
import com.linkloving.taiwan.logic.UI.personal.PersonalInfoActivity;
import com.linkloving.taiwan.logic.UI.ranking.RankingActivity;
import com.linkloving.taiwan.logic.dto.UserEntity;
import com.linkloving.taiwan.utils.ToolKits;
import com.linkloving.utils.CommonUtils;

/**
 * Created by zkx on 2016/2/24.
 */
public class IntentFactory {

    public static Intent createPrssedHomeKeyIntent() {
        Intent i = new Intent("android.intent.action.MAIN");
        i.setFlags(270532608);
        i.addCategory("android.intent.category.HOME");
        return i;
    }

    /**
     * 创建进入设置身体信息的方法
     */
    public static void startBodyActivityIntent(Activity activity) {
        Intent intent = new Intent(activity, BodyActivity.class);
        activity.startActivity(intent);
    }



    public static Intent start_HelpActivity(Activity activity) {
        Intent intent = new Intent(activity, HelpActivity.class);
        return intent;
    }

    public static Intent star_DeviceActivityIntent(Activity thisActivity,int type) {
        Intent deviceintent = new Intent(thisActivity, DeviceActivity.class);
        deviceintent.putExtra("type",type);
        return deviceintent;
    }
    public static void startHeartRateActivity(Activity thisActivity) {
        Intent intent = new Intent(thisActivity, HeartRateActivity.class);
        thisActivity.startActivity(intent);
    }


    public static Intent start_AlarmActivityIntent(Activity activity) {
//        无声闹钟
        Intent alarmIntent = new Intent(activity, AlarmActivity.class);
        return alarmIntent;
    }

    public static void start_LongSitActivityIntent(Activity activity) {
//        久坐提醒
        Intent longsitIntent = new Intent(activity, LongSitActivity.class);
        activity.startActivity(longsitIntent);
    }



    public static void start_HandUpActivity(Activity activity) {
//        勿扰模式
        Intent intent = new Intent(activity, HandUpActivity.class);
        activity.startActivity(intent);
    }

    public static void start_IncomingTel(Activity activity) {
//来电提醒
        Intent intent = new Intent(activity, IncomingTelActivity.class);
        activity.startActivity(intent);
    }

    public static void start_PowerActivity(Activity activity) {
//电源管理
        Intent intent = new Intent(activity, PowerActivity.class);
        activity.startActivity(intent);
    }

    public static void start_FirmwareUpdateActivity(Activity activity) {
//固件更新
        Intent intent = new Intent(activity, FirmwareUpdateActivity.class);
        activity.startActivity(intent);
    }

    public static void start_MoveDeviceActivity(Activity activity) {
//删除设备
        Intent intent = new Intent(activity, MoveDeviceActivity.class);
        activity.startActivity(intent);
    }

    public static void start_SetAlarmActivity(Activity activity, int i, String string, String strRepeat) {
        Intent intent = new Intent(activity, SetAlarmActivity.class);
        intent.putExtra("str", i);
        intent.putExtra("str_tv", string);
        intent.putExtra("strRepeat", strRepeat);
        activity.startActivityForResult(intent, i);

    }

    //朋友圈
    public static void start_FriendActivity(Activity activity, int i) {
        Intent intent = new Intent(activity, FriendActivity.class);
        intent.putExtra("_jump_friend_", i);
        activity.startActivity(intent);
    }

    public static void start_RankingActivityIntent(Activity activity) {
        Intent intent = new Intent(activity, RankingActivity.class);
        activity.startActivity(intent);
    }
    //进入主界面
    public static Intent createPortalActivityIntent(Activity activity) {

        Intent intent = new Intent(activity, PortalActivity.class);

        return intent;
    }


    public static void start_Attention_Activity(Activity activity) {

        Intent intent = new Intent(activity, AttentionActivity.class);

        activity.startActivity(intent);

    }

    public static Intent start_MoreActivityIntent(Activity activity) {
        //        无声闹钟
        Intent alarmIntent = new Intent(activity, MoreActivity.class);
        return alarmIntent;

    }


    public static Intent start_RegisterSuccess_Activity(Activity thisActivity, String u) {
        Intent intent = new Intent(thisActivity, RegisteredSuccessActivity.class);
        intent.putExtra("__UserRegisterDTO__", u);
        return intent;
    }

    public static Intent start_BodyActivityIntent(Activity thisActivity) {
        Intent intent = new Intent(thisActivity, BodyActivity.class);
        return intent;
    }

    /**
     * 解析intent传过来的RegisterActivity数据
     *
     * @param intent
     * @return
     */
    public static UserRegisterDTO parseRegisterSuccessIntent(Intent intent) {
        return (UserRegisterDTO) intent.getSerializableExtra("__UserRegisterDTO__");
    }

    public static Intent create_PersonalInfo_Activity_Intent(Activity activity) {

        Intent intent = new Intent(activity, PersonalInfoActivity.class);
        return intent;
    }

    /**
     * 打开GoalActivity的Intent构造方法
     */
    public static void startGoalActivityIntent(Activity thisActivity, UserEntity user) {
        Intent intent = new Intent(thisActivity, SportGoalActivity.class);

        double bmi = ToolKits.getBMI(CommonUtils.getFloatValue(user.getUserBase().getUser_weight() + ""), CommonUtils.getIntValue(user.getUserBase().getUser_height()));

        intent.putExtra("user_sex", user.getUserBase().getUser_sex());
        intent.putExtra("user_BMI", bmi + "");
        intent.putExtra("user_BMIDesc", ToolKits.getBMIDesc(thisActivity, bmi));
        intent.putExtra("user_target", user.getUserBase().getPlay_calory() + "");
        thisActivity.startActivity(intent);
    }

    public static void start_CustomerService_ActivityIntent(Activity activity, int index) {
        Intent intent = new Intent(activity, CustomerServiceActivity.class);
        intent.putExtra("__inedx__", index);
        activity.startActivity(intent);
    }

    /**
     * 通用WebActivity
     *
     * @param thisActivity
     * @param url
     * @return
     */
    public static Intent createCommonWebActivityIntent(Activity thisActivity, String url) {
        Intent intent = new Intent(thisActivity, CommonWebActivity.class);
        intent.putExtra("__url__", url);
        return intent;
    }

    public static String parseCommonWebIntent(Intent intent) {
        return intent.getStringExtra("__url__");
    }

    //步数数据
    public static Intent cteate_StepDataActivityIntent(Activity portalActivity) {
        Intent intent = new Intent(portalActivity, StepDataActivity.class);
        return intent;
    }

    //距离数据
    public static Intent cteate_DiatanceDataActivityIntent(Activity portalActivity) {
        Intent intent = new Intent(portalActivity, DistanceDataActivity.class);
        return intent;
    }

    //卡路里数据
    public static Intent cteate_CalDataActivityIntent(Activity portalActivity) {
        Intent intent = new Intent(portalActivity, CalDataActivity.class);
        return intent;
    }

    //睡眠数据
    public static Intent cteate_SleepDataActivityIntent(Activity portalActivity) {
        Intent intent = new Intent(portalActivity, SleepDataActivity.class);
        return intent;
    }

//好友信息界面
    /*public static Intent create_FriendInfoActivity(Context context) {
        Intent intent = new Intent(context, FriendInfoActivity.class);
        return intent;
    }*/

    public static Intent create_FriendInfoActivity(Context context, String userID, String userAvatar) {
        Intent intent = new Intent(context, FriendInfoActivity.class);
        intent.putExtra("__user_id__", userID);
        intent.putExtra("__UserAvatar__", userAvatar);
        return intent;
    }

//    public static Intent create_FriendInfoActivity(Context context,String userID)
//    {
//        Intent intent = new Intent(context, FriendInfoActivity.class);
//        intent.putExtra("__user_id__", userID);
//        return intent;
//    }

    //钱包页面
    public static Intent start_WalletActivityIntent(Activity activity) {
        Intent alarmIntent = new Intent(activity, WalletActivity.class);
        return alarmIntent;

    }


    public static int[] parseUserDetialActivityIntent(Intent intent) {
        int[] tmp = new int[3];
        tmp[0] = intent.getExtras().getInt("__user_id__");
        tmp[1] = intent.getExtras().getInt("__to_user_id__");
        tmp[2] = intent.getExtras().getInt("__check_tag_");
        return tmp;
    }

    public static Intent createUserDetialActivityIntent(Activity thisActivity, int user_id, int to_user_id, int tag) {
        Intent intent = new Intent(thisActivity, CommentActivity.class);
        intent.putExtra("__user_id__", user_id);
        intent.putExtra("__to_user_id__", to_user_id);
        intent.putExtra("__check_tag_", tag);
        return intent;
    }

    //绑定手环
    public static Intent startActivityBundBand(Context context) {
        Intent intent = new Intent(context, bundbandstep1.class);
        return intent;
    }

    //绑定手环列表
    public static void startBundList(Context context) {
        Intent intent = new Intent(context, BandListActivity.class);
        context.startActivity(intent);
    }

}

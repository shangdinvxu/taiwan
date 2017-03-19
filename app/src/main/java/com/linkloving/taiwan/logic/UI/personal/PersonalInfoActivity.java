package com.linkloving.taiwan.logic.UI.personal;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.linkloving.taiwan.CommParams;
import com.linkloving.taiwan.MyApplication;
import com.linkloving.taiwan.R;
import com.linkloving.taiwan.basic.toolbar.ToolBarActivity;
import com.linkloving.taiwan.http.basic.CallServer;
import com.linkloving.taiwan.http.basic.HttpCallback;
import com.linkloving.taiwan.http.basic.NoHttpRuquestFactory;
import com.linkloving.taiwan.http.data.DataFromServer;
import com.linkloving.taiwan.logic.UI.personal.util.FileHelper;
import com.linkloving.taiwan.logic.UI.personal.util.SelectPicPopupWindow;
import com.linkloving.taiwan.logic.UI.ranking.rankinglistitem.AvatarHelper;
import com.linkloving.taiwan.logic.dto.ModifyPasswordDTO;
import com.linkloving.taiwan.logic.dto.UserBase;
import com.linkloving.taiwan.logic.dto.UserEntity;
import com.linkloving.taiwan.utils.CommonUtils;
import com.linkloving.taiwan.utils.CountCalUtil;
import com.linkloving.taiwan.utils.MyToast;
import com.linkloving.taiwan.utils.ToolKits;
import com.linkloving.taiwan.utils.logUtils.MyLog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.yolanda.nohttp.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PersonalInfoActivity extends ToolBarActivity implements View.OnClickListener {
    public final static String TAG = PersonalInfoActivity.class.getSimpleName();
    LinearLayout linearLayout_nick_name,linearLayout_account,linearLayout_pwd,linearLayout_sex,linearLayout_bir,linearLayout_lenth,linearLayout_sign;
    RelativeLayout linearLayout_main;
    TextView name,account,sextv,birthday,lenth,sign;
    ImageView user_head;
    private UserEntity userEntity;
    private UserBase dataU;
    //自定义的弹出框类
    private PopupWindow menuWindow = null;
    // 修改头像的临时文件存放路径（头像修改成功后，会自动删除之）
    private String __tempImageFileLocation = null;
    /** 回调常量之：拍照 */
    private static final int TAKE_BIG_PICTURE = 991;
    /** 回调常量之：拍照后裁剪 */
    private static final int CROP_BIG_PICTURE = 993;
//	/** 回调常量之：从相册中选取 */
//	private static final int CHOOSE_BIG_PICTURE = 995;
    /** 回调常量之：从相册中选取2 */
    private static final int CHOOSE_BIG_PICTURE2 = 996;
    /** 图像保存大小（微信的也是这个大小） */
    private static final int AVATAR_SIZE = 640;

    private static ProgressDialog pd;// 等待进度圈

    File fileOfTempAvatar;

    private String resultStr = "";	// 服务端返回结果集

    boolean isShowDatadig;  //防止弹出框出现2次的判断
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Uri imagePhotoUri = getTempImageFileUri();
        switch (requestCode)
        {
            case TAKE_BIG_PICTURE:// 拍照完成则新拍的文件将会存放于指定的位置（即uri、tempImaheLocation所表示的地方）
            {
                if (resultCode == RESULT_OK) {
                    //从相机拍摄保存的Uri中取出图片，调用系统剪裁工具
                    if (imagePhotoUri != null) {
                        startPhotoZoom(imagePhotoUri,AVATAR_SIZE, AVATAR_SIZE, CROP_BIG_PICTURE);
                    } else {
//                        MyToast.showShort(this, "没有得到拍照图片");
                    }
                } else if (resultCode == RESULT_CANCELED) {

//                        MyToast.showShort(this, "取消拍照");

                } else {

//                   MyToast.showShort(this, "拍照失败");

                }



                break;
            }
            //裁切完成后的处理（上传头像）
            case CROP_BIG_PICTURE://from crop_big_picture
            {
                if (resultCode == RESULT_OK) {
                    MyLog.i("裁剪完成");
                    processAvatarUpload(imagePhotoUri);
                }else {
//                        MyToast.showShort(this, "裁剪取消");
                }
                break;
            }

            case CHOOSE_BIG_PICTURE2:// 图片选取完成时，其实该图片还有原处，如要裁剪则应把它复制出来一份（以免裁剪时覆盖原图)
            {

                if (resultCode == RESULT_OK) {
                    MyLog.i("选择图片,裁剪完成");
                    processAvatarUpload(imagePhotoUri);
                }else {
//                        MyToast.showShort(this, "选择图片取消");
                }
                break;
            }
        }
    }
    /**
     * 拍照后裁剪图片方法实现
     *
     */
    public void startPhotoZoom(Uri uri, int outputX, int outputY, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, requestCode);
    }
    /*进入相册*/
    private void startPhoto (){
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", AVATAR_SIZE);
        intent.putExtra("outputY", AVATAR_SIZE);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempImageFileUri());
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, CHOOSE_BIG_PICTURE2);
    }


    private Bitmap decodeUriAsBitmap(Uri uri){
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

    }

    @Override
    protected void getIntentforActivity() {

    }

    @Override
    protected void initView() {
        SetBarTitleText(getString(R.string.pesonal_activity_title));
        linearLayout_main= (RelativeLayout) findViewById(R.id.layout_peosoninfo);
        linearLayout_nick_name= (LinearLayout) findViewById(R.id.linear_name);
        linearLayout_account=(LinearLayout)findViewById(R.id.linear_account);
        linearLayout_pwd=(LinearLayout)findViewById(R.id.linear_pwd);
        linearLayout_account.setVisibility(View.GONE);
        linearLayout_pwd.setVisibility(View.GONE);
        linearLayout_sex=(LinearLayout)findViewById(R.id.linear_sex);
        linearLayout_bir=(LinearLayout)findViewById(R.id.linear_birthday);
        linearLayout_lenth=(LinearLayout)findViewById(R.id.linear_lenth);
        linearLayout_sign=(LinearLayout)findViewById(R.id.linear_sign);
        name= (TextView) findViewById(R.id.text_nick_name);
        account= (TextView) findViewById(R.id.text_account);
        sextv= (TextView) findViewById(R.id.text_sex);
        birthday= (TextView) findViewById(R.id.text_birthday);
        lenth= (TextView) findViewById(R.id.text_lenth);
        sign= (TextView) findViewById(R.id.text_sign);
        user_head= (ImageView) findViewById(R.id.user_head);
        userEntity = MyApplication.getInstance(this).getLocalUserInfoProvider();
        refreshDatas();//更新数据
        loadImage();//下载图像
    }
    private void loadImage() {
        if(userEntity==null){
            return;
        }
        String url= NoHttpRuquestFactory.getUserAvatarDownloadURL(PersonalInfoActivity.this, userEntity.getUser_id()+"", userEntity.getUserBase().getUser_avatar_file_name(), true);
        DisplayImageOptions options;
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                .cacheOnDisc(true)//设置下载的图片是否缓存在SD卡中
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                .resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                .build();//构建完成
        ImageLoader.getInstance().displayImage(url, user_head, options, new ImageLoadingListener(){
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }
            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                if(userEntity.getUserBase().getUser_sex()==0){
                    //女生
                    user_head.setImageResource(R.mipmap.default_avatar_m);
                }else {
                    //男生
                    user_head.setImageResource(R.mipmap.default_avatar);
                }
            }
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                user_head.setImageBitmap(loadedImage);
            }
            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if(userEntity.getUserBase().getUser_sex()==0){
                    //女生
                    user_head.setImageResource(R.mipmap.default_avatar_m);
                }else {
                    //男生
                    user_head.setImageResource(R.mipmap.default_avatar);
                }
            }
        });

    }

    private void refreshDatas() {
        UserEntity userEntity = MyApplication.getInstance(PersonalInfoActivity.this).getLocalUserInfoProvider();
        if(userEntity != null) {
            name.setText(userEntity.getUserBase().getNickname());
            // 如果是QQ登陆就不显示邮件地址了（也没有邮件地址）
            if (1==userEntity.getUserBase().getUser_type())
                account.setText(getString(R.string.main_more_from_qq_login));
            else if (3==userEntity.getUserBase().getUser_type())
                account.setText(getString(R.string.main_more_from_wx_login));
            else
                account.setText(userEntity.getUserBase().getUser_mail());

            if(userEntity.getUserBase().getUser_sex()==1){
                sextv.setText(getString(R.string.general_male));
            }else{
                sextv.setText(getString(R.string.general_female));
            }
            birthday.setText(userEntity.getUserBase().getBirthdate());
            lenth.setText(userEntity.getUserBase().getUser_height()+"");
            if(userEntity.getUserBase().getWhat_s_up()==null)
                sign.setText("");
            else
                sign.setText(userEntity.getUserBase().getWhat_s_up()+"");

        }

    }

    @Override
    protected void initListeners() {
        user_head.setOnClickListener(this);
        linearLayout_nick_name.setOnClickListener(this);
        linearLayout_pwd.setOnClickListener(this);
        linearLayout_sex.setOnClickListener(this);
        linearLayout_bir.setOnClickListener(this);
        linearLayout_lenth.setOnClickListener(this);
        linearLayout_sign.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        switch (v.getId()){
            case R.id.user_head:
//                initpopwindow();
                //修改头像
                break;
            case R.id.linear_name:
                //修改昵称
          /*      final Dialog alertDialog1=new Dialog(PersonalInfoActivity.this);
                alertDialog1.setTitle(getString(R.string.user_info_update_nick_name_title));
                // alertDialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
                View nicklayout = inflater.inflate(R.layout.nicknamedialog,null);
                final EditText nickname;
                Button nick_ok,nick_no;
                nickname= (EditText) nicklayout.findViewById(R.id.nickname);
                nickname.setInputType(InputType.TYPE_CLASS_TEXT);
                nick_ok= (Button) nicklayout.findViewById(R.id.ok);
                nick_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (CommonUtils.isStringEmpty(nickname.getText().toString().trim())) {
                            MyLog.i(TAG, "用户名是空的啊 啊啊啊啊啊 啊啊啊啊啊啊a");
                            nickname.setError(getString(R.string.error_field_required));
                            ToolKits.HideKeyboard(v);
                            return;
                        }else {

                            if (!nickname.getText().toString().trim().equals(userEntity.getUserBase().getNickname())) {
                                dataU = userEntity.getUserBase();
                                dataU.setNickname(nickname.getText().toString());
                                CallServer.getRequestInstance().add(PersonalInfoActivity.this, getString(R.string.changeinfo), CommParams.HTTP_USER_NAME, NoHttpRuquestFactory.submit_RegisterationToServer_Modify(dataU), httpCallback);
                                alertDialog1.dismiss();
                                ToolKits.HideKeyboard(v);
                            } else {
                                //用户名不变
                            }
                        }
                    }
                });
                nick_no= (Button) nicklayout.findViewById(R.id.no);
                nick_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog1.dismiss();
                        ToolKits.HideKeyboard(v);
                    }
                });
                alertDialog1.setCanceledOnTouchOutside(false);
                alertDialog1.addContentView(nicklayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                alertDialog1.show();*/
                View nicklayout = inflater.inflate(R.layout.nicknamedialog,null);
                final EditText mynickname;
                mynickname= (EditText) nicklayout.findViewById(R.id.nickname);
                mynickname.setInputType(InputType.TYPE_CLASS_TEXT);
                final AlertDialog alertDialog1=new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.user_info_update_nick_name_title))
                        .setView(nicklayout)
                        .setPositiveButton(getString(R.string.general_ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MyLog.i(TAG, "用户名" + userEntity.getUserBase().getNickname());
                                        if (CommonUtils.isStringEmpty(mynickname.getText().toString().trim())) {
                                            MyLog.i(TAG, "用户名是空的啊 啊啊啊啊啊 啊啊啊啊啊啊a");
                                            mynickname.setError(getString(R.string.error_field_required));
                                            CountCalUtil.allowCloseDialog(dialog,false);
                                        }else {
                                            if (!mynickname.getText().toString().trim().equals(userEntity.getUserBase().getNickname())) {
                                                userEntity.getUserBase().setNickname(mynickname.getText().toString().trim());
                                                name.setText(mynickname.getText().toString().trim());
                                                MyApplication.getInstance(PersonalInfoActivity.this).setLocalUserInfoProvider(userEntity);
                                                ToolKits.showCommonTosat(PersonalInfoActivity.this, true, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.changeinfook), Toast.LENGTH_SHORT);
//                                                if (MyApplication.getInstance(PersonalInfoActivity.this).isLocalDeviceNetworkOk()) {
//                                                    //网络连接了
//                                                    dataU = userEntity.getUserBase();
//                                                    dataU.setNickname(mynickname.getText().toString());
//                                                    CallServer.getRequestInstance().add(PersonalInfoActivity.this, false, CommParams.HTTP_USER_NAME, NoHttpRuquestFactory.submit_RegisterationToServer_Modify(dataU), httpCallback);
//                                                    CountCalUtil.allowCloseDialog(dialog, true);
//                                                }else {
//                                                    MyToast.show(PersonalInfoActivity.this, getString(R.string.general_network_faild), Toast.LENGTH_SHORT);
//                                                }
                                            } else {
                                                //用户名不变
                                                CountCalUtil.allowCloseDialog(dialog,true);

                                            }
                                        }
                                    }
                                })
                            .setNegativeButton(getString(R.string.general_cancel), new DatePickerDialog.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CountCalUtil.allowCloseDialog(dialog,true);
                            }
                        })
                        .show();
                alertDialog1.setCanceledOnTouchOutside(false);
                break;
            case R.id.linear_pwd:
                final View pwd_layout = inflater.inflate(R.layout.pwd, (ViewGroup) findViewById(R.id.pwd));
                final EditText lastpwd,newpwd,morepwd;
                lastpwd= (EditText) pwd_layout.findViewById(R.id.lastpwd);
                newpwd= (EditText) pwd_layout.findViewById(R.id.newpwd);
                morepwd= (EditText) pwd_layout.findViewById(R.id.morepwd);
                final AlertDialog alertDialog2=new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.user_info_update_psw_title))
                        .setView(pwd_layout)
                        .setPositiveButton(getString(R.string.general_ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        boolean valid = updatePswByVlidate(lastpwd, newpwd, morepwd);
                                        if (valid) {
                                            MyLog.i(TAG, "kaishi>>>>>>>>修改密码");
                                            String newPsw = String.valueOf(newpwd.getText());
                                            String oldPsw = String.valueOf(lastpwd.getText());
                                            ModifyPasswordDTO temp = new ModifyPasswordDTO();
                                            temp.setUser_id(userEntity.getUser_id());
                                            temp.setOldPwd(oldPsw);
                                            temp.setNewPwd(newPsw);
                                            if(MyApplication.getInstance(PersonalInfoActivity.this).isLocalDeviceNetworkOk()){
                                                CallServer.getRequestInstance().add(PersonalInfoActivity.this, getString(R.string.changeinfo), CommParams.HTTP_USER_PWD, NoHttpRuquestFactory.submit_pwd_ToServer_Modify(temp), httpCallback);
                                            }else {
                                                MyToast.show(PersonalInfoActivity.this, getString(R.string.general_network_faild), Toast.LENGTH_SHORT);
                                            }
                                            ToolKits.HideKeyboard(pwd_layout);
                                            CountCalUtil.allowCloseDialog(dialog, true);
                                        }else {
                                            CountCalUtil.allowCloseDialog(dialog, false);
                                        }

                                    }
                                })
                            .setNegativeButton(getString(R.string.general_cancel), new DatePickerDialog.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CountCalUtil.allowCloseDialog(dialog,true);
                            }
                        })
                        .show();
                alertDialog2.setCanceledOnTouchOutside(false);
               /* final Dialog alertDialog2=new Dialog(PersonalInfoActivity.this);
                alertDialog2.setTitle(getString(R.string.user_info_update_psw_title));
                View layout = inflater.inflate(R.layout.pwd, (ViewGroup) findViewById(R.id.pwd));
                final EditText lastpwd,newpwd,morepwd;
                Button ok,no;
                lastpwd= (EditText) layout.findViewById(R.id.lastpwd);
                newpwd= (EditText) layout.findViewById(R.id.newpwd);
                morepwd= (EditText) layout.findViewById(R.id.morepwd);
                ok= (Button) layout.findViewById(R.id.ok);
                no= (Button) layout.findViewById(R.id.no);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean valid = updatePswByVlidate(lastpwd, newpwd, morepwd);
                        if (valid) {
                            MyLog.i(TAG, "kaishi>>>>>>>>修改密码");
                            String newPsw = String.valueOf(newpwd.getText());
                            String oldPsw = String.valueOf(lastpwd.getText());
                            ModifyPasswordDTO temp = new ModifyPasswordDTO();
                            temp.setUser_id(userEntity.getUser_id());
                            temp.setOldPwd(oldPsw);
                            temp.setNewPwd(newPsw);
                            CallServer.getRequestInstance().add(PersonalInfoActivity.this, getString(R.string.changeinfo), CommParams.HTTP_USER_PWD, NoHttpRuquestFactory.submit_pwd_ToServer_Modify(temp), httpCallback);
                            ToolKits.HideKeyboard(v);
                            alertDialog2.dismiss();
                        }
                    }
                });
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToolKits.HideKeyboard(v);
                        alertDialog2.dismiss();
                    }
                });
                alertDialog2.setCanceledOnTouchOutside(false);
                alertDialog2.addContentView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                alertDialog2.show();*/
                break;
            case R.id.linear_sex:
                /*final Dialog alertDialog3=new Dialog(PersonalInfoActivity.this);
                alertDialog3.setTitle(getString(R.string.sex));
                alertDialog3.requestWindowFeature(Window.FEATURE_NO_TITLE);
                View sexLayout = inflater.inflate(R.layout.modify_sex_dialog,null);
                final RadioButton man= (RadioButton) sexLayout.findViewById(R.id.rb_left);
                man.setText(getString(R.string.general_male));
                final RadioButton woman=(RadioButton) sexLayout.findViewById(R.id.rb_right);
                woman.setText(getString(R.string.general_female));
                Button sex_ok,sex_no;
                sex_ok= (Button) sexLayout.findViewById(R.id.ok);
                sex_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int sex=0;
                        if(man.isChecked()){
                            sex=1;
                        }else if(woman.isChecked()){
                            sex=0;
                        }
                        MyLog.e(TAG,"sex="+sex+"userEntity.getUserBase().getUser_sex()="+userEntity.getUserBase().getUser_sex());
                        if(!(userEntity.getUserBase().getUser_sex()==sex)){
                            dataU = userEntity.getUserBase();
                            dataU.setUser_sex(sex);
                            CallServer.getRequestInstance().add(PersonalInfoActivity.this, getString(R.string.changeinfo), CommParams.HTTP_USER_SEX, NoHttpRuquestFactory.submit_RegisterationToServer_Modify(dataU), httpCallback);
                            alertDialog3.dismiss();
                        }else{
                            MyLog.e(TAG," //没变动sex="+sex+"userEntity.getUserBase().getUser_sex()="+userEntity.getUserBase().getUser_sex());
                        }
                    }
                });
                sex_no= (Button) sexLayout.findViewById(R.id.no);
                sex_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog3.dismiss();
                    }
                });
                alertDialog3.setCanceledOnTouchOutside(false);
                alertDialog3.addContentView(sexLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                alertDialog3.show();*/
                View layoutsex = inflater.inflate(R.layout.modify_sex_dialogfor_two, (ViewGroup) findViewById(R.id.linear_modify_sex));
                final RadioButton man= (RadioButton) layoutsex.findViewById(R.id.rb_left);
                man.setText(getString(R.string.general_male));
                final RadioButton woman=(RadioButton) layoutsex.findViewById(R.id.rb_right);
                woman.setText(getString(R.string.general_female));
                new android.support.v7.app.AlertDialog.Builder(this)
                        .setTitle(getString(R.string.sex))
                        .setView(layoutsex)
                        .setPositiveButton(getString(R.string.general_ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int sex=0;
                                        if(man.isChecked()){
                                            sex=1;
                                        }else if(woman.isChecked()){
                                            sex=0;
                                        }
                                        if(!(userEntity.getUserBase().getUser_sex()==sex)){
                                            dataU = userEntity.getUserBase();
                                            dataU.setUser_sex(sex);
                                            MyApplication.getInstance(PersonalInfoActivity.this).getLocalUserInfoProvider().getUserBase().setUser_sex(dataU.getUser_sex());
                                            MyApplication.getInstance(PersonalInfoActivity.this).setLocalUserInfoProvider(userEntity);
                                            switch (dataU.getUser_sex()){
                                                case 0:
                                                    sextv.setText(getString(R.string.general_female));
                                                    user_head.setImageResource(R.mipmap.default_avatar_m);
                                                    break;
                                                case 1:
                                                    sextv.setText(getString(R.string.general_male));
                                                    user_head.setImageResource(R.mipmap.default_avatar);
                                                    break;
                                            }
                                            ToolKits.showCommonTosat(PersonalInfoActivity.this, true, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.changeinfook), Toast.LENGTH_SHORT);

                                            }else{

                                        }
                                    }
                                })
                        .setNegativeButton(getString(R.string.general_cancel), null)
                        .create().show();
                break;
            case R.id.linear_birthday:
                final Calendar calendar;// 用来装日期的
                calendar = Calendar.getInstance();
                DatePickerDialog dialog1;
                dialog1 = new DatePickerDialog(PersonalInfoActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        MyLog.e(TAG, "dialog显示了");
                        if(isShowDatadig) return;
                        isShowDatadig = true;
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        Date date = new Date(calendar.getTimeInMillis());
                        final String endDateString = new SimpleDateFormat(ToolKits.DATE_FORMAT_YYYY_MM_DD).format(date);
                        if(ToolKits.compareDate(date,new Date())){
                            Toast.makeText(PersonalInfoActivity.this,getString(R.string.ranking_wait_tomorrow), Toast.LENGTH_SHORT).show();
                            isShowDatadig = false ;
                        }else {
                            MyLog.e(TAG, "riqi" + endDateString);
                            android.support.v7.app. AlertDialog dialog =  new android.support.v7.app.AlertDialog.Builder(PersonalInfoActivity.this)
                                    .setTitle(getString(R.string.birthday))
                                    .setMessage(getString(R.string.date_picker_activity_title)+":"+endDateString)
                                    .setPositiveButton(getString(R.string.general_ok),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    isShowDatadig = false ;
                                                    dataU = userEntity.getUserBase();
                                                    dataU.setBirthdate(endDateString);
                                                    birthday.setText(dataU.getBirthdate());
                                                    MyApplication.getInstance(PersonalInfoActivity.this).setLocalUserInfoProvider(userEntity);
                                                    ToolKits.showCommonTosat(PersonalInfoActivity.this, true, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.changeinfook), Toast.LENGTH_SHORT);
//                                                    if (MyApplication.getInstance(PersonalInfoActivity.this).isLocalDeviceNetworkOk()) {
//                                                        CallServer.getRequestInstance().add(PersonalInfoActivity.this, getString(R.string.changeinfo), CommParams.HTTP_USER_BIRTHDAY, NoHttpRuquestFactory.submit_RegisterationToServer_Modify(dataU), httpCallback);
//                                                    }else {
//                                                        MyToast.show(PersonalInfoActivity.this, getString(R.string.general_network_faild), Toast.LENGTH_SHORT);
//                                                    }
                                                }
                                            })
                                    .setNegativeButton(getString(R.string.general_cancel), new DialogInterface.OnClickListener(){

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            isShowDatadig = false ;
                                        }
                                    }).create();
                                    if(!dialog.isShowing())
                                        dialog.show();
                        }
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                dialog1.show();
               /* final EditText mybirthday=new EditText(this);
                mybirthday.setInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
                new android.support.v7.app.AlertDialog.Builder(this)
                        .setTitle(getString(R.string.birthday))
                        .setView(mybirthday)
                        .setPositiveButton(getString(R.string.general_ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                        .setNegativeButton(getString(R.string.general_cancel), null)
                        .create().show();*/
                break;
            case R.id.linear_lenth:
                //修改身高
               /* final Dialog alertDialog4=new Dialog(PersonalInfoActivity.this);
                alertDialog4.setTitle(getString(R.string.body_info_height));
                //alertDialog4.requestWindowFeature(Window.FEATURE_NO_TITLE);
                View lenthlayout = inflater.inflate(R.layout.nicknamedialog,null);
                final EditText mylenth;
                Button lenth_ok,lenth_no;
                mylenth= (EditText) lenthlayout.findViewById(R.id.nickname);
                mylenth.setInputType(InputType.TYPE_CLASS_NUMBER);
                lenth_ok= (Button) lenthlayout.findViewById(R.id.ok);
                lenth_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (CommonUtils.isStringEmpty(mylenth.getText().toString().trim())) {
                            mylenth.setError(getString(R.string.error_field_required));
                            ToolKits.HideKeyboard(v);
                            return;
                        }else {
                            if (!(Integer.parseInt(mylenth.getText().toString().trim()) ==(userEntity.getUserBase().getUser_height()))) {
                                dataU = userEntity.getUserBase();
                                dataU.setUser_height(Integer.parseInt(mylenth.getText().toString().trim()));
                                CallServer.getRequestInstance().add(PersonalInfoActivity.this, getString(R.string.changeinfo), CommParams.HTTP_USER_HEIGHT, NoHttpRuquestFactory.submit_RegisterationToServer_Modify(dataU), httpCallback);
                                alertDialog4.dismiss();
                                ToolKits.HideKeyboard(v);
                            } else {
                                //用户名不变
                            }
                        }
                    }
                });
                lenth_no= (Button) lenthlayout.findViewById(R.id.no);
                lenth_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog4.dismiss();
                        ToolKits.HideKeyboard(v);
                    }
                });
                alertDialog4.setCanceledOnTouchOutside(false);
                alertDialog4.addContentView(lenthlayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
                alertDialog4.show();*/
                View lenth_layout = inflater.inflate(R.layout.nicknamedialog,null);
                final EditText mylenth= (EditText) lenth_layout.findViewById(R.id.nickname);
                mylenth.setInputType(InputType.TYPE_CLASS_NUMBER);
                AlertDialog alertDialog4= new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.body_info_height))
                        .setView(lenth_layout)
                        .setPositiveButton(getString(R.string.general_ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MyLog.i(TAG,"用户名"+userEntity.getUserBase().getNickname());

                                        if (CommonUtils.isStringEmpty(mylenth.getText().toString().trim()))
                                        {
                                            mylenth.setError(getString(R.string.error_field_required));
                                            ToolKits.HideKeyboard(mylenth);
                                            CountCalUtil.allowCloseDialog(dialog, false);
                                            return;
                                        }else
                                        {
                                            if (!(Integer.parseInt(mylenth.getText().toString().trim()) ==(userEntity.getUserBase().getUser_height()))) {
                                                int height = CommonUtils.getIntValue(mylenth.getText().toString());
                                                if (height < 60 || height > 272) {
                                                    mylenth.setError(getString(R.string.body_info_height_recommend));
                                                    //et_userHeight.setError(getString(R.string.body_info_height_recommend));
                                                    ToolKits.HideKeyboard(mylenth);
                                                    CountCalUtil.allowCloseDialog(dialog, false);
                                                    return;
                                                }
                                                dataU = userEntity.getUserBase();
                                                userEntity.getUserBase().setUser_height(Integer.parseInt(mylenth.getText().toString().trim()));
                                                lenth.setText(dataU.getUser_height() + "");
                                                MyApplication.getInstance(PersonalInfoActivity.this).setLocalUserInfoProvider(userEntity);
                                                ToolKits.showCommonTosat(PersonalInfoActivity.this, true, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.changeinfook), Toast.LENGTH_SHORT);
//                                                if (MyApplication.getInstance(PersonalInfoActivity.this).isLocalDeviceNetworkOk()) {
//                                                    CallServer.getRequestInstance().add(PersonalInfoActivity.this, getString(R.string.changeinfo), CommParams.HTTP_USER_HEIGHT, NoHttpRuquestFactory.submit_RegisterationToServer_Modify(dataU), httpCallback);
//                                                }else {
//                                                    MyToast.show(PersonalInfoActivity.this, getString(R.string.general_network_faild), Toast.LENGTH_SHORT);
//                                                }
                                                ToolKits.HideKeyboard(mylenth);
                                                CountCalUtil.allowCloseDialog(dialog, true);
                                            } else {
                                                //用户名不变
                                                CountCalUtil.allowCloseDialog(dialog, true);
                                            }
                                        }
                                    }
                                })
                        .setNegativeButton(getString(R.string.general_cancel), new DatePickerDialog.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CountCalUtil.allowCloseDialog(dialog,true);
                            }
                        })
                        .show();
                alertDialog4.setCanceledOnTouchOutside(false);
                break;
            case R.id.linear_sign:
               /* final Dialog alertDialog5=new Dialog(PersonalInfoActivity.this);
                alertDialog5.setTitle(getString(R.string.sign));
                // alertDialog5.requestWindowFeature(Window.FEATURE_NO_TITLE);
                View signlayout = inflater.inflate(R.layout.nicknamedialog,null);
                final EditText mysign;
                Button sign_ok,sign_no;
                mysign= (EditText) signlayout.findViewById(R.id.nickname);
                mysign.setInputType(InputType.TYPE_CLASS_TEXT);
                sign_ok= (Button) signlayout.findViewById(R.id.ok);
                sign_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (CommonUtils.isStringEmpty(mysign.getText().toString().trim())) {
                            MyLog.i(TAG, "用户名是空的啊 啊啊啊啊啊 啊啊啊啊啊啊a");
                            mysign.setError(getString(R.string.error_field_required));
                            ToolKits.HideKeyboard(v);
                            return;
                        }else {
                            if (!mysign.getText().toString().trim().equals(userEntity.getUserBase().getWhat_s_up())) {
                                dataU = userEntity.getUserBase();
                                dataU.setWhat_s_up(mysign.getText().toString());
                                CallServer.getRequestInstance().add(PersonalInfoActivity.this, getString(R.string.changeinfo), CommParams.HTTP_USER_SIGN, NoHttpRuquestFactory.submit_RegisterationToServer_Modify(dataU), httpCallback);
                                alertDialog5.dismiss();
                                ToolKits.HideKeyboard(v);
                            } else {
                                //不变
                            }
                        }
                    }
                });
                sign_no= (Button) signlayout.findViewById(R.id.no);
                sign_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog5.dismiss();
                        ToolKits.HideKeyboard(v);
                    }
                });
                alertDialog5.setCanceledOnTouchOutside(false);
                alertDialog5.addContentView(signlayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                alertDialog5.show();*/

                View sign_layout = inflater.inflate(R.layout.nicknamedialog,null);
                final EditText mysign;
                mysign= (EditText) sign_layout.findViewById(R.id.nickname);
                mysign.setInputType(InputType.TYPE_CLASS_TEXT);
//              final EditText mysign=new EditText(this);
                AlertDialog alertDialog5=new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.sign))
                        .setView(sign_layout)
                        .setPositiveButton(getString(R.string.general_ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MyLog.i(TAG,"用户名"+userEntity.getUserBase().getNickname());
                                        if(CommonUtils.isStringEmpty(mysign.getText().toString().trim())){
                                            mysign.setError(getString(R.string.error_field_required));
                                            ToolKits.HideKeyboard(mysign);
                                            CountCalUtil.allowCloseDialog(dialog, false);
                                            return;
                                        }else{
                                            dataU = userEntity.getUserBase();
                                            userEntity.getUserBase().setWhat_s_up(mysign.getText().toString());
                                            sign.setText(dataU.getWhat_s_up());
                                            MyApplication.getInstance(PersonalInfoActivity.this).setLocalUserInfoProvider(userEntity);
                                            ToolKits.showCommonTosat(PersonalInfoActivity.this, true, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.changeinfook), Toast.LENGTH_SHORT);

//                                            if (MyApplication.getInstance(PersonalInfoActivity.this).isLocalDeviceNetworkOk()) {
//
//                                                CallServer.getRequestInstance().add(PersonalInfoActivity.this, false, CommParams.HTTP_USER_SIGN, NoHttpRuquestFactory.submit_RegisterationToServer_Modify(dataU), httpCallback);
//
//                                            }else {
//                                                MyToast.show(PersonalInfoActivity.this, getString(R.string.general_network_faild), Toast.LENGTH_SHORT);
//                                            }


                                            ToolKits.HideKeyboard(mysign);
                                            CountCalUtil.allowCloseDialog(dialog, true);
                                        }
                                    }
                                })
                        .setNegativeButton(getString(R.string.general_cancel), new DatePickerDialog.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CountCalUtil.allowCloseDialog(dialog,true);
                            }
                        })
                        .show();
                alertDialog5.setCanceledOnTouchOutside(false);
                break;
        }
    }
    private boolean updatePswByVlidate(TextView viewOldPsw, TextView viewNewPsw, TextView viewReNewPsw)
    {
        String oldPsw = String.valueOf(viewOldPsw.getText());
        String newPsw = String.valueOf(viewNewPsw.getText());
        String reNewPsw = String.valueOf(viewReNewPsw.getText());
        // 当前密码是否为空
        if (CommonUtils.isStringEmpty(oldPsw))
        {
            viewOldPsw.setError(getString(R.string.general_invild));
            return false;
        }

        if (oldPsw.length()<6)
        {
            viewOldPsw.setError(getString(R.string.login_phone_pwd));
            return false;
        }

        // 新密码是否为空
        if (CommonUtils.isStringEmpty(newPsw))
        {
            viewNewPsw.setError(getString(R.string.general_invild));
            return false;
        }

        if(CommonUtils.isStringEmpty(reNewPsw))
        {
            viewReNewPsw.setError(getString(R.string.general_invild));
            return false;
        }

        //两次输入新密码是否一致
        if (!newPsw.equals(reNewPsw))
        {
            viewNewPsw.setError(getString(R.string.user_info_update_user_psw_new_psw_not_equal));
            viewReNewPsw.setError(getString(R.string.user_info_update_user_psw_new_psw_not_equal));
            return false;
        }

        // 新密码长度是否大于6
        if (newPsw.length() < 6)
        {
            viewNewPsw.setError(getString(R.string.user_info_update_user_psw_length));
            return false;
        }

        // 登录密码与新密码是否一致
        if (oldPsw.equals(newPsw))
        {
            viewNewPsw.setError(getString(R.string.user_info_update_user_psw_old_equal_new));
            return false;
        }
        return true;
    }
    private HttpCallback<String> httpCallback=new HttpCallback<String>(){
        @Override
        public void onSucceed(int what, Response<String> response) {
            MyLog.i(TAG, "response=" + JSON.toJSONString(response.get()));
            DataFromServer dataFromServer = JSON.parseObject(response.get(),DataFromServer.class);
            String value=dataFromServer.getReturnValue().toString();
            int errorCode=dataFromServer.getErrorCode();
            switch (what){
                case  CommParams.HTTP_USER_PWD:
                    MyLog.i(TAG, "errorCode=" + errorCode);
                    if(errorCode==1) {
//                        UserEntity user = MyApplication.getInstance(PersonalInfoActivity.this).getLocalUserInfoProvider();
//                        user.getUserBase().setUser_psw(dataU.getUser_psw());
                        ToolKits.showCommonTosat(PersonalInfoActivity.this, true, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.changeinfook), Toast.LENGTH_SHORT);
                    }
                    else if(errorCode==10006) {
//                        UserEntity user = MyApplication.getInstance(PersonalInfoActivity.this).getLocalUserInfoProvider();
//                        user.getUserBase().setUser_psw(dataU.getUser_psw());
                        ToolKits.showCommonTosat(PersonalInfoActivity.this, false, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.relationship_search_group_psderror), Toast.LENGTH_SHORT);
                    }else {
                        ToolKits.showCommonTosat(PersonalInfoActivity.this, false, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.general_faild), Toast.LENGTH_SHORT);

                    }
                    break;
                case CommParams.HTTP_USER_NAME :
//                    if(errorCode==1) {
//                        UserEntity user = MyApplication.getInstance(PersonalInfoActivity.this).getLocalUserInfoProvider();
//                        MyLog.i(TAG,"修改的名字="+dataU.getNickname());
//                        user.getUserBase().setNickname(dataU.getNickname());
//                        name.setText(dataU.getNickname());
//                        ToolKits.showCommonTosat(PersonalInfoActivity.this, true, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.changeinfook), Toast.LENGTH_SHORT);
//                    }else {
//                        ToolKits.showCommonTosat(PersonalInfoActivity.this, false, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.general_faild), Toast.LENGTH_SHORT);
//
//                    }
                    break;
                case CommParams.HTTP_USER_SEX:
                    if(errorCode==1) {
                        UserEntity user = MyApplication.getInstance(PersonalInfoActivity.this).getLocalUserInfoProvider();
                        user.getUserBase().setUser_sex(dataU.getUser_sex());
                        switch (dataU.getUser_sex()){
                            case 0:
                                sextv.setText(getString(R.string.general_female));
                                break;
                            case 1:
                                sextv.setText(getString(R.string.general_male));
                                break;
                        }
                        ToolKits.showCommonTosat(PersonalInfoActivity.this, true, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.changeinfook), Toast.LENGTH_SHORT);


                    }else {
                        ToolKits.showCommonTosat(PersonalInfoActivity.this, false, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.general_faild), Toast.LENGTH_SHORT);
                    }
                    break;

                case CommParams.HTTP_USER_BIRTHDAY:
                    if(errorCode==1) {
                        UserEntity user = MyApplication.getInstance(PersonalInfoActivity.this).getLocalUserInfoProvider();
                        user.getUserBase().setBirthdate(dataU.getBirthdate());
                        birthday.setText(dataU.getBirthdate());
                        ToolKits.showCommonTosat(PersonalInfoActivity.this, true, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.changeinfook), Toast.LENGTH_SHORT);
                    }else{
                        ToolKits.showCommonTosat(PersonalInfoActivity.this, false, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.general_faild), Toast.LENGTH_SHORT);
                    }


                    break;
                case CommParams.HTTP_USER_SIGN:
                    if(errorCode==1) {
                        UserEntity user = MyApplication.getInstance(PersonalInfoActivity.this).getLocalUserInfoProvider();
                        user.getUserBase().setWhat_s_up(dataU.getWhat_s_up());
                        sign.setText(dataU.getWhat_s_up());
                        ToolKits.showCommonTosat(PersonalInfoActivity.this, true, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.changeinfook), Toast.LENGTH_SHORT);
                    }else{
                        ToolKits.showCommonTosat(PersonalInfoActivity.this, false, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.general_faild), Toast.LENGTH_SHORT);
                    }
                    break;
                case CommParams.HTTP_USER_HEIGHT:
                    if(errorCode==1) {
                        UserEntity user = MyApplication.getInstance(PersonalInfoActivity.this).getLocalUserInfoProvider();
                        user.getUserBase().setUser_height(dataU.getUser_height());
                        lenth.setText(dataU.getUser_height() + "");
                        ToolKits.showCommonTosat(PersonalInfoActivity.this, true, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.changeinfook), Toast.LENGTH_SHORT);
                    }else {
                        ToolKits.showCommonTosat(PersonalInfoActivity.this, false, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.general_faild), Toast.LENGTH_SHORT);
                    }
                    break;
                case CommParams.HTTP_USER_HEAD:
                    if(pd!=null&&pd.isShowing()){
                        pd.dismiss();
                    }
                    Bitmap bitmap = decodeUriAsBitmap(getTempImageFileUri());
                    MyLog.i(TAG, "returnValue" + value);
                    JSONObject filename=JSON.parseObject(value);
                    user_head.setImageBitmap(bitmap);
                    // 先尝试删除所有自已的以前老的头像缓存（否则占用用户SD卡的空间）
                    AvatarHelper.deleteUserAvatar(PersonalInfoActivity.this, userEntity.getUser_id()+"", null);
                    // 先将临时文件（也就是本次拍好或选好的新头像）复制一份（更名
                    // 为正式的本地用户头像缓存文件名）作为本地用户图像的正式文件
                    try {
                        FileHelper.copyFile(fileOfTempAvatar, new File(fileOfTempAvatar.getParent()+"/"+filename.getString("fileName")));
                        // 将用户信息的修改实时同步到设备中
                        UserEntity user = MyApplication.getInstance(PersonalInfoActivity.this).getLocalUserInfoProvider();
                        user.getUserBase().setUser_avatar_file_name(filename.getString("fileName"));
                        MyLog.i(TAG,fileOfTempAvatar.getParent()+"/"+value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // 再删除用完的临时文件（也就是本次拍好或选好的新头像），因为
                    // 正式的本地缓存文件已生成，本临时文件就失去意义了，当然删除之
                    FileHelper.deleteFile(getTempImageFileLocation());
                    ToolKits.showCommonTosat(PersonalInfoActivity.this, true, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.changeinfook), Toast.LENGTH_SHORT);
                    break;
            }
        }
        @Override
        public void onFailed(int what, String url, Object tag, CharSequence message, int responseCode, long networkMillis) {
            if( what==CommParams.HTTP_USER_HEAD){
                MyLog.i(TAG, "图像上传失败+"+message);
                if(pd!=null&&pd.isShowing()){
                    pd.dismiss();
                }
            }
        }
    };

    //为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            menuWindow.dismiss();
            switch (v.getId()) {
                // 拍照
                case R.id.takePhotoBtn:
                    //进入拍照
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//action is capture
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempImageFileUri());
                    startActivityForResult(intent, TAKE_BIG_PICTURE);
                    break;
                // 相册选择图片
                case R.id.pickPhotoBtn:
                    startPhoto();//相册
                    break;
                default:
                    break;
            }
        }
    };


    private void initpopwindow()
    {
        menuWindow = new SelectPicPopupWindow(PersonalInfoActivity.this, itemsOnClick);

        menuWindow.showAtLocation(findViewById(R.id.layout_peosoninfo),
                Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    /**
     * 获得临时文件存放地址的Uri(此地址存在与否并不代表该文件一定存在哦).
     *
     * @return 正常获得uri则返回，否则返回null
     */
    private Uri getTempImageFileUri()
    {
        String tempImageFileLocation = getTempImageFileLocation();
        if(tempImageFileLocation != null)
        {
            return Uri.parse("file://" + tempImageFileLocation);
        }
        return null;
    }
    /**
     * 获得临时文件存放地址(此地址存在与否并不代表该文件一定存在哦).
     *
     * @return 正常获得则返回，否则返回null
     */
    private String getTempImageFileLocation()
    {
        try
        {
            if(__tempImageFileLocation == null)
            {
                String avatarTempDirStr = AvatarHelper.getUserAvatarSavedDir(PersonalInfoActivity.this);
                File avatarTempDir = new File(avatarTempDirStr);
                if(avatarTempDir != null)
                {
                    // 目录不存在则新建之
                    if(!avatarTempDir.exists())
                        avatarTempDir.mkdirs();
                    // 临时文件名
                    __tempImageFileLocation = avatarTempDir.getAbsolutePath()+"/"+"local_avatar_temp.jpg";
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "【ChangeAvatar】读取本地用户的头像临时存储路径时出错了，" + e.getMessage(), e);
        }

        Log.d(TAG, "【ChangeAvatar】正在获取本地用户的头像临时存储路径：" + __tempImageFileLocation);

        return __tempImageFileLocation;
    }

    /**
     * 处理用户头像的上传.
     *
     * @param avatarTermpImgUri
     */
    private void processAvatarUpload(final Uri avatarTermpImgUri) {

        Log.e(TAG, "处理用户头像的上传.");

        //********************************************************** 【1】压缩头像文件
        // 先将拍好的临时文件decode成bitmap
        Bitmap bmOfTempAvatar = null;
        try {
            bmOfTempAvatar = decodeUriAsBitmap(avatarTermpImgUri);
        }
        // 显示处理下OOM使得APP更健壮（OOM只能显示抓取，否则会按系统Error的方式报出从而使APP崩溃哦）
        catch (OutOfMemoryError e) {
            //WidgetUtils.showToast(parentActivity, parentActivity.getString(R.string.user_info_avatar_upload_faild3), ToastType.WARN);
            Log.e(TAG, "【ChangeAvatar】将头像文件数据decodeUriAsBitmap到内存时内存溢出了，上传没有继续！", e);
        }
        if (bmOfTempAvatar == null) {
            // WidgetUtils.showToast(parentActivity, parentActivity.getString(R.string.user_info_avatar_upload_faild1), ToastType.WARN);
            return;
        }
        //** 再将该bitmap压缩后覆盖原临时文件
        fileOfTempAvatar = new File(getTempImageFileLocation());
        if (bmOfTempAvatar != null) {
            try {
                // # 据测试，微信将640*640的图片裁剪、压缩后的大小约为34K左右，经测试估计是质量75哦
                // # 调整此值可压缩图像大小，经测试，再小于75后，压缩大小就不明显了
                // # 经Jack Jiang在Galaxy sIII上测试：原拍照裁剪完成的60K左右的头像按75压缩后大小约为34K左右，
                //   从高清图片中选取的裁剪完成时的200K左右按75压缩后大小依然约为34K左右，所以75的压缩比率在头
                //   像质量和文件大小上应是一个较好的平衡点
                com.linkloving.taiwan.logic.UI.personal.util.BitmapHelper.saveBitmapToFile(bmOfTempAvatar, 75, fileOfTempAvatar);
                pd = ProgressDialog.show(PersonalInfoActivity.this, null, "正在上传图片，请稍候...");

                new Thread(uploadImageRunnable).start();

                Log.d(TAG, "【ChangeAvatar】尝试压缩本地用户头像临时文件已成功完成.");
            } catch (Exception e) {
                Log.e(TAG, "【ChangeAvatar】要更新的本地用户头像在尝试压缩临时文件时出错了，" + e.getMessage() + "，压缩将不能继续，但不影响继续上传处理！", e);


            }
        }

    }

    String imgUrl=CommParams.APP_ROOT_URL_NEW+"upload/avatar";

    Runnable uploadImageRunnable = new Runnable() {
        @Override
        public void run() {
//            if(TextUtils.isEmpty(imgUrl)){
//                return;
//            }
//            Log.e(TAG, "imgUrl="+imgUrl);
//
//            Map<String, String> textParams = new HashMap<String, String>();
//            Map<String, File> fileparams = new HashMap<String, File>();
//            try {
//                // 创建一个URL对象
//                URL url = new URL(imgUrl);
//                textParams = new HashMap<String, String>();
//                fileparams = new HashMap<String, File>();
//                // 要上传的图片文件
//                textParams.put("user_id",String.valueOf(userEntity.getUser_id()));
//                fileparams.put("file",fileOfTempAvatar);
//                // 利用HttpURLConnection对象从网络中获取网页数据
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                // 设置连接超时（记得设置连接超时,如果网络不好,Android系统在超过默认时间会收回资源中断操作）
//                conn.setConnectTimeout(5000);
//                // 设置允许输出（发送POST请求必须设置允许输出）
//                conn.setDoOutput(true);
//                // 设置使用POST的方式发送
//                conn.setRequestMethod("POST");
//                // 设置不使用缓存（容易出现问题）
//                conn.setUseCaches(false);
//                conn.setRequestProperty("Charset", "UTF-8");//设置编码
//                // 设置contentType
//                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + NetUtil.BOUNDARY);
//                OutputStream os = conn.getOutputStream();
//                DataOutputStream ds = new DataOutputStream(os);
//                NetUtil.writeStringParams(textParams, ds);
//                NetUtil.writeFileParams(fileparams, ds);
//                NetUtil.paramsEnd(ds);
//                // 对文件流操作完,要记得及时关闭
//                os.close();
//                // 服务器返回的响应吗
//                int code = conn.getResponseCode(); // 从Internet获取网页,发送请求,将网页以流的形式读回来
//                // 对响应码进行判断
//                if (code == 200) {// 返回的响应码200,是成功
//                    // 得到网络返回的输入流
//                    InputStream is = conn.getInputStream();
//                    resultStr = NetUtil.readString(is);
//                } else {
//                    Toast.makeText(PersonalInfoActivity.this, "请求URL失败！", Toast.LENGTH_SHORT).show();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            handler.sendEmptyMessage(0);// 执行耗时的方法之后发送消给handler
        }
    };

    Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    pd.dismiss();
                    try {
                        // 返回数据示例，根据需求和后台数据灵活处理
                        Bitmap bitmap = decodeUriAsBitmap(getTempImageFileUri());
                        MyLog.i(TAG, "returnValue" + resultStr);
//                        DataFromServer dataFromServer = JSON.parseObject(resultStr,DataFromServer.class);
//                        JSONObject filename=JSON.parseObject(dataFromServer.getReturnValue().toString());
                        user_head.setImageBitmap(bitmap);
                        //先尝试删除所有自已的以前老的头像缓存（否则占用用户SD卡的空间）
                        AvatarHelper.deleteUserAvatar(PersonalInfoActivity.this, userEntity.getUser_id()+"", null);
                        // 先将临时文件（也就是本次拍好或选好的新头像）复制一份（更名
                        // 为正式的本地用户头像缓存文件名）作为本地用户图像的正式文件
                        try {
                            FileHelper.copyFile(fileOfTempAvatar, new File(fileOfTempAvatar.getParent()+"/"+"myhead"));
                            // 将用户信息的修改实时同步到设备中
                            UserEntity user = MyApplication.getInstance(PersonalInfoActivity.this).getLocalUserInfoProvider();
                            user.getUserBase().setUser_avatar_file_name("myhead");
                            MyApplication.getInstance(PersonalInfoActivity.this).setLocalUserInfoProvider(user);
                            MyLog.i(TAG,"存本地的地址头像"+MyApplication.Const.DIR_KCHAT_AVATART_RELATIVE_DIR+"/"+"myhead");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // 再删除用完的临时文件（也就是本次拍好或选好的新头像），因为
                        // 正式的本地缓存文件已生成，本临时文件就失去意义了，当然删除之
                        FileHelper.deleteFile(getTempImageFileLocation());
                        ToolKits.showCommonTosat(PersonalInfoActivity.this, true, ToolKits.getStringbyId(PersonalInfoActivity.this, R.string.changeinfook), Toast.LENGTH_SHORT);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;

                default:
                    break;
            }
            return false;
        }
    });








}

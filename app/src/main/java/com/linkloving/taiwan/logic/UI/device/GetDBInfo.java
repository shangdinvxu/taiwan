package com.linkloving.taiwan.logic.UI.device;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;


import com.example.android.bluetoothlegatt.proltrol.LPUtil;
import com.linkloving.taiwan.logic.UI.HeartRate.GreendaoUtils;
import com.linkloving.taiwan.utils.logUtils.MyLog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import Trace.GreenDao.heartrate;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Daniel.Xu on 2017/3/13.
 */

public class GetDBInfo {

    public static String getDiskCacheDir(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }


    public  static void getDBInfoToFile(final Context context, final String pathName){
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        final Calendar instance = Calendar.getInstance();
        final String diskCacheDir = getDiskCacheDir(context);
        File file = new File(diskCacheDir+"/"+pathName);
        BufferedWriter bufferedWriter = null ;
        try {
            bufferedWriter =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        GreendaoUtils greendaoUtils = new GreendaoUtils(context);
        List<heartrate> heartrates = greendaoUtils.searchAll();
        final BufferedWriter finalBufferedWriter = bufferedWriter;
        Observable.from(heartrates)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<heartrate>() {
                    @Override
                    public void onCompleted() {
                        MyLog.e("getDBInfo", "onCompleted");
                        try {
                            if (finalBufferedWriter != null) {
                                finalBufferedWriter.flush();
                                finalBufferedWriter.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        toSendEmail(context,diskCacheDir+"/"+pathName);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(heartrate heartrate) {
                        instance.setTimeInMillis((long)heartrate.getStartTime()*1000);
                        String format = simpleDateFormat.format(instance.getTime());
                        try {
                            if (finalBufferedWriter!=null)
                            finalBufferedWriter.write("时间"+format+"-----"+"心率数值   "+heartrate.getMax()+"-------"+heartrate.getAvg()
                                    +"-------"+heartrate.getFakeMaxRate()+"-------"+heartrate.getFakeAvgRate() +"\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public static void writeException(byte[] bytes,Context context){
        String exceptionString = LPUtil.printLogData(bytes, "异常的信息是");
        final String diskCacheDir = getDiskCacheDir(context);
        File file = new File(diskCacheDir+"/"+"HeartRate.txt");
        BufferedWriter bufferedWriter = null ;
        try {
            bufferedWriter =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        final BufferedWriter finalBufferedWriter = bufferedWriter;
        try {
            if (finalBufferedWriter!=null)
            finalBufferedWriter.write(exceptionString);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (finalBufferedWriter != null) {
                try {
                    finalBufferedWriter.flush();
                    finalBufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }


    private  static void toSendEmail(Context context,String diskCacheDir) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("application/octet-stream");
        //设置对方邮件地址
        emailIntent.putExtra(Intent.EXTRA_EMAIL, "daniel.xu@linkloving.com");
        //设置标题内容
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Heartrate");
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + diskCacheDir ));
        context.startActivity(Intent.createChooser(emailIntent, "Choose Email Client"));
    }
}

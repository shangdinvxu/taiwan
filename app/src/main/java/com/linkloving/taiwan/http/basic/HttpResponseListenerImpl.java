package com.linkloving.taiwan.http.basic;

import android.content.Context;

import com.google.gson.Gson;
import com.linkloving.taiwan.http.data.DataFromServer;
import com.yolanda.nohttp.OnResponseListener;
import com.yolanda.nohttp.Response;

/**
 * Created by zkx on 2016/4/1.
 */
public class HttpResponseListenerImpl<T> implements OnResponseListener<T> {

    private WaitDialog mWaitDialog;

    private HttpCallbackImpl<T> callback;

    public HttpResponseListenerImpl(Context context, HttpCallbackImpl<T> httpCallback, boolean showDialog) {
        if (context != null && showDialog)
            mWaitDialog = new WaitDialog(context);
        this.callback = httpCallback;
    }
    @Override
    public void onStart(int what) {
        if (mWaitDialog != null && !mWaitDialog.isShowing())
            mWaitDialog.show();
    }

    @Override
    public void onSucceed(int what, Response<T> response) {
        if (callback != null){
            DataFromServer dataFromServer = new Gson().fromJson((String) response.get(),DataFromServer.class);
            if(dataFromServer.getErrorCode()== 1 ){
//                callback.onSucceed(what, dataFromServer.getReturnValue());
            }else{
                callback.onSucceed(what, response);
            }
        }

        //在这里封装方法
    }

//    @Override
//    public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) {
//        CharSequence message = "";
//        if (exception instanceof ClientError) {// 客户端错误
//            message = "客户端发生错误";
//        } else if (exception instanceof ServerError) {// 服务器错误
//            message = "服务器发生错误";
//        } else if (exception instanceof NetworkError) {// 网络不好
//            message = "请检查网络";
//        } else if (exception instanceof TimeoutError) {// 请求超时
//            message = "请求超时，网络不好或者服务器不稳定";
//        } else if (exception instanceof UnKnownHostError) {// 找不到服务器
//            message = "未发现指定服务器";
//        } else if (exception instanceof URLError) {// URL是错的
//            message = "URL错误";
//        } else {
//            message = "未知错误";
//        }
//        if (callback != null)
//            callback.onFailed(what, url, tag, message, responseCode, networkMillis);
//    }

    @Override
    public void onFailed(int what, String url, Object tag, CharSequence message, int responseCode, long networkMillis) {
        if (callback != null)
            callback.onFailed(what, url, tag, message, responseCode, networkMillis);
    }

    @Override
    public void onFinish(int what) {
        if (mWaitDialog != null && mWaitDialog.isShowing())
            mWaitDialog.dismiss();
    }
}

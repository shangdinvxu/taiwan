package com.linkloving.taiwan.http.basic;

import com.yolanda.nohttp.Response;

/**
 * Created by Administrator on 2016/4/1.
 */
public abstract interface HttpCallbackImpl<T>  {
    public abstract void onSucceed(int what, Response<T> response);

    public abstract void onFailed(int what, String url, Object tag, CharSequence message, int responseCode, long networkMillis);
}

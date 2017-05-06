/*
 * Copyright © YOLANDA. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkloving.taiwan.http.basic;

import android.content.Context;

import com.yolanda.nohttp.OnResponseListener;
import com.yolanda.nohttp.Response;

/**
 * Created in Nov 4, 2015 12:02:55 PM
 * 
 * @author YOLANDA
 */
public class HttpResponseListener<T> implements OnResponseListener<T> {

	private WaitDialog mWaitDialog;

	private HttpCallback<T> callback;

	public HttpResponseListener(Context context, HttpCallback<T> httpCallback,boolean showDialog) {
		if (context != null && showDialog)
			mWaitDialog = new WaitDialog(context);
		this.callback = httpCallback;
	}

	public HttpResponseListener(Context context, HttpCallback<T> httpCallback ,String message) {
		if (context != null )
			mWaitDialog = new WaitDialog(context,message);
		this.callback = httpCallback;
	}

	@Override
	public void onStart(int what) {
		if (mWaitDialog != null && !mWaitDialog.isShowing())
			mWaitDialog.show();
	}

	@Override
	public void onSucceed(int what, Response<T> response) {
		if (callback != null)
			callback.onSucceed(what, response);
		//在这里封装方法
	}

	@Override
	public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) {
		if (callback != null)
			callback.onFailed(what, url, tag, exception.getMessage(), responseCode, networkMillis);
	}


//	@Override
//	public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) {
//		CharSequence message = "";
//		if (exception instanceof ClientError) {// 客户端错误
//			message = "客户端发生错误";
//		} else if (exception instanceof ServerError) {// 服务器错误
//			message = "服务器发生错误";
//		} else if (exception instanceof NetworkError) {// 网络不好
//			message = "请检查网络";
//		} else if (exception instanceof TimeoutError) {// 请求超时
//			message = "请求超时，网络不好或者服务器不稳定";
//		} else if (exception instanceof UnKnownHostError) {// 找不到服务器
//			message = "未发现指定服务器";
//		} else if (exception instanceof URLError) {// URL是错的
//			message = "URL错误";
//		} else {
//			message = "未知错误";
//		}
//		if (callback != null)
//			callback.onFailed(what, url, tag, message, responseCode, networkMillis);
//	}


//	@Override
//	public void onFailed(int what, String url, Object tag, Exception exception, CharSequence message, int responseCode, long networkMillis) {
//		if (exception instanceof ClientError) {// 客户端错误
//			Toast.show("客户端发生错误");
//		} else if (exception instanceof ServerError) {// 服务器错误
//			Toast.show("服务器发生错误");
//		} else if (exception instanceof NetworkError) {// 网络不好
//			Toast.show("请检查网络");
//		} else if (exception instanceof TimeoutError) {// 请求超时
//			Toast.show("请求超时，网络不好或者服务器不稳定");
//		} else if (exception instanceof UnKnownHostError) {// 找不到服务器
//			Toast.show("未发现指定服务器");
//		} else if (exception instanceof URLError) {// URL是错的
//			Toast.show("URL错误");
//		} else {
//			Toast.show("未知错误");
//		}
//		if (callback != null)
//			callback.onFailed(what, url, tag, message, responseCode, networkMillis);
//	}

	@Override
	public void onFinish(int what) {
		if (mWaitDialog != null && mWaitDialog.isShowing())
			mWaitDialog.dismiss();
	}

}

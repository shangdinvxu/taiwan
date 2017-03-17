package com.linkloving.taiwan.logic.UI.customerservice.serviceItem;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.linkloving.taiwan.R;
import com.linkloving.taiwan.http.HttpHelper;
import com.linkloving.taiwan.utils.logUtils.MyLog;


/**
 * Created by Linkloving on 2016/3/24.
 */
public class FQAPage extends Fragment{
     WebView webView;
   String  url;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        url= HttpHelper.creatTermUrl(getActivity(),1);
        return inflateAndSetupView(inflater, container, savedInstanceState, R.layout.faq_web_view);
    }
    private View inflateAndSetupView(LayoutInflater inflater, ViewGroup container,
                                     Bundle savedInstanceState, int layoutResourceId) {
        View layout = inflater.inflate(layoutResourceId, container, false);
        webView = (WebView) layout.findViewById(R.id.view_web_webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setSaveFormData(false);
        webView.getSettings().setLoadsImagesAutomatically(true);
        return layout;
    }
    private class mWebViewClient extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {

            view.loadUrl(url);
            return true;
        }
    }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            //只有当该Fragment被用户可见的时候,才加载网络数据
            //加载网页
            webView.setWebViewClient(new mWebViewClient());
            MyLog.i("加载常见问题的url"+url);
            webView.loadUrl(url);
        }else{
            //否则不加载网络数据

        }

    };
}

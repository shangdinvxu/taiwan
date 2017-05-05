package com.linkloving.taiwan.RetrofitUtils;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Daniel.Xu on 2016/12/15.
 */

public class RetrofitClient {
    private static Retrofit retrofitClient;

    private RetrofitClient(String baseUrl) {
        retrofitClient = new Retrofit.Builder()
                //设置OKHttpClient
                .client(new OKHttpFactory().getOkHttpClient())
                //baseUrl
                .baseUrl(baseUrl)
                //gson转化器
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static Retrofit getInstance(String baseUrl){
        if (retrofitClient ==null){
           new RetrofitClient(baseUrl);
        }
            return retrofitClient ;
    }
}

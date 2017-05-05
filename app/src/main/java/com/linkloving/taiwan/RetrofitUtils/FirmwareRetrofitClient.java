package com.linkloving.taiwan.RetrofitUtils;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Daniel.Xu on 2016/12/15.
 */

public class FirmwareRetrofitClient {
    private static Retrofit retrofitClient;

    private FirmwareRetrofitClient() {
        retrofitClient = new Retrofit.Builder()
                //设置OKHttpClient
                .client(new OKHttpFactory().getOkHttpClient())

                //baseUrl
                .baseUrl("http://www.robotime.com/")
                //gson转化器
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static Retrofit getInstance(){
        if (retrofitClient ==null){
           new FirmwareRetrofitClient();
        }
            return retrofitClient ;
    }
}

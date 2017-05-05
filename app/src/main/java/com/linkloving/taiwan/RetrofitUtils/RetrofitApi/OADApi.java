package com.linkloving.taiwan.RetrofitUtils.RetrofitApi;


import com.linkloving.taiwan.RetrofitUtils.Bean.CheckFirmwareVersionReponse;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * Created by Daniel.Xu on 2017/2/19.
 */

public interface OADApi {
    @POST("Firmware/checkFirmwareVersion")
    Call<CheckFirmwareVersionReponse> checkFirmwareVersion(@Body HashMap hashMap);

    @GET("download_file")
    Call<ResponseBody> download_file(@QueryMap HashMap<String, Object> hashMap);

    @POST("check")
    Call<CheckFirmwareVersionReponse> check(@Body HashMap<String, Object> hashMap);

}

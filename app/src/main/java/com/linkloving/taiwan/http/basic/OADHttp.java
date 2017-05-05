package com.linkloving.taiwan.http.basic;

import com.alibaba.fastjson.JSON;
import com.yolanda.nohttp.Request;

import java.util.HashMap;

/**
 * Created by Daniel.Xu on 2017/5/5.
 */

public class OADHttp {
    //OAD最新版本信息获取

    public final static String checkFirmwareVersion = "http://www.robotime.com/check/";
    public static Request<String> creat_New_OAD_Request(String modelName, int version_int) {
        MyJsonRequest httpsRequest = new MyJsonRequest(checkFirmwareVersion);
        HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("model_name",modelName);
        objectObjectHashMap.put("version_code",version_int);
        httpsRequest.setRequestBody(JSON.toJSONString(objectObjectHashMap));
        return httpsRequest;
    }
}

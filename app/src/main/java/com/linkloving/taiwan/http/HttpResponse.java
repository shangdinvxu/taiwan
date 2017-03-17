package com.linkloving.taiwan.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.linkloving.taiwan.utils.CommonUtils;
import com.linkloving.taiwan.utils.ToolKits;

/**
 * Created by Administrator on 2016/3/22.
 */
/** @deprecated */
public class HttpResponse {
    public  static  boolean praseHttpResponseOK(String response){
        JSONObject object=JSON.parseObject(response);
        boolean success = object.getBoolean("success");
//        if(success){
//            String value=object.getString("returnValue");
//            JSONObject object= JSON.parseObject(response);
//            String value=object.getString("returnValue");
//            return success;
//        }
        return success;

    }
    public  static  String praseHttpResponseStr(String response){
        JSONObject object=JSON.parseObject(response);
        String value=object.getString("returnValue");
        if(!CommonUtils.isStringEmptyPrefer(value) && value instanceof String && !ToolKits.isJSONNullObj(value)){
            value = "";
        }
        return value;

    }
}

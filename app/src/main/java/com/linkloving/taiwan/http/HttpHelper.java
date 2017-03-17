package com.linkloving.taiwan.http;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.linkloving.taiwan.CommParams;
import com.linkloving.taiwan.MyApplication;
import com.linkloving.taiwan.http.data.ActionConst;
import com.linkloving.taiwan.http.data.DataFromClientNew;
import com.linkloving.taiwan.http.data.JobDispatchConst;
import com.linkloving.taiwan.logic.dto.SportRecordUploadDTO;
import com.linkloving.taiwan.utils.LanguageHelper;
import com.linkloving.taiwan.utils.logUtils.MyLog;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.Request;

import java.util.HashMap;
import java.util.Map;

 /**
 * Created by Administrator on 2016/3/22.
 */
public class HttpHelper {

    /**
     * 获取服务器最新的UserEntity接口参数
     */
    public static Request<String> createUserEntityRequest(String userId) {
        Map<String, String> newData = new HashMap<String, String>();
        newData.put("user_id", userId);
//        Request<String> request= NoHttp.createStringRequestPost(CommParams.SERVER_CONTROLLER_URL);
//        JSONObject obj = new JSONObject();
//        DataFromClient dataFromClient = new DataFromClient();
//        dataFromClient.setProcessorId(1008);
//        dataFromClient.setJobDispatchId(1);
//        dataFromClient.setActionId(3);
//        dataFromClient.setNewData(JSON.toJSONString(newData));
//        request.setRequestBody(JSON.toJSONString(dataFromClient).getBytes());
//        return request;
        Request<String> request= NoHttp.createStringRequestPost(CommParams.SERVER_CONTROLLER_URL_NEW);
        DataFromClientNew dataFromClientNew = new DataFromClientNew();
        dataFromClientNew.setActionId(ActionConst.ACTION_6);
        dataFromClientNew.setJobDispatchId(JobDispatchConst.LOGIC_REGISTER);
        dataFromClientNew.setData(JSON.toJSONString(newData));
        request.setRequestBody(JSON.toJSONString(dataFromClientNew).getBytes());
        return request;
    }
    /**
     * 提交卡号到服务端接口参数
     */
    public static Request<String> createUpCardNumberRequest(String userId, String cardnumber) {
        Map<String, String> newData = new HashMap<String, String>();
        newData.put("user_id", userId);
        newData.put("card_number", cardnumber);
//        Request<String> request= NoHttp.createStringRequestPost(CommParams.SERVER_CONTROLLER_URL);
//        DataFromClient dataFromClient = new DataFromClient();
//        dataFromClient.setProcessorId(1008);
//        dataFromClient.setJobDispatchId(1);
//        dataFromClient.setActionId(4);
//        dataFromClient.setNewData(JSON.toJSONString(newData));
//        request.setRequestBody(JSON.toJSONString(dataFromClient).getBytes());
//        return request;

        Request<String> request= NoHttp.createStringRequestPost(CommParams.SERVER_CONTROLLER_URL_NEW);
        DataFromClientNew dataFromClientNew = new DataFromClientNew();
        dataFromClientNew.setActionId(ActionConst.ACTION_7);
        dataFromClientNew.setJobDispatchId(JobDispatchConst.LOGIC_REGISTER);
        dataFromClientNew.setData(JSON.toJSONString(newData));
        request.setRequestBody(JSON.toJSONString(dataFromClientNew).getBytes());
        return request;
    }

    /**
     * 提交deviceId到服务端接口参数
     */
    public static Request<String> createUpDeviceIdRequest(String user_id, String last_sync_device_id,String last_sync_device_id2) {

        Map<String, String> newData = new HashMap<String, String>();
        newData.put("user_id", user_id);
        newData.put("last_sync_device_id", last_sync_device_id);
        newData.put("last_sync_device_id2", last_sync_device_id2);

        Request<String> request= NoHttp.createStringRequestPost(CommParams.SERVER_CONTROLLER_URL_NEW);
        DataFromClientNew dataFromClientNew = new DataFromClientNew();
        dataFromClientNew.setActionId(ActionConst.ACTION_8);
        dataFromClientNew.setJobDispatchId(JobDispatchConst.LOGIC_REGISTER);
        dataFromClientNew.setData(JSON.toJSONString(newData));
        request.setRequestBody(JSON.toJSONString(dataFromClientNew).getBytes());
        return request;
    }

//    /**
//     *  提交运动数据到服务器
//     */
//    public static Request<String> sportDataSubmitServer(String userId,List<SportRecord>upList) {
//        Map<String, String> newData = new HashMap<String, String>();
//        newData.put("user_id", userId);
//        newData.put("device_id", 1 + "");
//        newData.put("utc_time", 1 + "");
//        newData.put("list", JSON.toJSONString(upList));
//        Request<String> request= NoHttp.createStringRequestPost(CommParams.SERVER_CONTROLLER_URL);
//        DataFromClient dataFromClient = new DataFromClient();
//        dataFromClient.setProcessorId(1010);
//        dataFromClient.setJobDispatchId(11);
//        dataFromClient.setActionId(1);
//        dataFromClient.setNewData(JSON.toJSONString(newData));
//        request.setRequestBody(JSON.toJSONString(dataFromClient).getBytes());
//        return request;
//    }

     /**
      *  提交运动数据到服务器
      */
     public static Request<String> sportDataSubmitServer(SportRecordUploadDTO sportRecordUploadDTO) {
         Request<String> request= NoHttp.createStringRequestPost(CommParams.SERVER_CONTROLLER_URL_NEW);
         DataFromClientNew dataFromClient = new DataFromClientNew();
         dataFromClient.setActionId(ActionConst.ACTION_101);
         dataFromClient.setJobDispatchId(JobDispatchConst.REPORT_BASE);
         dataFromClient.setData(JSON.toJSONString(sportRecordUploadDTO));
         request.setRequestBody(JSON.toJSONString(dataFromClient).getBytes());
         return request;
     }

     /**
      *  提交运动数据到服务器 JSON
      */
     public static String sportDataSubmitServerJSON(SportRecordUploadDTO sportRecordUploadDTO) {
         Request<String> request= NoHttp.createStringRequestPost(CommParams.SERVER_CONTROLLER_URL_NEW);
         DataFromClientNew dataFromClient = new DataFromClientNew();
         dataFromClient.setActionId(ActionConst.ACTION_101);
         dataFromClient.setJobDispatchId(JobDispatchConst.REPORT_BASE);
         dataFromClient.setData(JSON.toJSONString(sportRecordUploadDTO));
         return JSON.toJSONString(dataFromClient);
     }

    /**
     * 生成云同步
     */
    public static DataFromClientNew GenerateCloudSyncParams(String userId,int pageIndex) {
        Map<String, String> newData = new HashMap<String, String>();
        newData.put("user_id", userId);
        newData.put("pageNum", pageIndex+"");
        DataFromClientNew dataFromClientNew = new DataFromClientNew();
        dataFromClientNew.setActionId(ActionConst.ACTION_100);
        dataFromClientNew.setJobDispatchId(JobDispatchConst.REPORT_BASE);
        dataFromClientNew.setData(JSON.toJSONString(newData));

        return dataFromClientNew;
    }
    /**
     * 生成解绑请求
     */
     /** @deprecated */
    public static Request<String> creatUnbundRequest(String userId) {
        Map<String, String> newData = new HashMap<String, String>();
        newData.put("user_id", userId);
        Request<String> request= NoHttp.createStringRequestPost(CommParams.SERVER_CONTROLLER_URL);
        DataFromClient dataFromClient = new DataFromClient();
        dataFromClient.setProcessorId(1008);
        dataFromClient.setJobDispatchId(1);
        dataFromClient.setActionId(25);
        dataFromClient.setNewData(JSON.toJSONString(newData));
        request.setRequestBody(JSON.toJSONString(dataFromClient).getBytes());
        return request;
    }

     /**
      * 生成常见问题等请求
      */
     public static String creatTermUrl(Context context,int type) {
         String termUrl="";
//         if(MyApplication.getInstance(context).getLocalUserInfoProvider().getDeviceEntity().getDevice_type() > 0 ){
//             termUrl = "https://www.linkloving.com/linkloving_server_2.0/web/service?" +
//                     "page="+type+
//                     "&device="+MyApplication.getInstance(context).getLocalUserInfoProvider().getDeviceEntity().getDevice_type()+"&app=1&chan=1&" +
//                     "isEn="+!LanguageHelper.isChinese_SimplifiedChinese();
//             MyLog.e("faq",termUrl);
//
//         }else{
//             termUrl = "https://linkloving.com/linkloving_server_2.0/web/service?" +
//                     "page="+type+
//                     "&device="+1+"&app=1&chan=1&" +
//                     "isEn="+!LanguageHelper.isChinese_SimplifiedChinese();
//         }
         switch (type){
             case 2:
                termUrl ="https://www.linkloving.com/linkloving_server_2.0/web/service?app=2&page=2&device=0&chan=0";
                 break;
             case 3:
                 termUrl ="https://www.linkloving.com/linkloving_server_2.0/web/service?app=2&page=3&device=0&chan=0";
                 break;
         }
         return termUrl;
     }
}

package com.linkloving.taiwan.logic.UI.main.materialmenu;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linkloving.taiwan.IntentFactory;
import com.linkloving.taiwan.MyApplication;
import com.linkloving.taiwan.R;
import com.linkloving.taiwan.logic.UI.customerservice.serviceItem.Feedback;
import com.linkloving.taiwan.logic.dto.UserEntity;
import com.linkloving.taiwan.utils.CommonUtils;
import com.linkloving.taiwan.utils.ToolKits;

import java.util.List;

/**
 * Created by zkx on 2016/3/11.
 */
public class MenuNewAdapter extends RecyclerView.Adapter {

//    private int pageIndex = 1;
//    private  ProgressDialog progressDialog;
//    private  int progressindex;

    private static final String TAG = MenuNewAdapter.class.getSimpleName();
    public  static int JUMP_FRIEND_TAG=1;
    UserEntity userEntity;
    private Context mContext;
    private OnRecyclerViewListener onRecyclerViewListener;
    public void setOnRecyclerViewListener(OnRecyclerViewListener onRecyclerViewListener) {
        this.onRecyclerViewListener = onRecyclerViewListener;
    }

    public static interface OnRecyclerViewListener {
        void onItemClick(int position);
    }
    private List<MenuVO> list;

    public MenuNewAdapter(Context context, List<MenuVO> list) {
        this.mContext = context;
        this.list = list;
//        userEntity = MyApplication.getInstance(mContext).getLocalUserInfoProvider();
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_menu, null);
//        不知道为什么在xml设置的“android:layout_width="match_parent"”无效了，需要在这里重新设置
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        MenuViewHolder holder = (MenuViewHolder) viewHolder;
        MenuVO menuVO = list.get(position);
        holder.itemImg.setBackgroundResource(list.get(position).getImgID());
        holder.itemName.setText(list.get(position).getTextID());
        holder.itemName.setTextColor(Color.WHITE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    //还可以添加长点击事件
    class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public LinearLayout layout;
        public TextView itemName;
        public ImageView itemImg;
        public TextView unread;

        public MenuViewHolder(View convertView) {
            super(convertView);
            itemImg = (ImageView) convertView.findViewById(R.id.fragment_item_img);
            itemName = (TextView) convertView.findViewById(R.id.fragment_item_text);
            unread = (TextView) convertView.findViewById(R.id.fragment_item_unread_text);
            layout = (LinearLayout) convertView.findViewById(R.id.Layout);
            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (null != onRecyclerViewListener) {
                onRecyclerViewListener.onItemClick(this.getPosition());
                switch (list.get(this.getPosition()).getOrderId()){

//                    case Left_viewVO.FRIENDS:
//                        IntentFactory.start_FriendActivity((Activity) mContext,JUMP_FRIEND_TAG);
//                        break;
//
//                    case Left_viewVO.RANKING:
//                        IntentFactory.start_RankingActivityIntent((Activity) mContext);
//                        break;
                    case 0:
                        IntentFactory.startHeartRateActivity((Activity) mContext);
                        break;
                    case 1:
                        IntentFactory.startGoalActivityIntent((Activity) mContext, MyApplication.getInstance((Activity) mContext).getLocalUserInfoProvider());
                        break;

                    case 2:
                        IntentFactory.start_CustomerService_ActivityIntent((Activity) mContext, Feedback.PAGE_INDEX_ONE);
                        break;

                    case 3:
                        userEntity = MyApplication.getInstance(mContext).getLocalUserInfoProvider();
                        String s = userEntity.getDeviceEntity().getLast_sync_device_id();
                        if (CommonUtils.isStringEmpty(s)) {
                            AlertDialog dialog = new AlertDialog.Builder(mContext)
                                    .setTitle(ToolKits.getStringbyId(mContext, R.string.portal_main_unbound))
                                    .setMessage(ToolKits.getStringbyId(mContext, R.string.portal_main_unbound_msg))
                                    .setPositiveButton(ToolKits.getStringbyId(mContext, R.string.general_ok),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            }).create();
                            dialog.show();
                        }else {
                            mContext.startActivity(IntentFactory.start_AlarmActivityIntent((Activity) mContext));
                        }
                        break;

                    case 4:
                        /*AlertDialog dialog = new AlertDialog.Builder(mContext)
                                .setTitle(ToolKits.getStringbyId(mContext, R.string.main_more_sycn_title))
                                .setMessage(ToolKits.getStringbyId(mContext, R.string.main_more_sycn_message))
                                .setPositiveButton(ToolKits.getStringbyId(mContext, R.string.general_yes),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                progressDialog = initDialog();
                                                progressDialog.show();
                                                DataFromClient dataFromClient = HttpHelper.GenerateCloudSyncParams(userEntity.getUser_id(),pageIndex);
                                                try {
                                                    HttpUtils.doPostAsyn(CommParams.SERVER_CONTROLLER_URL, JSON.toJSONString(dataFromClient), httpcallback);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        })
                                .setNegativeButton(ToolKits.getStringbyId(mContext, R.string.general_no), null)
                                .create();
                        dialog.show();*/
//        跳转到更多界面
                        mContext.startActivity(IntentFactory.start_MoreActivityIntent((Activity) mContext));
                        break;
                    default :
                }
            }
        }

//        private ProgressDialog initDialog() {
//            //this表示该对话框是针对当前Activity的
//            ProgressDialog progressDialog = new ProgressDialog(mContext);
//            //设置最大值为100
//            progressDialog.setMax(100);
//            //设置进度条风格STYLE_HORIZONTAL
//            progressDialog.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL);
//            progressDialog.setTitle("云同步...");
//            progressDialog.setCancelable(false);
//            progressDialog.setCanceledOnTouchOutside(false);
//            return progressDialog;
//
//        }
//        private HttpUtils.CallBack httpcallback = new HttpUtils.CallBack() {
//            @Override
//            public void onRequestComplete(String result) {
//                JSONObject object=JSON.parseObject(result);
//                String returnValue=object.getString("returnValue");
//                if(!CommonUtils.isStringEmpty(returnValue)){
//                    Map<String, String> data_result = new Gson().fromJson(returnValue, new TypeToken<HashMap<String, String>>(){}.getType());
//                    String datas = data_result.get("datas");
//                    //云同步数据总量
//                    int count = Integer.parseInt(data_result.get("datas_total_count").isEmpty()?"0":data_result.get("datas_total_count"));
//                    List<SportRecord> srs = JSON.parseArray((String)datas, SportRecord.class);
//                    MyLog.e("MenuAdapter","srs："+srs.size());
//                    progressindex += srs.size();
//                    progressDialog.setMax(count);
//                    progressDialog.setProgress(progressindex);
//                    if(srs.size() > 0)
//                    {
////                        List<SportRecord> upList_new =  SportDataHelper.backStauffSleepState(mContext,userEntity.getUser_id(),srs);
//                        UserDeviceRecord.saveToSqliteAsync(mContext, srs, userEntity.getUser_id()+"", true, null);
//                        new AsyncTask<Object, Object, Boolean>(){
//                            @Override
//                            protected Boolean doInBackground(Object... params)
//                            {
//                                List<DaySynopic> srsOffline=com.linkloving.rtring_c_watch.utils.sportUtils.SportDataHelper.creatDaySynopiclist((ArrayList<SportRecord>) params[0]);
//                                DaySynopicTable.saveToSqlite(mContext, srsOffline, userEntity.getUser_id()+"");
//                                return null;
//                            }
//                            @Override
//                            protected void onPostExecute(Boolean result)
//                            {
//
//                                MyLog.i(TAG,"汇总数据完成了");
//
//                            }
//                        }.execute(srs);
//                        //更新百分比
////                      updateProgess(srs.size(),count,progessWidget);
//                        pageIndex++;
//                        DataFromClientNew dataFromClient = HttpHelper.GenerateCloudSyncParams(userEntity.getUser_id()+"",pageIndex);
//                        try {
//                            HttpUtils.doPostAsyn(CommParams.SERVER_CONTROLLER_URL_NEW, JSON.toJSONString(dataFromClient), httpcallback);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    else
//                    {
//                        //云同步完成
//                        MyLog.e("MenuAdapter", "云同步完成");
//                        pageIndex = 1;
//                        progressDialog.dismiss();
//                    }
//
//                }
//            }
//        };


    }
}

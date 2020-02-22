package com.clc.learnplatform.pager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.clc.learnplatform.R;
import com.clc.learnplatform.activity.CtqhActivity;
import com.clc.learnplatform.activity.ItemBankStudyActivity;
import com.clc.learnplatform.activity.MnksActivity;
import com.clc.learnplatform.activity.SerchAnswerActivity;
import com.clc.learnplatform.activity.StudiedActivity;
import com.clc.learnplatform.activity.WeiZuoTiActivity;
import com.clc.learnplatform.dialog.MyAchievementDialog;
import com.clc.learnplatform.dialog.WeiZuoTiDialog;
import com.clc.learnplatform.entity.KSXM_Entity;
import com.clc.learnplatform.entity.UserInfoEntity;
import com.clc.learnplatform.entity.WDCJ_Entity;
import com.clc.learnplatform.entity.XMFL_Entity;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.SPUtils;
import com.clc.learnplatform.util.TimeUtil;
import com.clc.learnplatform.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 理论知识
 */
public class TheoryStudiedPager implements View.OnClickListener {
    private static final String TAG = "TheoryStudiedPager";
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private boolean run = true;

    private Activity mActivity;
    private View mView;

    private GridView mKnowledge;
    private GridViewAdapter mAdapter;

    private RelativeLayout mMnks;//模拟考试
    private TextView mMnksCoin;//模拟考试消耗金币数
    private ImageView mCtlx;//错题练习
    private ImageView mMyAchievement;//我的成绩
    private ImageView mWeiZuoTi;//未做题练习
    private ImageView mSearchAnswer;//搜答案

    private KSXM_Entity mKSXM;//当前项目类
    private ArrayList<XMFL_Entity> mXmflList;//项目分类列表 例如基础知识 专业知识等
    private UserInfoEntity mUserInfoEntiry;//用户类
    private String xmid;//项目id
    private WDCJ_Entity mWdcj;//我的成绩

    private AlertDialog alertDialog;

    private int mLXL;//练题率

    public TheoryStudiedPager(Activity activity,int lxl, UserInfoEntity userInfoEntity, String xmid, WDCJ_Entity wdcj_entity, KSXM_Entity ksxm) {
        mActivity = activity;
        mUserInfoEntiry = userInfoEntity;
        this.mLXL = lxl;
        mKSXM = ksxm;
        this.xmid = xmid;
        mWdcj = wdcj_entity;
        // TODO 动态添加布局(xml方式)
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);       //LayoutInflater inflater1=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //      LayoutInflater inflater2 = getLayoutInflater();
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        mView = inflater.inflate(R.layout.fragment_studied_theory, null);
        mView.setLayoutParams(lp);

        initView();

        alertDialog = new AlertDialog
                .Builder(mActivity).setMessage("请等待...")
                .create();
    }

    private void initView() {
        mKnowledge = mView.findViewById(R.id.grid_knowledge);
        mMnks = mView.findViewById(R.id.rl_mnks);
        mMnks.setOnClickListener(this);
        mMnksCoin = mView.findViewById(R.id.tv_mnks_coin_num);
        mCtlx = mView.findViewById(R.id.iv_button_ctqh);
        mCtlx.setOnClickListener(this);
        mMyAchievement = mView.findViewById(R.id.iv_button_wdcj);
        mMyAchievement.setOnClickListener(this);
        mWeiZuoTi = mView.findViewById(R.id.iv_button_wztlx);
        mWeiZuoTi.setOnClickListener(this);
        mSearchAnswer = mView.findViewById(R.id.iv_button_sda);
        mSearchAnswer.setOnClickListener(this);

    }

    public void initData(ArrayList<XMFL_Entity> list) {
        mMnksCoin.setText(String.valueOf(mKSXM.MNXH));
        this.mXmflList = list;
        mAdapter = new GridViewAdapter(mActivity);
        mKnowledge.setAdapter(mAdapter);
        //开启一个线程专门更新时间
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (run) {
                    Message msg = new Message();
                    msg.what = 0x05;
                    ((StudiedActivity) mActivity).mHandler.sendMessage(msg);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        run = false;
                    }
                }
            }
        }).start();

    }

    //更新时间
    public void updataTime() {
        mAdapter.notifyDataSetChanged();
    }


    /**
     * 将更新时间子线程杀死
     */
    public void cleanView() {
        run = false;
    }


    public View getmView() {
        run = true;
        return mView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_mnks://模拟考试
                //判读学习币够不够
                if(mUserInfoEntiry.ZHYE<mKSXM.MNXH){
                    ToastUtil.getInstance().shortShow("学习币不足，请充值后再试");
                }else {
                    new AlertDialog
                            .Builder(mActivity).setTitle("")
                            .setMessage("确定要消耗学习币进行模拟考试吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //ToDo: 你想做的事情
                                    //改变金币值
                                    mUserInfoEntiry.ZHYE = mUserInfoEntiry.ZHYE - mKSXM.MNXH;
                                    ((StudiedActivity) mActivity).changeCoin(mUserInfoEntiry.ZHYE);
                                    //进入模拟考试
                                    Intent intent = new Intent();
                                    intent.putExtra("openid", mUserInfoEntiry.WXCODE);
                                    intent.putExtra("xmid", xmid);
                                    intent.setClass(mActivity, MnksActivity.class);
                                    mActivity.startActivity(intent);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //ToDo: 你想做的事情
                                    dialogInterface.dismiss();
                                }
                            })
                            .create()
                            .show();
                }
                break;
            case R.id.iv_button_ctqh://错题强化
                Intent intent2 = new Intent();
                intent2.putExtra("openid", mUserInfoEntiry.WXCODE);
                intent2.putExtra("xmid", xmid);
                intent2.setClass(mActivity, CtqhActivity.class);
                mActivity.startActivity(intent2);
                break;
            case R.id.iv_button_wdcj://我的成绩
                //定义一个自己的dialog
                MyAchievementDialog myDialog1new =
                        new MyAchievementDialog(mActivity, mWdcj);
                //实例化自定义的dialog
                myDialog1new.show();
                break;
            case R.id.iv_button_wztlx://未做题练习
                alertDialog.show();
                doWeiZuoTiLianXi();
                break;
            case R.id.iv_button_sda://搜答案
                new AlertDialog
                        .Builder(mActivity).setTitle("")
                        .setMessage("答案搜索消耗" + mKSXM.STXH + "个学习币/次，确定继续吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //ToDo: 你想做的事情
                                //进入搜答案
                                Intent intent3 = new Intent();
                                intent3.putExtra("openid", mUserInfoEntiry.WXCODE);
                                intent3.putExtra("xmid", xmid);
                                intent3.setClass(mActivity, SerchAnswerActivity.class);
                                mActivity.startActivityForResult(intent3, 100);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //ToDo: 你想做的事情
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();


                break;
        }
    }

    //进入未做题练习
    private void doWeiZuoTiLianXi() {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(100, TimeUnit.SECONDS)//设置读取超时时间
                .build();

        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(mUserInfoEntiry.WXCODE)
                .append("&xmid=")
                .append(xmid);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.WZTLX_URL)//模拟考试接口
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                alertDialog.dismiss();
                Message msg = new Message();
                msg.what = 0x04;
                ((StudiedActivity) mActivity).mHandler.sendMessage(msg);
                Log.i(TAG, "onFailure: 获取未做题练习失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                alertDialog.dismiss();
                String responseInfo = response.body().string();
                Log.i(TAG, "onResponse: responseInfo===" + responseInfo);
                String error = null;
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//失败
                        String message = jsonObject.getString("message");
                        Message msg = new Message();
                        msg.what = 0x03;
                        ((StudiedActivity) mActivity).mHandler.sendMessage(msg);
                        Log.i(TAG, "onResponse: message===" + message);
                    } else if (error.equals("false")) {//成功
                        Intent intent = new Intent();
                        intent.putExtra("openid", mUserInfoEntiry.WXCODE);
                        intent.putExtra("xmid", xmid);
                        intent.putExtra("responseInfo", responseInfo);
                        intent.setClass(mActivity, WeiZuoTiActivity.class);
                        mActivity.startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public class GridViewAdapter extends BaseAdapter {
        private Activity mActivity;

        public GridViewAdapter(Activity activity) {
            mActivity = activity;
        }

        @Override
        public int getCount() {
            return mXmflList.size();
        }

        @Override
        public Object getItem(int position) {
            return mXmflList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(mActivity, R.layout.gridview_item, null);
                holder = new ViewHolder();
                holder.linerLayout = convertView.findViewById(R.id.ll_grid_root);
                holder.tvItemName = convertView.findViewById(R.id.tv_item_name);
                holder.tvMsg = convertView.findViewById(R.id.tv_coin);
                holder.tvCoin = convertView.findViewById(R.id.tv_coin_num);
                holder.ivCoin = convertView.findViewById(R.id.iv_coin);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvItemName.setText(mXmflList.get(position).FLNC);
            //判断是否已消耗金币进行观看
            long time = new Date().getTime();
            //用项目id与分类名称作为sp的key
            final String key = mXmflList.get(position).XMID + mXmflList.get(position).FLNC;
            final long sur_time = (long) SPUtils.get(mActivity, key, 0L);
            long l = time - sur_time;//从点击到现在的毫秒值
            long ll = l / 1000;//转换成秒
            int lll = (int) (mXmflList.get(position).XSFZ * 60 - ll);//还剩余的秒值
            if (0 >= lll) {
                holder.tvMsg.setText("查阅消耗");
                holder.tvCoin.setText(mXmflList.get(position).ZZDXH + "");
                holder.ivCoin.setVisibility(View.VISIBLE);
            } else {
                holder.tvMsg.setText("剩余时间");
                holder.tvCoin.setText(TimeUtil.getTimeString(lll));
                holder.ivCoin.setVisibility(View.GONE);
            }
            holder.linerLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //判断是否已经购买过 并且没有过期
                    long now_time = new Date().getTime();
                    long sur_time1 = (long) SPUtils.get(mActivity, key, 0L);
                    long l = (now_time - sur_time1) / 1000;
                    int ll = (int) (mXmflList.get(position).XSFZ * 60 - l);
                    if (ll > 0) {//已经购买过 直接进去
                        //进入题库学习页面
                        Intent intent = new Intent();
                        intent.putExtra("openid", mUserInfoEntiry.WXCODE);
                        intent.putExtra("flid", mXmflList.get(position).ID);
                        intent.setClass(mActivity, ItemBankStudyActivity.class);
                        mActivity.startActivity(intent);
                    } else {
                        if (mUserInfoEntiry.ZHYE >= mXmflList.get(position).ZZDXH) {
                            new AlertDialog
                                    .Builder(mActivity).setTitle("")
                                    .setMessage("确定要消耗学习币进行学习吗？")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //ToDo: 你想做的事情
                                            mUserInfoEntiry.ZHYE = mUserInfoEntiry.ZHYE - mXmflList.get(position).ZZDXH;
                                            ((StudiedActivity) mActivity).changeCoin(mUserInfoEntiry.ZHYE);
                                            long time = new Date().getTime();
                                            SPUtils.put(mActivity, key, time);
                                            //进入题库学习页面
                                            Intent intent = new Intent();
                                            intent.putExtra("openid", mUserInfoEntiry.WXCODE);
                                            intent.putExtra("flid", mXmflList.get(position).ID);
                                            intent.setClass(mActivity, ItemBankStudyActivity.class);
                                            mActivity.startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //ToDo: 你想做的事情
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .create()
                                    .show();
                        }else {
                            ToastUtil.getInstance().shortShow("您的学习不足，请充值后再试");
                        }
                    }
                }
            });
            return convertView;
        }

        class ViewHolder {
            LinearLayout linerLayout;
            TextView tvItemName;
            TextView tvMsg;
            TextView tvCoin;
            ImageView ivCoin;
        }

    }

}

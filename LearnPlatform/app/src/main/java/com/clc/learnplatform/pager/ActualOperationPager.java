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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.clc.learnplatform.R;
import com.clc.learnplatform.activity.ActualActivity;
import com.clc.learnplatform.activity.StudiedActivity;
import com.clc.learnplatform.entity.KSXM_Entity;
import com.clc.learnplatform.entity.SJCZ_Entity;
import com.clc.learnplatform.util.SPUtils;
import com.clc.learnplatform.util.TimeUtil;
import com.clc.learnplatform.util.ToastUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Handler;

/**
 * 实际操作（在项目学习里边）
 */
public class ActualOperationPager {
    private static final String TAG = "ActualOperationPager";
    private Activity mActivity;
    private View mView;

    private TextView mTishi;//暂无相关信息提示
    private ListView mListView;
    private ActualItemAdapter mAdapter;

    private boolean run = true;

    private KSXM_Entity mKSXM;//当前项目类
    private int mCurr_Coin;//当前金币数量
    private ArrayList<SJCZ_Entity> mSjczList;//实际操作list
    private boolean isBindingCard;//标示是否有绑定学习卡

    public ActualOperationPager(Activity activity,KSXM_Entity ksxm_entity, int coin,boolean bindCard) {
        this.mCurr_Coin = coin;
        mActivity = activity;
        this.mKSXM = ksxm_entity;
        this.isBindingCard = bindCard;
        // TODO 动态添加布局(xml方式)
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);       //LayoutInflater inflater1=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //      LayoutInflater inflater2 = getLayoutInflater();
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        mView = inflater.inflate(R.layout.fragment_studied_actual, null);
        mView.setLayoutParams(lp);

        initView();
    }

    private void initView() {
        mTishi = mView.findViewById(R.id.tv_tishi);
        mListView = mView.findViewById(R.id.lv_actual_item);
    }

    public void initData(ArrayList<SJCZ_Entity> list) {
        if(list.size()>0){
            mTishi.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }else{
            mTishi.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }

        this.mSjczList = list;
        mAdapter = new ActualItemAdapter();
        mListView.setAdapter(mAdapter);
        //开启一个线程专门更新时间
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (run) {
                    Message msg = new Message();
                    msg.what = 0x02;
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

    public class ActualItemAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSjczList.size();
        }

        @Override
        public Object getItem(int position) {
            return mSjczList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(mActivity, R.layout.list_studied_actual, null);
                holder = new ViewHolder();
                holder.rlRoot = convertView.findViewById(R.id.rl_root);
                holder.tvItemTitle = convertView.findViewById(R.id.tv_studied_actual_tital);
                holder.tvSyCoinOrTime = convertView.findViewById(R.id.tv_sy_coin_time);
                holder.tvCoinOrTime = convertView.findViewById(R.id.tv_coin_time);
                holder.ivCoin = convertView.findViewById(R.id.iv_coin);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvItemTitle.setText(mSjczList.get(position).TM);
            if(isBindingCard) {//如果已经绑定了学习卡
                //获取绑定的学习卡有效期
                String yxq = (String) SPUtils.get(mActivity, mKSXM.ID + mKSXM.NAME, "");
                Log.i(TAG, "getView: yxq = " + yxq);
                if(!yxq.isEmpty()){
                    SimpleDateFormat sdf =   new SimpleDateFormat( "yyyyMMddHHmmss" );
                    try {
                        String s = yxq.replaceAll("-", " ");
                        String s1 = s.replaceAll(":", " ");
                        Date date=sdf.parse( s1.replaceAll(" +","") );
                        long time = date.getTime();//有效期的时间
                        Log.i(TAG, "getView: time="+time);
                        long time1 = new Date().getTime();//现在的时间
                        Log.i(TAG, "getView: time1="+time1);
                        long l = time - time1;//距离到有效期结束还剩余的毫秒值
                        long ll = l/1000;//转换成秒
                        holder.tvSyCoinOrTime.setVisibility(View.VISIBLE);
                        holder.tvCoinOrTime.setVisibility(View.VISIBLE);
                        holder.tvSyCoinOrTime.setText("剩余");
                        holder.tvCoinOrTime.setText(TimeUtil.getTimeString((int) ll));
                        holder.ivCoin.setVisibility(View.INVISIBLE);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    holder.rlRoot.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //跳转到实际操作页面
                            gotoActualActiviay();
                        }
                    });
                }
            }else{//未绑定学习卡
                //判断是否已消耗金币进行观看
                long time = new Date().getTime();
                final long sur_time = (long) SPUtils.get(mActivity, String.valueOf(mSjczList.get(position).ID), 0L);
                long l = time - sur_time;//从点击到现在的毫秒值
                long ll = l / 1000;//转换成秒
                int lll = (int) (mSjczList.get(position).XSFZ * 60 - ll);//还剩余的秒值
                if (0 >= lll) {
                    holder.tvSyCoinOrTime.setText("查询消耗");
                    holder.tvCoinOrTime.setText(mSjczList.get(position).XH + "");
                    holder.ivCoin.setVisibility(View.VISIBLE);
                } else {
                    holder.tvSyCoinOrTime.setText("剩余");
                    holder.tvCoinOrTime.setText(TimeUtil.getTimeString(lll));
                    holder.ivCoin.setVisibility(View.INVISIBLE);
                }
                holder.rlRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //判断是否已经购买过 并且没有过期
                        long now_time = new Date().getTime();
                        long sur_time1 = (long) SPUtils.get(mActivity, String.valueOf(mSjczList.get(position).ID), 0L);
                        long l = (now_time - sur_time1) / 1000;
                        int ll = (int) (mSjczList.get(position).XSFZ * 60 - l);
                        if (ll > 0) {//已经购买过
                            //跳转到实际操作页面
                            gotoActualActiviay();
                        } else {
                            if (mCurr_Coin >= mSjczList.get(position).XH) {
                                new AlertDialog
                                        .Builder(mActivity).setTitle("")
                                        .setMessage("确定要消耗学习币进行学习吗？")
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                //ToDo: 你想做的事情
                                                mCurr_Coin = mCurr_Coin - mSjczList.get(position).XH;
                                                ((StudiedActivity) mActivity).changeCoin(mCurr_Coin);
                                                long time = new Date().getTime();
                                                SPUtils.put(mActivity, String.valueOf(mSjczList.get(position).ID), time);
                                                //跳转到实际操作页面
                                                gotoActualActiviay();
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
                            } else {
                                ToastUtil.getInstance().shortShow("您的学习不足，请充值后再试");
                            }
                        }

                    }
                });
            }
            return convertView;
        }

        class ViewHolder {
            RelativeLayout rlRoot;
            TextView tvItemTitle;
            TextView tvSyCoinOrTime;
            TextView tvCoinOrTime;
            ImageView ivCoin;
        }
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

    /**
     * 跳转到实际学习activity
     */
    private void gotoActualActiviay() {
        Intent intent = new Intent();
        intent.putExtra("TTSUtileLisenning", "TTSUtileLisenning");
        intent.setClass(mActivity, ActualActivity.class);
        mActivity.startActivity(intent);
    }


}

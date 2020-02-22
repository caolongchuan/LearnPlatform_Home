package com.clc.learnplatform.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.clc.learnplatform.R;
import com.clc.learnplatform.entity.CTJL_Entity;
import com.clc.learnplatform.entity.LSSJ_Entity;
import com.clc.learnplatform.entity.ST_Entity;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.ToastUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 错题强化
 */
public class CtqhActivity extends AppCompatActivity {
    private final String TAG = "CtqhActivity";
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private String openid;
    private String xmid;

    private int mAllNum = 0;
    private ArrayList<View> mViewList;
    private ArrayList<CTJL_Entity> mCtjlList;//错题list
    private MyPagerAdapter mAdapter;

    private ImageView mBack;
    private ViewPager mViewPager;

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case 0x01:
                    initData();
                    break;
            }
            return false;
        }
    });

    private void initData() {
        mAdapter = new MyPagerAdapter(mViewList,mCtjlList);
        mViewPager.setAdapter(mAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ctqh);
        //隐藏标题栏,有效
        getSupportActionBar().hide();

        Intent intent = getIntent();
        openid = intent.getStringExtra("openid");
        xmid = intent.getStringExtra("xmid");
        initView();
        getDataFromService();
    }

    //从服务器获取数据
    private void getDataFromService() {
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(openid)
                .append("&xmid=")
                .append(xmid);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.CTLX_URL)//错题练习
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 获取错题数据失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.i(TAG, "CtqhActivity.onResponse: responseInfo===" + responseInfo);
                String error = null;
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//登录失败
                        String message = jsonObject.getString("message");
                        Log.i(TAG, "CtqhActivity: message===" + message);
                    } else if (error.equals("false")) {//成功
                        analysisData(responseInfo);//解析数据
                        Message msg = new Message();
                        msg.what = 0x01;
                        mHandler.sendMessage(msg);//通知UI线程更新界面
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //解析数据
    private void analysisData(String responseInfo) {
        try {
            JSONObject jsonObject = new JSONObject(responseInfo);
            JSONArray ctlist = jsonObject.getJSONArray("ctlist");//错题list
            LayoutInflater li = getLayoutInflater();
            mAllNum = ctlist.length();
            for (int i=0;i<mAllNum;i++){
                JSONObject ct_obj = ctlist.getJSONObject(i);
                CTJL_Entity ce = new CTJL_Entity();
                ce.ID = ct_obj.getString("ID");
                ce.XMID = ct_obj.getString("XMID");
                ce.XZDA = ct_obj.getString("XZDA");
                ce.STID = ct_obj.getString("STID");
                ce.YHID = ct_obj.getString("YHID");
                ce.SJ = ct_obj.getString("SJ");
                JSONObject st = ct_obj.getJSONObject("ST");
                ce.ST = new ST_Entity();
                ce.ST.ID = st.getString("ID");
                ce.ST.TM = st.getString("TM");
                ce.ST.TP = st.getString("TP");
                ce.ST.TX = st.getString("TX");
                ce.ST.TXID = st.getString("TXID");
                ce.ST.XX = st.getString("XX");
                ce.ST.XZDA = st.getString("XZDA");
                ce.ST.ZQDA = st.getString("ZQDA");

                mCtjlList.add(ce);
                mViewList.add(li.inflate(R.layout.view_ctqh_item, null, false));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mBack = findViewById(R.id.iv_back);
        mViewList = new ArrayList<>();
        mCtjlList = new ArrayList<>();

        mViewPager = findViewById(R.id.vp_ctqh);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }





    public class MyPagerAdapter extends PagerAdapter {

        private ArrayList<View> viewLists;
        private ArrayList<CTJL_Entity> ctjlList;

        public MyPagerAdapter() {

        }

        public MyPagerAdapter(ArrayList<View> viewList, ArrayList<CTJL_Entity> ctjlList) {
            super();
            this.viewLists = viewList;
            this.ctjlList = ctjlList;
        }

        @Override
        public int getCount() {
            return mCtjlList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            TextView mTvCurrNum =   viewLists.get(position).findViewById(R.id.tv_curr_num);
            mTvCurrNum.setText(String.valueOf(position+1));
            TextView mTvAllNum =   viewLists.get(position).findViewById(R.id.tv_all_num);
            mTvAllNum.setText(String.valueOf(ctjlList.size()));

            Button btnOk = viewLists.get(position).findViewById(R.id.btn_ok);
            TextView tvItemType = viewLists.get(position).findViewById(R.id.tv_item_type);//是什么类型  //00单选 01多选 02判断
            switch (ctjlList.get(position).ST.TX) {
                case "00":
                    tvItemType.setText("单选");
                    btnOk.setVisibility(View.GONE);
                    break;
                case "01":
                    tvItemType.setText("多选");
                    btnOk.setVisibility(View.VISIBLE);
                    break;
                case "02":
                    tvItemType.setText("判断");
                    btnOk.setVisibility(View.GONE);
                    break;
            }
            ImageView ivTP = viewLists.get(position).findViewById(R.id.iv_tp);
            if(ctjlList.get(position).ST.TP.equals("null")){
                ivTP.setVisibility(View.GONE);
            }else{
                ivTP.setVisibility(View.VISIBLE);
                Glide.with(CtqhActivity.this).load(ctjlList.get(position).ST.TP).into(ivTP);
            }

            LinearLayout[] llXuanXiang = new LinearLayout[10];
            llXuanXiang[0] = viewLists.get(position).findViewById(R.id.ll_1);
            llXuanXiang[1] = viewLists.get(position).findViewById(R.id.ll_2);
            llXuanXiang[2] = viewLists.get(position).findViewById(R.id.ll_3);
            llXuanXiang[3] = viewLists.get(position).findViewById(R.id.ll_4);
            llXuanXiang[4] = viewLists.get(position).findViewById(R.id.ll_5);
            llXuanXiang[5] = viewLists.get(position).findViewById(R.id.ll_6);
            llXuanXiang[6] = viewLists.get(position).findViewById(R.id.ll_7);
            llXuanXiang[7] = viewLists.get(position).findViewById(R.id.ll_8);
            llXuanXiang[8] = viewLists.get(position).findViewById(R.id.ll_9);
            llXuanXiang[9] = viewLists.get(position).findViewById(R.id.ll_10);
            ImageView[] ivXuanXiang = new ImageView[10];
            ivXuanXiang[0] = viewLists.get(position).findViewById(R.id.iv_1);
            ivXuanXiang[1] = viewLists.get(position).findViewById(R.id.iv_2);
            ivXuanXiang[2] = viewLists.get(position).findViewById(R.id.iv_3);
            ivXuanXiang[3] = viewLists.get(position).findViewById(R.id.iv_4);
            ivXuanXiang[4] = viewLists.get(position).findViewById(R.id.iv_5);
            ivXuanXiang[5] = viewLists.get(position).findViewById(R.id.iv_6);
            ivXuanXiang[6] = viewLists.get(position).findViewById(R.id.iv_7);
            ivXuanXiang[7] = viewLists.get(position).findViewById(R.id.iv_8);
            ivXuanXiang[8] = viewLists.get(position).findViewById(R.id.iv_9);
            ivXuanXiang[9] = viewLists.get(position).findViewById(R.id.iv_10);
            TextView tvItemName = viewLists.get(position).findViewById(R.id.tv_item_name);//题目
            tvItemName.setText(ctjlList.get(position).ST.TM);
            final TextView tvDanAn = viewLists.get(position).findViewById(R.id.tv_zqda);//答案
            tvDanAn.setText(ctjlList.get(position).ST.ZQDA);
            final char[] cc = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
            final String[] split = ctjlList.get(position).ST.XX.trim().split("\\^");
            final TextView[] tvXuanXiang = new TextView[10];
            tvXuanXiang[0] = viewLists.get(position).findViewById(R.id.tv_1);
            tvXuanXiang[1] = viewLists.get(position).findViewById(R.id.tv_2);
            tvXuanXiang[2] = viewLists.get(position).findViewById(R.id.tv_3);
            tvXuanXiang[3] = viewLists.get(position).findViewById(R.id.tv_4);
            tvXuanXiang[4] = viewLists.get(position).findViewById(R.id.tv_5);
            tvXuanXiang[5] = viewLists.get(position).findViewById(R.id.tv_6);
            tvXuanXiang[6] = viewLists.get(position).findViewById(R.id.tv_7);
            tvXuanXiang[7] = viewLists.get(position).findViewById(R.id.tv_8);
            tvXuanXiang[8] = viewLists.get(position).findViewById(R.id.tv_9);
            tvXuanXiang[9] = viewLists.get(position).findViewById(R.id.tv_10);
            final int[] iv_choice = new int[]{R.mipmap.icon_choice_xuanxiang_a, R.mipmap.icon_choice_xuanxiang_b,
                    R.mipmap.icon_choice_xuanxiang_c, R.mipmap.icon_choice_xuanxiang_d,
                    R.mipmap.icon_choice_xuanxiang_e, R.mipmap.icon_choice_xuanxiang_f,
                    R.mipmap.icon_choice_xuanxiang_g, R.mipmap.icon_choice_xuanxiang_h,
                    R.mipmap.icon_choice_xuanxiang_i, R.mipmap.icon_choice_xuanxiang_j};
            Button mOk = viewLists.get(position).findViewById(R.id.btn_ok);//确定按钮
            mOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (tvDanAn.getVisibility() == View.GONE) {
                        int jj = 0;
                        StringBuilder ss = new StringBuilder();
                        for (int i = 0; i < cc.length; i++) {
                            Object tag = tvXuanXiang[i].getTag();
                            if (tag != null) {
                                jj++;
                                ss.append(cc[i]);
                                if (ctjlList.get(position).ST.ZQDA.contains(String.valueOf(cc[i]))) {
                                    tvXuanXiang[i].setTextColor(Color.GREEN);
                                    Drawable drawable1 = getResources().getDrawable(R.mipmap.icon_right);
                                    drawable1.setBounds(0, 0, drawable1.getMinimumWidth(), drawable1.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[i].setCompoundDrawables(drawable1, null, null, null);
                                } else {
                                    tvXuanXiang[i].setTextColor(Color.RED);
                                    Drawable drawable1 = getResources().getDrawable(R.mipmap.icon_wrong);
                                    drawable1.setBounds(0, 0, drawable1.getMinimumWidth(), drawable1.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[i].setCompoundDrawables(drawable1, null, null, null);
                                }
                            }
                            if (tvXuanXiang[i].getVisibility() == View.GONE) {
                                break;
                            }
                        }
                        if (jj == 0) {
                            ToastUtil.getInstance().shortShow("此题为选择题，请先选择");
                        } else {
                            if (ctjlList.get(position).ST.ZQDA.equals(ss.toString())) {//正确
                                doTiJiao(ctjlList.get(position).ID);//提交
                            }
                            tvDanAn.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
            for (int j = 0; j < split.length; j++) {
                llXuanXiang[j].setVisibility(View.VISIBLE);
                if(split[j].contains("http")){
                    String s = StringUtils.substringAfter(split[j], "'");
                    String s1 = StringUtils.substringBefore(s, "'");

                    tvXuanXiang[j].setText("");
                    ivXuanXiang[j].setVisibility(View.VISIBLE);
                    Glide.with(CtqhActivity.this).load(s1).into(ivXuanXiang[j]);
                }else{
                    ivXuanXiang[j].setVisibility(View.GONE);
                    tvXuanXiang[j].setText(split[j]);
                }

            }
            for (int i = 0; i < 10; i++) {
                final int finalI = i;
                ivXuanXiang[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (tvDanAn.getVisibility() != View.VISIBLE) {//判断是否已经做完此题
                            if (ctjlList.get(position).ST.TX.equals("00") || ctjlList.get(position).ST.TX.equals("02")) {//单选或者判断
                                if (ctjlList.get(position).ST.ZQDA.equals(String.valueOf(cc[finalI]))) {//答对了
                                    tvXuanXiang[finalI].setTextColor(Color.GREEN);
                                    Drawable drawable = getResources().getDrawable(R.mipmap.icon_right);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);

                                    doTiJiao(ctjlList.get(position).ID);//提交
                                } else {//答错了
                                    tvXuanXiang[finalI].setTextColor(Color.RED);
                                    Drawable drawable = getResources().getDrawable(R.mipmap.icon_wrong);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);
                                    for (int k = 0; k < cc.length; k++) {
                                        if (ctjlList.get(position).ST.ZQDA.equals(String.valueOf(cc[k]))) {
                                            tvXuanXiang[k].setTextColor(Color.GREEN);
                                            Drawable drawable1 = getResources().getDrawable(R.mipmap.icon_right);
                                            drawable1.setBounds(0, 0, drawable1.getMinimumWidth(), drawable1.getMinimumHeight());//对图片进行压缩
                                            tvXuanXiang[k].setCompoundDrawables(drawable1, null, null, null);
                                            break;
                                        }
                                    }

                                }
                                tvDanAn.setVisibility(View.VISIBLE);
                            } else if (ctjlList.get(position).ST.TX.equals("01")) {//多选题
                                Drawable drawable = getResources().getDrawable(iv_choice[finalI]);
                                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);
                                tvXuanXiang[finalI].setTag(1);// 添加标记
                            }
                        }
                    }
                });
                tvXuanXiang[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (tvDanAn.getVisibility() != View.VISIBLE) {//判断是否已经做完此题
                            if (ctjlList.get(position).ST.TX.equals("00") || ctjlList.get(position).ST.TX.equals("02")) {//单选或者判断
                                if (ctjlList.get(position).ST.ZQDA.equals(String.valueOf(cc[finalI]))) {//答对了
                                    tvXuanXiang[finalI].setTextColor(Color.GREEN);
                                    Drawable drawable = getResources().getDrawable(R.mipmap.icon_right);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);

                                    doTiJiao(ctjlList.get(position).ID);//提交
                                } else {//答错了
                                    tvXuanXiang[finalI].setTextColor(Color.RED);
                                    Drawable drawable = getResources().getDrawable(R.mipmap.icon_wrong);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);
                                    for (int k = 0; k < cc.length; k++) {
                                        if (ctjlList.get(position).ST.ZQDA.equals(String.valueOf(cc[k]))) {
                                            tvXuanXiang[k].setTextColor(Color.GREEN);
                                            Drawable drawable1 = getResources().getDrawable(R.mipmap.icon_right);
                                            drawable1.setBounds(0, 0, drawable1.getMinimumWidth(), drawable1.getMinimumHeight());//对图片进行压缩
                                            tvXuanXiang[k].setCompoundDrawables(drawable1, null, null, null);
                                            break;
                                        }
                                    }

                                }
                                tvDanAn.setVisibility(View.VISIBLE);
                            } else if (ctjlList.get(position).ST.TX.equals("01")) {//多选题
                                Drawable drawable = getResources().getDrawable(iv_choice[finalI]);
                                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);
                                tvXuanXiang[finalI].setTag(1);// 添加标记
                            }
                        }
                    }
                });
            }
            container.addView(viewLists.get(position));
            return viewLists.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(mViewList.get(position));
        }
    }

    //提交正确题
    private void doTiJiao(String stid){
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        sb.append("stid=").append(stid);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.CTLXDD_URL)//错题正确提交
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

}

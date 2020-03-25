package com.clc.learnplatform.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
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
import com.clc.learnplatform.entity.ST_Entity;
import com.clc.learnplatform.entity.WZTK_Entity;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.StringUtil;
import com.clc.learnplatform.util.ToastUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 未做题练习
 */
public class WeiZuoTiActivity extends AppCompatActivity {
    private final String TAG = "WeiZuoTiActivity";
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private String openid;
    private String xmid;

    private int mWztkListSize;//总共有多少条数据
    private ArrayList<WZTK_Entity> mAllWztkList;//未做题数据
    private ArrayList<WZTK_Entity> mSonWztkList;//先加载600个

    private ArrayList<View> mViewList;
    private ViewPager mViewPager;
    private MyPagerAdapter mAdapter;

    private ImageView mBack;
    private AlertDialog alertDialog;//等待对话框


    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case 0x01://更新
                    alertDialog.dismiss();
                    mAdapter.notifyDataSetChanged();
                    break;
                case 0x02:
                    alertDialog.dismiss();
                    mAdapter.notifyDataSetChanged();
                    break;
            }

            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wei_zuo_ti);
        //隐藏标题栏,有效
        getSupportActionBar().hide();

        Intent intent = getIntent();
        this.openid = intent.getStringExtra("openid");
        this.xmid = intent.getStringExtra("xmid");
        String responseInfo = intent.getStringExtra("responseInfo");

        initView();
        analysisData(responseInfo);//解析数据 开启子线程
        initData();
    }

    private void initData() {
        mAdapter = new MyPagerAdapter();
        mViewPager.setAdapter(mAdapter);
    }

    //解析数据
    private void analysisData(final String responseInfo) {
        alertDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    JSONArray wzlist = jsonObject.getJSONArray("wzlist");
                    LayoutInflater li = getLayoutInflater();
                    mWztkListSize = wzlist.length();
                    for (int i = 0; i < mWztkListSize; i++) {
                        JSONObject wz_obj = wzlist.getJSONObject(i);
                        WZTK_Entity we = new WZTK_Entity();
                        we.ID = wz_obj.getString("ID");
                        we.XMID = wz_obj.getString("XMID");
                        we.YHID = wz_obj.getString("YHID");
                        we.STID = wz_obj.getString("STID");
                        we.TXID = wz_obj.getString("TXID");
                        we.ST = new ST_Entity();
                        JSONObject st = wz_obj.getJSONObject("ST");
                        we.ST.ID = st.getString("ID");
                        we.ST.TM = st.getString("TM");
                        we.ST.TP = st.getString("TP");
                        we.ST.TX = st.getString("TX");
                        we.ST.TXID = st.getString("TXID");
                        we.ST.XX = st.getString("XX");
                        we.ST.XZDA = st.getString("XZDA");
                        we.ST.ZQDA = st.getString("ZQDA");
                        mAllWztkList.add(we);
                        mViewList.add(li.inflate(R.layout.view_wztlx_item, null, false));//添加View
                        if (i < 600) {
                            mSonWztkList.add(we);
                        } else if (i == 600) {//加载了50条数据
                            Message msg = new Message();
                            msg.what = 0x01;
                            mHandler.sendMessage(msg);
                        }
                    }
                    //加载完成
                    Message msg = new Message();
                    msg.what = 0x02;
                    mHandler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).start();


    }

    private void initView() {
        alertDialog = new AlertDialog
                .Builder(this).setMessage("正在加载数据...")
                .create();

        mBack = findViewById(R.id.iv_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mViewPager = findViewById(R.id.vp_ctqh);
        mAllWztkList = new ArrayList<>();
        mSonWztkList = new ArrayList<>();
        mViewList = new ArrayList<>();

        mAdapter = new MyPagerAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int position;
            private int oldPositon;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                this.position = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == 1) {
                    oldPositon = position;
                }
                if (state == 0) {

                    if (position == oldPositon) {
                        if (position == 0) {
                            //滑动到第一页，继续向右滑
                        } else if (position == mViewPager.getAdapter().getCount() - 1) {
                            //滑动到最后一页，继续向左滑

                        } else {
                            //滑动到一半时停止滑动，当前停留在第position页
                        }
                    } else {
                        if (position < oldPositon) {
                            //从左向右
                            Log.i(TAG, "onPageScrollStateChanged: 从左向右");
                        } else {
                            //从右向左
                            Log.i(TAG, "onPageScrollStateChanged: 从右向左");
                        }
                        Message msg = new Message();
                        msg.what = 0x01;
                        mHandler.sendMessage(msg);
                    }
                }
            }
        });
    }


    public class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mSonWztkList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            TextView mCurrNum = mViewList.get(position).findViewById(R.id.tv_curr_num);
            mCurrNum.setText(String.valueOf(position + 1));
            TextView mTotalNum = mViewList.get(position).findViewById(R.id.tv_total_num);
            mTotalNum.setText(String.valueOf(mWztkListSize));
            Button btnOk = mViewList.get(position).findViewById(R.id.btn_ok);
            TextView tvItemType = mViewList.get(position).findViewById(R.id.tv_item_type);//是什么类型  //00单选 01多选 02判断
            switch (mSonWztkList.get(position).ST.TX) {
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
            ImageView ivTP = mViewList.get(position).findViewById(R.id.iv_tp);
            if (mSonWztkList.get(position).ST.TP.equals("null")) {
                ivTP.setVisibility(View.GONE);
            } else {
                ivTP.setVisibility(View.VISIBLE);
                Glide.with(WeiZuoTiActivity.this).load(mSonWztkList.get(position).ST.TP).into(ivTP);
            }

            TextView tvItemName = mViewList.get(position).findViewById(R.id.tv_item_name);//题目
            tvItemName.setText(mSonWztkList.get(position).ST.TM);
            final TextView tvDanAn = mViewList.get(position).findViewById(R.id.tv_zqda);//答案
            tvDanAn.setText(mSonWztkList.get(position).ST.ZQDA);
            final char[] cc = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
            final String[] split = mSonWztkList.get(position).ST.XX.trim().split("\\^");
            LinearLayout[] llXuanXiang = new LinearLayout[10];
            llXuanXiang[0] = mViewList.get(position).findViewById(R.id.ll_1);
            llXuanXiang[1] = mViewList.get(position).findViewById(R.id.ll_2);
            llXuanXiang[2] = mViewList.get(position).findViewById(R.id.ll_3);
            llXuanXiang[3] = mViewList.get(position).findViewById(R.id.ll_4);
            llXuanXiang[4] = mViewList.get(position).findViewById(R.id.ll_5);
            llXuanXiang[5] = mViewList.get(position).findViewById(R.id.ll_6);
            llXuanXiang[6] = mViewList.get(position).findViewById(R.id.ll_7);
            llXuanXiang[7] = mViewList.get(position).findViewById(R.id.ll_8);
            llXuanXiang[8] = mViewList.get(position).findViewById(R.id.ll_9);
            llXuanXiang[9] = mViewList.get(position).findViewById(R.id.ll_10);
            ImageView[] ivXuanXiang = new ImageView[10];
            ivXuanXiang[0] = mViewList.get(position).findViewById(R.id.iv_1);
            ivXuanXiang[1] = mViewList.get(position).findViewById(R.id.iv_2);
            ivXuanXiang[2] = mViewList.get(position).findViewById(R.id.iv_3);
            ivXuanXiang[3] = mViewList.get(position).findViewById(R.id.iv_4);
            ivXuanXiang[4] = mViewList.get(position).findViewById(R.id.iv_5);
            ivXuanXiang[5] = mViewList.get(position).findViewById(R.id.iv_6);
            ivXuanXiang[6] = mViewList.get(position).findViewById(R.id.iv_7);
            ivXuanXiang[7] = mViewList.get(position).findViewById(R.id.iv_8);
            ivXuanXiang[8] = mViewList.get(position).findViewById(R.id.iv_9);
            ivXuanXiang[9] = mViewList.get(position).findViewById(R.id.iv_10);
            final TextView[] tvXuanXiang = new TextView[10];
            tvXuanXiang[0] = mViewList.get(position).findViewById(R.id.tv_1);
            tvXuanXiang[1] = mViewList.get(position).findViewById(R.id.tv_2);
            tvXuanXiang[2] = mViewList.get(position).findViewById(R.id.tv_3);
            tvXuanXiang[3] = mViewList.get(position).findViewById(R.id.tv_4);
            tvXuanXiang[4] = mViewList.get(position).findViewById(R.id.tv_5);
            tvXuanXiang[5] = mViewList.get(position).findViewById(R.id.tv_6);
            tvXuanXiang[6] = mViewList.get(position).findViewById(R.id.tv_7);
            tvXuanXiang[7] = mViewList.get(position).findViewById(R.id.tv_8);
            tvXuanXiang[8] = mViewList.get(position).findViewById(R.id.tv_9);
            tvXuanXiang[9] = mViewList.get(position).findViewById(R.id.tv_10);
            final int[] iv_no_choice = new int[]{R.mipmap.icon_xuanxiang_a, R.mipmap.icon_xuanxiang_b,
                    R.mipmap.icon_xuanxiang_c, R.mipmap.icon_xuanxiang_d,
                    R.mipmap.icon_xuanxiang_e, R.mipmap.icon_xuanxiang_f,
                    R.mipmap.icon_xuanxiang_g, R.mipmap.icon_xuanxiang_h,
                    R.mipmap.icon_xuanxiang_i, R.mipmap.icon_xuanxiang_j};
            final int[] iv_choice = new int[]{R.mipmap.icon_choice_xuanxiang_a, R.mipmap.icon_choice_xuanxiang_b,
                    R.mipmap.icon_choice_xuanxiang_c, R.mipmap.icon_choice_xuanxiang_d,
                    R.mipmap.icon_choice_xuanxiang_e, R.mipmap.icon_choice_xuanxiang_f,
                    R.mipmap.icon_choice_xuanxiang_g, R.mipmap.icon_choice_xuanxiang_h,
                    R.mipmap.icon_choice_xuanxiang_i, R.mipmap.icon_choice_xuanxiang_j};
            Button mOk = mViewList.get(position).findViewById(R.id.btn_ok);//确定按钮
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
                                if (mSonWztkList.get(position).ST.ZQDA.contains(String.valueOf(cc[i]))) {
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
                            if (mSonWztkList.get(position).ST.ZQDA.equals(ss.toString())) {//正确
                            } else {//错误
                                doSave(mSonWztkList.get(position).STID, "cuo", ss.toString());//保存
                            }
                            tvDanAn.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
            for (int j = 0; j < split.length; j++) {
                llXuanXiang[j].setVisibility(View.VISIBLE);

                if (split[j].contains("http")) {
                    String s = StringUtils.substringAfter(split[j], "'");
                    String s1 = StringUtils.substringBefore(s, "'");

                    tvXuanXiang[j].setText("");
                    ivXuanXiang[j].setVisibility(View.VISIBLE);
                    Glide.with(WeiZuoTiActivity.this).load(s1).into(ivXuanXiang[j]);
                } else {
                    ivXuanXiang[j].setVisibility(View.GONE);
                    tvXuanXiang[j].setText(StringUtil.replaceBlank(split[j]));
                }
            }
            for (int i = 0; i < 10; i++) {
                final int finalI = i;
                ivXuanXiang[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (tvDanAn.getVisibility() != View.VISIBLE) {//判断是否已经做完此题
                            if (mSonWztkList.get(position).ST.TX.equals("00") || mSonWztkList.get(position).ST.TX.equals("02")) {//单选或者判断
                                if (mSonWztkList.get(position).ST.ZQDA.equals(String.valueOf(cc[finalI]))) {//答对了
//                                    totalScore = totalScore + lssjList.get(position).FZ;//添加进分数
                                    tvXuanXiang[finalI].setTextColor(Color.GREEN);
                                    Drawable drawable = getResources().getDrawable(R.mipmap.icon_right);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);

//                                    rightItemNum++;
//                                    mRightItemNum.setText(rightItemNum + "");
//                                    mnksSave(lssjList.get(position).TX, "dui", lssjList.get(position).ID,
//                                            String.valueOf(lssjList.get(position).FZ),
//                                            String.valueOf(cc[finalI]), lssjList.get(position).STID);
                                } else {//答错了
                                    tvXuanXiang[finalI].setTextColor(Color.RED);
                                    Drawable drawable = getResources().getDrawable(R.mipmap.icon_wrong);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);
                                    for (int k = 0; k < cc.length; k++) {
                                        if (mSonWztkList.get(position).ST.ZQDA.equals(String.valueOf(cc[k]))) {
                                            tvXuanXiang[k].setTextColor(Color.GREEN);
                                            Drawable drawable1 = getResources().getDrawable(R.mipmap.icon_right);
                                            drawable1.setBounds(0, 0, drawable1.getMinimumWidth(), drawable1.getMinimumHeight());//对图片进行压缩
                                            tvXuanXiang[k].setCompoundDrawables(drawable1, null, null, null);
                                            break;
                                        }
                                    }
                                    doSave(mSonWztkList.get(position).STID, "cuo", String.valueOf(cc[finalI]));//保存
                                }
                                tvDanAn.setVisibility(View.VISIBLE);
                            } else if (mSonWztkList.get(position).ST.TX.equals("01")) {//多选题
                                Object tag = tvXuanXiang[finalI].getTag();
                                if(tag == null){
                                    Drawable drawable = getResources().getDrawable(iv_choice[finalI]);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);
                                    tvXuanXiang[finalI].setTag(1);// 添加标记
                                }else{
                                    Drawable drawable = getResources().getDrawable(iv_no_choice[finalI]);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);
                                    tvXuanXiang[finalI].setTag(null);// 添加空标记
                                }
                            }
                        }
                    }
                });
                llXuanXiang[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (tvDanAn.getVisibility() != View.VISIBLE) {//判断是否已经做完此题
                            if (mSonWztkList.get(position).ST.TX.equals("00") || mSonWztkList.get(position).ST.TX.equals("02")) {//单选或者判断
                                if (mSonWztkList.get(position).ST.ZQDA.equals(String.valueOf(cc[finalI]))) {//答对了
//                                    totalScore = totalScore + lssjList.get(position).FZ;//添加进分数
                                    tvXuanXiang[finalI].setTextColor(Color.GREEN);
                                    Drawable drawable = getResources().getDrawable(R.mipmap.icon_right);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);

//                                    rightItemNum++;
//                                    mRightItemNum.setText(rightItemNum + "");
//                                    mnksSave(lssjList.get(position).TX, "dui", lssjList.get(position).ID,
//                                            String.valueOf(lssjList.get(position).FZ),
//                                            String.valueOf(cc[finalI]), lssjList.get(position).STID);
                                } else {//答错了
                                    tvXuanXiang[finalI].setTextColor(Color.RED);
                                    Drawable drawable = getResources().getDrawable(R.mipmap.icon_wrong);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);
                                    for (int k = 0; k < cc.length; k++) {
                                        if (mSonWztkList.get(position).ST.ZQDA.equals(String.valueOf(cc[k]))) {
                                            tvXuanXiang[k].setTextColor(Color.GREEN);
                                            Drawable drawable1 = getResources().getDrawable(R.mipmap.icon_right);
                                            drawable1.setBounds(0, 0, drawable1.getMinimumWidth(), drawable1.getMinimumHeight());//对图片进行压缩
                                            tvXuanXiang[k].setCompoundDrawables(drawable1, null, null, null);
                                            break;
                                        }
                                    }
                                    doSave(mSonWztkList.get(position).STID, "cuo", String.valueOf(cc[finalI]));//保存
                                }
                                tvDanAn.setVisibility(View.VISIBLE);
                            } else if (mSonWztkList.get(position).ST.TX.equals("01")) {//多选题
                                Object tag = tvXuanXiang[finalI].getTag();
                                if(tag == null){
                                    Drawable drawable = getResources().getDrawable(iv_choice[finalI]);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);
                                    tvXuanXiang[finalI].setTag(1);// 添加标记
                                }else{
                                    Drawable drawable = getResources().getDrawable(iv_no_choice[finalI]);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);
                                    tvXuanXiang[finalI].setTag(null);// 添加空标记
                                }
                            }
                        }
                    }
                });
            }
            container.addView(mViewList.get(position));
            return mViewList.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(mViewList.get(position));
        }
    }

    //保存
    public void doSave(String stid, String type, String xzda) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(100, TimeUnit.SECONDS)//设置读取超时时间
                .build();

        StringBuffer sb = new StringBuffer();
        sb.append("stid=").append(stid).append("&type=").append(type).append("&xzda=").append(xzda);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.WZBC_URL)//未做题保存
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 未做题保存失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "onResponse: 未做题保存成功");
            }
        });
    }


}

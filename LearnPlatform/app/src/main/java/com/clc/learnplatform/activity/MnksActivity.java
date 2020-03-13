package com.clc.learnplatform.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.clc.learnplatform.R;
import com.clc.learnplatform.entity.KSXM_Entity;
import com.clc.learnplatform.entity.LSSJ_Entity;
import com.clc.learnplatform.entity.MNKS_Entity;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.SPUtils;
import com.clc.learnplatform.util.TimeUtil;
import com.clc.learnplatform.util.ToastUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 模拟考试
 */
public class MnksActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "MnksActivity";
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private String openid;
    private String xmid;//项目id

    private ImageView mIvBack;//返回
    private TextView mShangYiTi;//上一题
    private TextView mXiaYiTi;//下一题
    private ViewPager mViewPager;
    private LinearLayout mJiaoJuan;//交卷
    private TextView mCurrItem;//当前是第几道题
    private TextView mTotalItemNum;//全部题数
    private TextView mRightItemNum;//对的题数
    private TextView mWrongItemNum;//错的题数
    private TextView mClock;//剩余时间

    private int mnksTime = 3600;//模拟考试的时间（秒）
    private int rightItemNum = 0;//正确的数量
    private int wrongItemNum = 0;//错误的数量

    private KSXM_Entity mKsxmEntity;//考试项目
    private MNKS_Entity mMnksEntity;//模拟考试
    private ArrayList<LSSJ_Entity> mLssjList;//临时试卷类
    private ArrayList<View> mViewList;

    private boolean mRun;//子线程运行开关
    private int totalScore = 0;//总分数

    private AlertDialog alertDialog;//等待对话框

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case 0x01://从服务器获取数据完成
                    alertDialog.dismiss();
                    initData();
                    break;
                case 0x02://更新倒计时
                    mnksTime--;
                    if (mnksTime <= 0) {
                        mnksTime = 0;
                        mClock.setText(TimeUtil.getTimeString(mnksTime));
                        mRun = false;
                    } else {
                        mClock.setText(TimeUtil.getTimeString(mnksTime));
                    }
                    break;
            }
            return false;
        }
    });

    //初始化数据
    private void initData() {
        MyPagerAdapter mAdapter = new MyPagerAdapter(mViewList, mLssjList);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrItem.setText((position + 1) + "");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mTotalItemNum.setText(mMnksEntity.ZTS + "");
        mRightItemNum.setText(rightItemNum + "");
        mWrongItemNum.setText(wrongItemNum + "");
        mClock.setText(TimeUtil.getTimeString(mnksTime));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mnks);
        //隐藏标题栏,有效
        getSupportActionBar().hide();

        Intent intent = getIntent();
        openid = intent.getStringExtra("openid");
        xmid = intent.getStringExtra("xmid");

        initView();
        getDataFromService();//从服务器获取数据
    }

    private void initView() {
        alertDialog = new AlertDialog
                .Builder(this).setMessage("正在加载数据...")
                .create();

        mKsxmEntity = new KSXM_Entity();
        mMnksEntity = new MNKS_Entity();
        mLssjList = new ArrayList<>();
        mViewList = new ArrayList<>();

        mIvBack = findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(this);
        mShangYiTi = findViewById(R.id.tv_shangyiti);
        mShangYiTi.setOnClickListener(this);
        mXiaYiTi = findViewById(R.id.tv_xiayiti);
        mXiaYiTi.setOnClickListener(this);
        mViewPager = findViewById(R.id.vp_main);
        mJiaoJuan = findViewById(R.id.ll_jiaojuan);
        mJiaoJuan.setOnClickListener(this);
        mCurrItem = findViewById(R.id.tv_curr_ti_no);
        mTotalItemNum = findViewById(R.id.tv_curr_ti_total);
        mRightItemNum = findViewById(R.id.tv_right_num);
        mWrongItemNum = findViewById(R.id.tv_wrong_num);
        mClock = findViewById(R.id.tv_mnks_time);

        //开启一个子线程更新计时器
        mRun = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mRun) {
                    Message msg = new Message();
                    msg.what = 0x02;
                    mHandler.sendMessage(msg);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    //从服务器获取数据
    private void getDataFromService() {
        alertDialog.show();
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(openid)
                .append("&xmid=")
                .append(xmid);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.MNKS_URL)//模拟考试接口
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 自动登录失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.i(TAG, "autoSignIn.onResponse: responseInfo===" + responseInfo);
                String error = null;
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//登录失败
                        String message = jsonObject.getString("message");
                        Log.i(TAG, "onResponse: message===" + message);
                    } else if (error.equals("false")) {//登录成功
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

    //解析从服务器返回的数据
    private void analysisData(String responseInfo) {
        try {
            JSONObject jsonObject = new JSONObject(responseInfo);
            JSONObject ksxm = jsonObject.getJSONObject("ksxm");//考试项目
            mKsxmEntity.ID = ksxm.getString("ID");
            mKsxmEntity.NAME = ksxm.getString("NAME");
            mKsxmEntity.DM = ksxm.getString("DM");
            mKsxmEntity.BZ = ksxm.getString("BZ");
            mKsxmEntity.ZT = ksxm.getString("ZT");
            mKsxmEntity.ZLID = ksxm.getString("ZLID");
            mKsxmEntity.ZTL = ksxm.getInt("ZTL");
            mKsxmEntity.MNXH = ksxm.getInt("MNXH");
            mKsxmEntity.CTL = ksxm.getInt("CTL");
            mKsxmEntity.STXH = ksxm.getInt("STXH");
            mKsxmEntity.STXH = ksxm.getInt("STXH");
            JSONObject mnks = jsonObject.getJSONObject("mnks");//模拟考试
            mMnksEntity.ID = mnks.getString("ID");
            mMnksEntity.XMID = mnks.getString("XMID");
            mMnksEntity.YHID = mnks.getString("YHID");
            mMnksEntity.KSSJ = mnks.getString("KSSJ");
            mMnksEntity.JSSJ = mnks.getString("JSSJ");
            mMnksEntity.FS = mnks.getInt("FS");
            mMnksEntity.SFJL = mnks.getString("SFJL");
            mMnksEntity.DTSL = mnks.getInt("DTSL");
            mMnksEntity.ZTS = mnks.getInt("ZTS");
            mMnksEntity.ZQSL = mnks.getInt("ZQSL");
//            mMnksEntity.GXSJ = mnks.getString("GXSJ");
            JSONArray sjlist = jsonObject.getJSONArray("sjlist");
            LayoutInflater li = getLayoutInflater();
            for (int i = 0; i < sjlist.length(); i++) {
                JSONObject sj_obj = sjlist.getJSONObject(i);
                LSSJ_Entity le = new LSSJ_Entity();
                le.ID = sj_obj.getString("ID");
                le.XMID = sj_obj.getString("XMID");
                le.STID = sj_obj.getString("STID");
                le.XZDA = sj_obj.getString("XZDA");
                le.MNKSID = sj_obj.getString("MNKSID");
                le.YHID = sj_obj.getString("YHID");
                le.TM = sj_obj.getString("TM");
                le.TP = sj_obj.getString("TP");
                le.XX = sj_obj.getString("XX");
                le.ZQDA = sj_obj.getString("ZQDA");
                le.TXID = sj_obj.getString("TXID");
                le.PLSX = sj_obj.getString("PLSX");
                le.TX = sj_obj.getString("TX");
                le.FZ = sj_obj.getInt("FZ");
                mLssjList.add(le);
                mViewList.add(li.inflate(R.layout.view_mnks_item, null, false));//添加View
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back://返回
                finish();
                break;
            case R.id.ll_jiaojuan://交卷
                if (rightItemNum + wrongItemNum < mMnksEntity.ZTS) {
                    new AlertDialog
                            .Builder(this).setTitle("")
                            .setMessage("你还有未做完的试题，确定要交卷吗？")
                            .setPositiveButton("交卷", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //ToDo: 你想做的事情
                                    doJiaoJuan();//交卷
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton("再做一会", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //ToDo: 你想做的事情
                                    dialogInterface.dismiss();
                                }
                            })
                            .create()
                            .show();
                } else {
                    doJiaoJuan();//交卷
                }
                break;
            case R.id.tv_shangyiti://上一题
                String temp = mCurrItem.getText().toString();
                int integer = Integer.valueOf(temp) - 1;
                if(integer==0){
                    ToastUtil.getInstance().shortShow("已经是第一页");
                }else{
                    mViewPager.setCurrentItem(integer - 1);
                }
                break;
            case R.id.tv_xiayiti://下一题
                String temp1 = mCurrItem.getText().toString();
                int integer1 = Integer.valueOf(temp1);
                if(integer1 == mMnksEntity.ZTS){
                    ToastUtil.getInstance().shortShow("已经是最后一页");
                }else{
                    mViewPager.setCurrentItem(integer1);
                }
                break;
        }
    }

    //交卷
    private void doJiaoJuan() {
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        sb.append("openid=").append(openid)
                .append("&mnksid=").append(mMnksEntity.ID);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.JIAOJUAN_URL)//答题保存
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
                String responseInfo = response.body().string();
                Log.i(TAG, "doJiaoJuan: " + responseInfo);
                String error = null;
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//交卷失败
                        String message = jsonObject.getString("message");
                        Log.i(TAG, "onResponse: message===" + message);
                    } else if (error.equals("false")) {//交卷成功
                        JSONObject ksxm = jsonObject.getJSONObject("ksxm");//考试项目
                        mKsxmEntity.ID = ksxm.getString("ID");
                        mKsxmEntity.NAME = ksxm.getString("NAME");
                        mKsxmEntity.DM = ksxm.getString("DM");
                        mKsxmEntity.BZ = ksxm.getString("BZ");
                        mKsxmEntity.ZT = ksxm.getString("ZT");
                        mKsxmEntity.ZLID = ksxm.getString("ZLID");
                        mKsxmEntity.ZTL = ksxm.getInt("ZTL");
                        mKsxmEntity.MNXH = ksxm.getInt("MNXH");
                        mKsxmEntity.CTL = ksxm.getInt("CTL");
                        mKsxmEntity.STXH = ksxm.getInt("STXH");
                        mKsxmEntity.STXH = ksxm.getInt("STXH");
                        JSONObject mnks = jsonObject.getJSONObject("mnks");//模拟考试
                        mMnksEntity.ID = mnks.getString("ID");
                        mMnksEntity.XMID = mnks.getString("XMID");
                        mMnksEntity.YHID = mnks.getString("YHID");
                        mMnksEntity.KSSJ = mnks.getString("KSSJ");
                        mMnksEntity.JSSJ = mnks.getString("JSSJ");
                        mMnksEntity.FS = mnks.getInt("FS");
                        mMnksEntity.SFJL = mnks.getString("SFJL");
                        mMnksEntity.DTSL = mnks.getInt("DTSL");
                        mMnksEntity.ZTS = mnks.getInt("ZTS");
                        mMnksEntity.ZQSL = mnks.getInt("ZQSL");

                        Intent intent = new Intent();
                        intent.putExtra("scroe",mMnksEntity.FS);
                        intent.putExtra("item_name",mKsxmEntity.NAME);
                        intent.putExtra("item_type",mKsxmEntity.DM);
                        intent.putExtra("start_time",mMnksEntity.KSSJ);
                        intent.putExtra("end_time",mMnksEntity.JSSJ);
                        intent.setClass(MnksActivity.this,SeeFractionActivity.class);
                        startActivity(intent);//进入查看分数页面
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRun = false;
    }

    public class MyPagerAdapter extends PagerAdapter {

        private ArrayList<View> viewLists;
        private ArrayList<LSSJ_Entity> lssjList;

        public MyPagerAdapter() {

        }

        public MyPagerAdapter(ArrayList<View> viewList, ArrayList<LSSJ_Entity> lssjList) {
            super();
            this.viewLists = viewList;
            this.lssjList = lssjList;
        }

        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            TextView tvCurrNum = viewLists.get(position).findViewById(R.id.tv_curr_num);
            tvCurrNum.setText(String.valueOf(position+1));
            TextView tvAllNum = viewLists.get(position).findViewById(R.id.tv_all_num);
            tvAllNum.setText(String.valueOf(lssjList.size()));
            Button btnOk = viewLists.get(position).findViewById(R.id.btn_ok);
            TextView tvItemType = viewLists.get(position).findViewById(R.id.tv_item_type);//是什么类型  //00单选 01多选 02判断
            switch (lssjList.get(position).TX) {
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
            if(lssjList.get(position).TP.equals("null")){
                ivTP.setVisibility(View.GONE);
            }else{
                ivTP.setVisibility(View.VISIBLE);
                Glide.with(MnksActivity.this).load(lssjList.get(position).TP).into(ivTP);
            }

            TextView tvItemName = viewLists.get(position).findViewById(R.id.tv_item_name);//题目
            tvItemName.setText(lssjList.get(position).TM);
            final TextView tvDanAn = viewLists.get(position).findViewById(R.id.tv_zqda);//答案
            tvDanAn.setText(lssjList.get(position).ZQDA);
            final char[] cc = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
            final String[] split = lssjList.get(position).XX.trim().split("\\^");
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
                                if (lssjList.get(position).ZQDA.contains(String.valueOf(cc[i]))) {
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
                            if (lssjList.get(position).ZQDA.equals(ss.toString())) {//正确
                                totalScore = totalScore + lssjList.get(position).FZ;//添加进分数
                                rightItemNum++;
                                mRightItemNum.setText(rightItemNum + "");
                                mnksSave(lssjList.get(position).TX, "dui", lssjList.get(position).ID,
                                        String.valueOf(lssjList.get(position).FZ),
                                        ss.toString(), lssjList.get(position).STID);
                            } else {//错误
                                wrongItemNum++;
                                mWrongItemNum.setText(wrongItemNum + "");
                                mnksSave(lssjList.get(position).TX, "cuo", lssjList.get(position).ID,
                                        String.valueOf(lssjList.get(position).FZ),
                                        ss.toString(), lssjList.get(position).STID);
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
                    Glide.with(MnksActivity.this).load(s1).into(ivXuanXiang[j]);
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
                            if (lssjList.get(position).TX.equals("00") || lssjList.get(position).TX.equals("02")) {//单选或者判断
                                if (lssjList.get(position).ZQDA.equals(String.valueOf(cc[finalI]))) {//答对了
                                    totalScore = totalScore + lssjList.get(position).FZ;//添加进分数
                                    tvXuanXiang[finalI].setTextColor(Color.GREEN);
                                    Drawable drawable = getResources().getDrawable(R.mipmap.icon_right);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);

                                    rightItemNum++;
                                    mRightItemNum.setText(rightItemNum + "");
                                    mnksSave(lssjList.get(position).TX, "dui", lssjList.get(position).ID,
                                            String.valueOf(lssjList.get(position).FZ),
                                            String.valueOf(cc[finalI]), lssjList.get(position).STID);
                                } else {//答错了
                                    tvXuanXiang[finalI].setTextColor(Color.RED);
                                    Drawable drawable = getResources().getDrawable(R.mipmap.icon_wrong);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);
                                    for (int k = 0; k < cc.length; k++) {
                                        if (lssjList.get(position).ZQDA.equals(String.valueOf(cc[k]))) {
                                            tvXuanXiang[k].setTextColor(Color.GREEN);
                                            Drawable drawable1 = getResources().getDrawable(R.mipmap.icon_right);
                                            drawable1.setBounds(0, 0, drawable1.getMinimumWidth(), drawable1.getMinimumHeight());//对图片进行压缩
                                            tvXuanXiang[k].setCompoundDrawables(drawable1, null, null, null);
                                            break;
                                        }
                                    }
                                    wrongItemNum++;
                                    mWrongItemNum.setText(wrongItemNum + "");
                                    mnksSave(lssjList.get(position).TX, "cuo", lssjList.get(position).ID,
                                            String.valueOf(lssjList.get(position).FZ),
                                            String.valueOf(cc[finalI]), lssjList.get(position).STID);
                                }
                                tvDanAn.setVisibility(View.VISIBLE);
                            } else if (lssjList.get(position).TX.equals("01")) {//多选题
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
                tvXuanXiang[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (tvDanAn.getVisibility() != View.VISIBLE) {//判断是否已经做完此题
                            if (lssjList.get(position).TX.equals("00") || lssjList.get(position).TX.equals("02")) {//单选或者判断
                                if (lssjList.get(position).ZQDA.equals(String.valueOf(cc[finalI]))) {//答对了
                                    totalScore = totalScore + lssjList.get(position).FZ;//添加进分数
                                    tvXuanXiang[finalI].setTextColor(Color.GREEN);
                                    Drawable drawable = getResources().getDrawable(R.mipmap.icon_right);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);

                                    rightItemNum++;
                                    mRightItemNum.setText(rightItemNum + "");
                                    mnksSave(lssjList.get(position).TX, "dui", lssjList.get(position).ID,
                                            String.valueOf(lssjList.get(position).FZ),
                                            String.valueOf(cc[finalI]), lssjList.get(position).STID);
                                } else {//答错了
                                    tvXuanXiang[finalI].setTextColor(Color.RED);
                                    Drawable drawable = getResources().getDrawable(R.mipmap.icon_wrong);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                                    tvXuanXiang[finalI].setCompoundDrawables(drawable, null, null, null);
                                    for (int k = 0; k < cc.length; k++) {
                                        if (lssjList.get(position).ZQDA.equals(String.valueOf(cc[k]))) {
                                            tvXuanXiang[k].setTextColor(Color.GREEN);
                                            Drawable drawable1 = getResources().getDrawable(R.mipmap.icon_right);
                                            drawable1.setBounds(0, 0, drawable1.getMinimumWidth(), drawable1.getMinimumHeight());//对图片进行压缩
                                            tvXuanXiang[k].setCompoundDrawables(drawable1, null, null, null);
                                            break;
                                        }
                                    }
                                    wrongItemNum++;
                                    mWrongItemNum.setText(wrongItemNum + "");
                                    mnksSave(lssjList.get(position).TX, "cuo", lssjList.get(position).ID,
                                            String.valueOf(lssjList.get(position).FZ),
                                            String.valueOf(cc[finalI]), lssjList.get(position).STID);
                                }
                                tvDanAn.setVisibility(View.VISIBLE);
                            } else if (lssjList.get(position).TX.equals("01")) {//多选题
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
            container.addView(viewLists.get(position));
            return viewLists.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(mViewList.get(position));
        }


    }

    //答题保存
    public void mnksSave(String tx, String type, String sjid, String fz, String xzda, String stid) {
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        sb.append("openid=").append(openid)
                .append("&tx=").append(tx)
                .append("&type=").append(type)
                .append("&mnksid=").append(mMnksEntity.ID)
                .append("&sjid=").append(sjid)
                .append("&fz=").append(fz)
                .append("&xzda=").append(xzda)
                .append("&stid=").append(stid)
                .append("&xmid=").append(xmid);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.MNKS_DTBC_URL)//答题保存
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 答题保存失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            }
        });
    }


}

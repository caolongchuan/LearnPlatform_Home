package com.clc.learnplatform.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.clc.learnplatform.R;
import com.clc.learnplatform.dialog.WeiZuoTiDialog;
import com.clc.learnplatform.entity.KHZL_Entity;
import com.clc.learnplatform.entity.KSXM_Entity;
import com.clc.learnplatform.entity.LXK_Entity;
import com.clc.learnplatform.entity.MNKS_Entity;
import com.clc.learnplatform.entity.SJCZ_Entity;
import com.clc.learnplatform.entity.TKXX_Entity;
import com.clc.learnplatform.entity.UserInfoEntity;
import com.clc.learnplatform.entity.WDCJ_Entity;
import com.clc.learnplatform.entity.XMFL_Entity;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.pager.ActualOperationPager;
import com.clc.learnplatform.pager.TheoryStudiedPager;
import com.clc.learnplatform.util.SPUtils;
import com.clc.learnplatform.util.TTSUtils;
import com.clc.learnplatform.util.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
 * 项目学习
 */
public class StudiedActivity extends AppCompatActivity implements View.OnClickListener {
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private static final String TAG = "StudiedActivity";

    private ImageView mBack;//返回
    private ImageView mHeadIcon;//头像

    private LinearLayout mMain;//主布局 包含下列所以控件 用于显示与隐藏
    private TextView mNickName;//昵称
    private TextView mPhoneNumber;//手机号码
    private TextView mLearnCoin;//学习币
    private TextView mrecharge;//充值
    private TextView mItemName;//项目名称
    private TextView mItemSign;//项目代号
    private TextView mBindingCard;//绑定学习卡
    private boolean isBindingCard = false;//标示是否有绑定学习卡

    private LinearLayout mContainer;//view容器 理论知识、实际操作
    Drawable drawable;
    private TextView mTheory;
    private TextView mActual;

    private TheoryStudiedPager tsp;//理论知识页面
    private ActualOperationPager aop;//实际操作页面

    private String openid;
    private String xmid;//项目id
    private String mDataString;//首页传过来的数据

    private ArrayList<XMFL_Entity> mXmflList;//项目分类列表 例如基础知识 专业知识等
    private KSXM_Entity mKSXM;//当前项目类
    private ArrayList<SJCZ_Entity> mSjczList;//实际操作list
    private UserInfoEntity mUserInfoEntiry;//用户类
    private WDCJ_Entity mWdcj;//我的成绩
    private KHZL_Entity mKhzl;//证书种类
    private MNKS_Entity mMnks;//模拟考试

    private int mLXL;//练题率

    private KHZL_Entity mKhzlEntity;//证书种类
    private ArrayList<LXK_Entity> mLxkList;//学习卡列表

    private AlertDialog alertDialog;//等待对话框
    private boolean dataDane;//用来表示数据是否加载完成 用于防止数据没有加载完时关闭发生的程序崩溃

    private int MNCSBZ;//规定模拟考试次数
    private int ZQLBZ;//规定正确率
    private int LXLBZ;//规定练题率

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case 0x01://初始化界面数据
                    alertDialog.dismiss();//隐藏等待提示框
                    dataDane = true;
                    mMain.setVisibility(View.VISIBLE);//初始化完成数据 将所有控件全部显示出来
                    //判读当前项目是否有绑定学习卡
                    for(int i=0;i<mLxkList.size();i++){
                        if(mKSXM.ID.equals(mLxkList.get(i).XMID)){
                            //先判断是否过期
                            SimpleDateFormat sdf =   new SimpleDateFormat( "yyyyMMddHHmmss" );
                            try {
                                String s = mLxkList.get(i).YXQ.replaceAll("-", " ");
                                String s1 = s.replaceAll(":", " ");
                                Date date = sdf.parse(s1.replaceAll(" +", ""));
                                long time = date.getTime();//有效期的时间
                                Log.i(TAG, "getView: time=" + time);
                                long time1 = new Date().getTime();//现在的时间
                                Log.i(TAG, "getView: time1=" + time1);
                                //判断有效期是否已经过期
                                if (time1 < time) {//未过期
                                    mBindingCard.setText("已绑定学习卡");
                                    isBindingCard = true;
                                    //将该学习卡的结束时间记录起来 用于计算显示倒计时（用该项目的ID与NAME加起来作为key）
                                    String key = mKSXM.ID + mKSXM.NAME;
                                    SPUtils.put(StudiedActivity.this,key,mLxkList.get(i).YXQ);
                                }else{//已过期
                                    mBindingCard.setText("绑定学习卡");
                                    isBindingCard = false;
                                }
                            }catch (ParseException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                    initUI();
                    break;
                case 0x02://定时更新实际操作的剩余时间
                    aop.updataTime();
                    break;
                case 0x03://未做题练习不到标准
                    //定义一个自己的dialog
                    WeiZuoTiDialog myDialog2new =
                            new WeiZuoTiDialog(StudiedActivity.this,
                                    MNCSBZ,ZQLBZ,LXLBZ,
                                    mWdcj.MNCS,mLXL, mWdcj.ZQL);
                    //实例化自定义的dialog
                    myDialog2new.show();
                    break;
                case 0x04:
                    ToastUtil.getInstance().shortShow("获取未做题数据失败");
                    break;
                case 0x05://定时更新理论知识的剩余时间
                    tsp.updataTime();
                    break;
            }
            return false;
        }
    });

    //初始化界面数据
    private void initUI() {
        Glide.with(this).load((String) mUserInfoEntiry.HEADIMGURL).into(mHeadIcon);//显示头像
        mNickName.setText(mUserInfoEntiry.NC);//显示昵称
        mPhoneNumber.setText(mUserInfoEntiry.SJH);//显示手机号码
        mLearnCoin.setText(String.valueOf(mUserInfoEntiry.ZHYE));//显示学习币数量
        mItemName.setText(mKSXM.NAME);//学习的项目名称
        mItemSign.setText(mKSXM.DM);//学习的项目的代号
        initData();
        tsp.initData(mXmflList);//初始化理论知识数据
        tsp.startProgress();//开始进度条动画
        aop.initData(mSjczList);//初始化实际操作数据
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studied);
        //隐藏标题栏,有效
        getSupportActionBar().hide();
        Intent intent = getIntent();
        openid = intent.getStringExtra("openid");
        xmid = intent.getStringExtra("xmid");
        mDataString = intent.getStringExtra("data_string");

        initView();
        getDataFromService();//从服务器获取数据
    }

    @Override
    protected void onResume() {
        super.onResume();
        //设置学习币
        int coin_num = (int) SPUtils.get(this, "COIN_NUM", 0);
        mLearnCoin.setText(String.valueOf(coin_num));
        if(tsp!=null){
            tsp.updataUI();
        }
    }

    private void initView() {
        alertDialog = new AlertDialog
                .Builder(this).setMessage("正在加载数据...")
                .create();
        mBack = findViewById(R.id.iv_back);
        mBack.setOnClickListener(this);
        mMain = findViewById(R.id.ll_main);
        mHeadIcon = findViewById(R.id.iv_user_head);
        mNickName = findViewById(R.id.tv_user_name);
        mPhoneNumber = findViewById(R.id.tv_phone_number);
        mLearnCoin = findViewById(R.id.tv_learn_b);
        mItemName = findViewById(R.id.tv_item_name);
        mItemSign = findViewById(R.id.tv_item_sign);
        mBindingCard = findViewById(R.id.tv_binding_card);
        mBindingCard.setOnClickListener(this);

        mContainer = findViewById(R.id.ll_container);//view容器 理论知识、实际操作
        mTheory = findViewById(R.id.tv_theoretical);
        mActual = findViewById(R.id.tv_actual);
        mTheory.setOnClickListener(this);
        mActual.setOnClickListener(this);
        drawable = getResources().getDrawable(R.mipmap.icon_bottom_blue_line);
        /// 这一步必须要做,否则不会显示.
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());

        mrecharge = findViewById(R.id.btn_recharge);
        mrecharge.setOnClickListener(this);

        mKhzlEntity = new KHZL_Entity();
        try {
            JSONObject jsonObject = new JSONObject(mDataString);
            JSONObject khzl = jsonObject.getJSONObject("khzl");
            mKhzlEntity.ID = khzl.getString("ID");
            mKhzlEntity.JGFS = khzl.getInt("JGFS");
            mKhzlEntity.KSFZ = khzl.getInt("KSFZ");
            mKhzlEntity.MF = khzl.getInt("MF");
            mKhzlEntity.NAME = khzl.getString("NAME");
            mKhzlEntity.ZT = khzl.getString("ZT");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mLxkList = new ArrayList<>();
    }

    private void initData() {
        tsp = new TheoryStudiedPager(this,mUserInfoEntiry,mKSXM.ID,mWdcj,mKSXM,mKhzlEntity,isBindingCard);
        aop = new ActualOperationPager(this,openid,mKSXM, mUserInfoEntiry.ZHYE,isBindingCard);
        mContainer.addView(tsp.getmView());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back://返回
                if(dataDane){
                    Intent intent = new Intent();
                    intent.putExtra("zjxx_id",mKSXM.ID);
                    intent.putExtra("zjxx_name",mKSXM.NAME);
                    intent.putExtra("zjxx_dm",mKSXM.DM);
                    intent.putExtra("zjxx_bz",mKSXM.BZ);
                    intent.putExtra("zjxx_zt",mKSXM.ZT);
                    intent.putExtra("zjxx_zlid",mKSXM.ZLID);
                    intent.putExtra("zjxx_ztl",mKSXM.ZTL);
                    intent.putExtra("zjxx_mnxh",mKSXM.MNXH);
                    intent.putExtra("zjxx_ctl",mKSXM.CTL);
                    intent.putExtra("zjxx_stxh",mKSXM.STXH);
                    intent.putExtra("zjxx_sxrq",mKSXM.SXRQ);
                    intent.putExtra("coin",mUserInfoEntiry.ZHYE);
                    setResult(1, intent);
                    finish();
                }
                break;
            case R.id.tv_theoretical://理论知识
                if (mContainer != null && tsp != null) {
                    mContainer.removeAllViews();
                    mContainer.addView(tsp.getmView());
                    mActual.setTextColor(getResources().getColor(R.color.clc_gray));
                    mActual.setCompoundDrawables(null, null, null, null);
                    mTheory.setTextColor(getResources().getColor(R.color.clc_black));
                    mTheory.setCompoundDrawables(null, null, null, drawable);
                }
                break;
            case R.id.tv_actual://实际操作
                if (mContainer != null && aop != null) {
                    mContainer.removeAllViews();
                    mContainer.addView(aop.getmView());
                    mActual.setTextColor(getResources().getColor(R.color.clc_black));
                    mActual.setCompoundDrawables(null, null, null, drawable);
                    mTheory.setTextColor(getResources().getColor(R.color.clc_gray));
                    mTheory.setCompoundDrawables(null, null, null, null);
                }
                break;
            case R.id.btn_recharge://充值
                Intent intent1 = new Intent();
                intent1.putExtra("openid",openid);
                intent1.putExtra("YHID",mUserInfoEntiry.ID);
                intent1.setClass(this, ChongZhiActivity.class);
                startActivity(intent1);
                break;
            case R.id.tv_binding_card://绑定学习卡
                //先判断是否已经绑定学习卡
                if(!isBindingCard){
                    Intent intent = new Intent();
                    intent.putExtra("openid",openid);
                    intent.putExtra("data_string",mDataString);
                    intent.putExtra("ismypage","01");
                    intent.putExtra("khzl_name",mKhzl.NAME);
                    intent.putExtra("ksxm_name",mKSXM.NAME);
                    intent.setClass(this, AddCardActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if(dataDane){
                Intent intent = new Intent();
                intent.putExtra("zjxx_id",mKSXM.ID);
                intent.putExtra("zjxx_name",mKSXM.NAME);
                intent.putExtra("zjxx_dm",mKSXM.DM);
                intent.putExtra("zjxx_bz",mKSXM.BZ);
                intent.putExtra("zjxx_zt",mKSXM.ZT);
                intent.putExtra("zjxx_zlid",mKSXM.ZLID);
                intent.putExtra("zjxx_ztl",mKSXM.ZTL);
                intent.putExtra("zjxx_mnxh",mKSXM.MNXH);
                intent.putExtra("zjxx_ctl",mKSXM.CTL);
                intent.putExtra("zjxx_stxh",mKSXM.STXH);
                intent.putExtra("zjxx_sxrq",mKSXM.SXRQ);
                intent.putExtra("coin",mUserInfoEntiry.ZHYE);
                setResult(1, intent);
                finish();
                return true;
            }else{
                return false;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    //从服务器获取数据
    private void getDataFromService() {
        alertDialog.show();//显示等待提示框

        //开始获取项目学习数据
        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(openid)
                .append("&xmid=")
                .append(xmid);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(Constants.ITEM_STUDIED_URL)
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: 获取项目学习数据失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.d(TAG, "onResponse: " + responseInfo);
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    String error = jsonObject.getString("error");
                    if (error.equals("false")) {//获取项目学习数据成功
                        Log.d(TAG, "onResponse: 获取项目学习数据成功");
                        nalysisData(responseInfo);//解析数据
                        getLxkList();//获取学习卡列表
                    } else if (error.equals("true")) {//获取项目学习数据失败
                        String message = jsonObject.getString("message");
                        Log.d(TAG, "onResponse: 获取项目学习数据失败--失败信息是：" + message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //解析服务器返回的数据
    private void nalysisData(String responseInfo) {
        try {
            JSONObject jsonObject = new JSONObject(responseInfo);
            JSONArray fllist = jsonObject.getJSONArray("fllist");//获取项目分类
            mXmflList = new ArrayList<>();
            for (int i = 0; i < fllist.length(); i++) {
                JSONObject fl_Object = fllist.getJSONObject(i);//项目分类list
                XMFL_Entity xe = new XMFL_Entity();
                xe.ID = fl_Object.getInt("ID");
                xe.XMID = fl_Object.getString("XMID");
                xe.FLNC = fl_Object.getString("FLNC");
                xe.CTS = fl_Object.getInt("CTS");
                xe.ZTL = fl_Object.getInt("ZTL");
                xe.ZFZ = fl_Object.getInt("ZFZ");
                xe.ZT = fl_Object.getString("ZT");
                xe.XSFZ = fl_Object.getInt("XSFZ");
                xe.ZZDTS = fl_Object.getInt("ZZDTS");
                xe.ZZDXH = fl_Object.getInt("ZZDXH");

                String s_tkxx = fl_Object.getString("TKXX");
                if (!s_tkxx.equals("null")) {
                    JSONObject tkxx = fl_Object.getJSONObject("TKXX");
                    xe.TKXX = new TKXX_Entity();
                    xe.TKXX.ID = tkxx.getString("ID");
                    xe.TKXX.XMID = tkxx.getString("XMID");
                    xe.TKXX.YHID = tkxx.getString("YHID");
                    xe.TKXX.FLID = tkxx.getString("FLID");
                    xe.TKXX.KSSJ = tkxx.getString("KSSJ");
                    xe.TKXX.JSSJ = tkxx.getString("JSSJ");
                    xe.TKXX.DQYS = tkxx.getInt("DQYS");
                    xe.TKXX.LX = tkxx.getString("LX");
                    xe.TKXX.ZT = tkxx.getString("ZT");
                    SimpleDateFormat sdf =   new SimpleDateFormat( "yyyyMMddHHmmss" );
                    String s = xe.TKXX.KSSJ.replaceAll("-", " ");
                    String s1 = s.replaceAll(":", " ");
                    Date date=sdf.parse( s1.replaceAll(" +","") );
                    long time = date.getTime();//有效期的时间
                    String key = xe.XMID + xe.FLNC;
                    SPUtils.put(this, key, time);
                }
                mXmflList.add(xe);
            }
            JSONObject ksxm_obj = jsonObject.getJSONObject("ksxm");
            mKSXM = new KSXM_Entity();
            mKSXM.ID = ksxm_obj.getString("ID");
            mKSXM.NAME = ksxm_obj.getString("NAME");
            mKSXM.DM = ksxm_obj.getString("DM");
            mKSXM.BZ = ksxm_obj.getString("BZ");
            mKSXM.ZT = ksxm_obj.getString("ZT");
            mKSXM.ZLID = ksxm_obj.getString("ZLID");
            mKSXM.ZTL = ksxm_obj.getInt("ZTL");
            mKSXM.MNXH = ksxm_obj.getInt("MNXH");
            mKSXM.CTL = ksxm_obj.getInt("CTL");
            mKSXM.STXH = ksxm_obj.getInt("STXH");
            JSONArray sclist = jsonObject.getJSONArray("sclist");//获取实际操作list
            mSjczList = new ArrayList<>();
            for (int i = 0; i < sclist.length(); i++) {
                JSONObject sc_Object = sclist.getJSONObject(i);
                SJCZ_Entity se = new SJCZ_Entity();
                se.ID = sc_Object.getString("ID");
                se.XMID = sc_Object.getString("XMID");
                se.TM = sc_Object.getString("TM");
                se.XH = sc_Object.getInt("XH");
                se.XSFZ = sc_Object.getInt("XSFZ");
                se.ZT = sc_Object.getString("ZT");

                String s_tkxx = sc_Object.getString("TKXX");
                if (!s_tkxx.equals("null")) {
                    JSONObject tkxx = sc_Object.getJSONObject("TKXX");
                    se.TKXX = new TKXX_Entity();
                    se.TKXX.ID = tkxx.getString("ID");
                    se.TKXX.XMID = tkxx.getString("XMID");
                    se.TKXX.YHID = tkxx.getString("YHID");
                    se.TKXX.FLID = tkxx.getString("FLID");
                    se.TKXX.KSSJ = tkxx.getString("KSSJ");
                    se.TKXX.JSSJ = tkxx.getString("JSSJ");
                    se.TKXX.DQYS = tkxx.getInt("DQYS");
                    se.TKXX.LX = tkxx.getString("LX");
                    se.TKXX.ZT = tkxx.getString("ZT");
                    SimpleDateFormat sdf =   new SimpleDateFormat( "yyyyMMddHHmmss" );
                    String s = se.TKXX.KSSJ.replaceAll("-", " ");
                    String s1 = s.replaceAll(":", " ");
                    Date date=sdf.parse( s1.replaceAll(" +","") );
                    long time = date.getTime();//有效期的时间
                    String key = se.ID;
                    SPUtils.put(this, key, time);
                }
                mSjczList.add(se);
            }
            String syzh = jsonObject.getString("syzh");//解析用户信息
            JSONObject syzh_obj = new JSONObject(syzh);
            mUserInfoEntiry = new UserInfoEntity();
            mUserInfoEntiry.ID = syzh_obj.getString("ID");
            mUserInfoEntiry.NC = syzh_obj.getString("NC");
            mUserInfoEntiry.SJH = syzh_obj.getString("SJH");
            mUserInfoEntiry.LX = syzh_obj.getString("LX");
            mUserInfoEntiry.ZHYE = syzh_obj.getInt("ZHYE");
            mUserInfoEntiry.SSS = syzh_obj.getString("SSS");
            mUserInfoEntiry.SHI = syzh_obj.getString("SHI");
            mUserInfoEntiry.ZHDLSJ = syzh_obj.getString("ZHDLSJ");
            mUserInfoEntiry.KH = syzh_obj.getString("KH");
            mUserInfoEntiry.ZCSJ = syzh_obj.getString("ZCSJ");
            mUserInfoEntiry.HEADIMGURL = syzh_obj.getString("HEADIMGURL");
            mUserInfoEntiry.GXSJ = syzh_obj.getString("GXSJ");
            mUserInfoEntiry.ZJXXXM = syzh_obj.getString("ZJXXXM");
            mUserInfoEntiry.WXCODE = syzh_obj.getString("WXCODE");
            mUserInfoEntiry.SQVIP = syzh_obj.getString("SQVIP");
            JSONObject wdcj_obj = jsonObject.getJSONObject("wdcj");//获取我的成绩
            mWdcj = new WDCJ_Entity();
            mWdcj.ID = wdcj_obj.getString("ID");
            mWdcj.XMID = wdcj_obj.getString("XMID");
            mWdcj.YHID = wdcj_obj.getString("YHID");
            mWdcj.LSGF = wdcj_obj.getInt("LSGF");
            mWdcj.PJF = wdcj_obj.getInt("PJF");
            mWdcj.MNCS = wdcj_obj.getInt("MNCS");
            mWdcj.JGCS = wdcj_obj.getInt("JGCS");
            mWdcj.ZQL = wdcj_obj.getInt("ZQL");
            mWdcj.WWCCS = wdcj_obj.getInt("WWCCS");
            mWdcj.ZTZS = wdcj_obj.getInt("ZTZS");
            mWdcj.ZQZS = wdcj_obj.getInt("ZQZS");
            mWdcj.GXSJ = wdcj_obj.getString("GXSJ");
            //获取模拟考试
            String mnks_s = jsonObject.getString("mnks");
            if(!mnks_s.equals("null")){
                JSONObject mnks = jsonObject.getJSONObject("mnks");
                mMnks = new MNKS_Entity();
                mMnks.ID = mnks.getString("ID");
                mMnks.XMID = mnks.getString("XMID");
                mMnks.YHID = mnks.getString("YHID");
                mMnks.KSSJ = mnks.getString("KSSJ");
                mMnks.JSSJ = mnks.getString("JSSJ");
                mMnks.FS = mnks.getInt("FS");
                mMnks.SFJL = mnks.getString("SFJL");
                mMnks.DTSL = mnks.getInt("DTSL");
                mMnks.ZTS = mnks.getInt("ZTS");
                mMnks.ZQSL = mnks.getInt("ZQSL");
//                mMnks.GXSJ = mnks.getString("GXSJ");


                SimpleDateFormat sdf =   new SimpleDateFormat( "yyyyMMddHHmmss" );
                String s = mMnks.KSSJ.replaceAll("-", " ");
                String s1 = s.replaceAll(":", " ");
                Date date=sdf.parse( s1.replaceAll(" +","") );
                long time = date.getTime();//有效期的时间
                String key = mMnks.XMID;
                SPUtils.put(this, key, time);
            }


            this.mLXL = wdcj_obj.getInt("LXL");//练题率
            JSONObject zl_obj = jsonObject.getJSONObject("zl");//获取证书种类
            mKhzl = new KHZL_Entity();
            mKhzl.ID = zl_obj.getString("ID");
            mKhzl.NAME = zl_obj.getString("NAME");
            mKhzl.KSFZ = zl_obj.getInt("KSFZ");
            mKhzl.JGFS = zl_obj.getInt("JGFS");
            mKhzl.MF = zl_obj.getInt("MF");
            mKhzl.ZT = zl_obj.getString("ZT");
            //解析出规定模拟考试次数
            MNCSBZ = jsonObject.getInt("mncsbz");
            //解析出规定正确率
            ZQLBZ = jsonObject.getInt("zqlbz");
            //解析出规定练体率
            LXLBZ = jsonObject.getInt("lxlbz");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 100://搜答案返回
                int mSearchTime = data.getIntExtra("mSearchTime",0);
                mUserInfoEntiry.ZHYE = mUserInfoEntiry.ZHYE - (mSearchTime * mKSXM.STXH );
                changeCoin(mUserInfoEntiry.ZHYE);
                break;
        }
    }

    /**
     * 获取学习卡列表
     */
    private void getLxkList() {
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(openid);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.XXK_URL)//学习卡列表
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 获取学习卡数据失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.i(TAG, "autoSignIn.onResponse: responseInfo===" + responseInfo);
                String error = null;
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//获取学习卡数据失败
                        String message = jsonObject.getString("message");
                        Log.i(TAG, "onResponse: message===" + message);
                    } else if (error.equals("false")) {//获取学习卡数据成功
                        analysisLxkData(responseInfo);//解析数据
                        Message msg = new Message();
                        msg.what = 0x01;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //解析学习卡数据
    private void analysisLxkData(String responseInfo) {
        try {
            JSONObject jsonObject = new JSONObject(responseInfo);
            JSONArray xxklist = jsonObject.getJSONArray("xxklist");
            for (int i = 0; i < xxklist.length(); i++) {
                JSONObject xxk = xxklist.getJSONObject(i);
                LXK_Entity le = new LXK_Entity();
                le.KH = xxk.getString("KH");
                le.MM = xxk.getString("MM");
                le.ZT = xxk.getString("ZT");
                le.BDSJ = xxk.getString("BDSJ");
                le.XXB = xxk.getInt("XXB");
                le.YHID = xxk.getString("YHID");
                le.LX = xxk.getString("LX");
                le.ZDTGY = xxk.getString("ZDTGY");
                le.YXTS = xxk.getInt("YXTS");
                le.XMID = xxk.getString("XMID");
                le.YXQ = xxk.getString("YXQ");
                le.STCS = xxk.getInt("STCS");
                mLxkList.add(le);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    /**
     * 减少金币值
     */
    public void changeCoin(int coin) {
        SPUtils.put(this,"COIN_NUM",coin);
        mUserInfoEntiry.ZHYE = coin;
        mLearnCoin.setText(coin + "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (aop != null) {
            aop.cleanView();
        }
        if (tsp != null) {
            tsp.cleanView();
        }
    }
}

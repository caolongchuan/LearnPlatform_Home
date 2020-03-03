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
import com.clc.learnplatform.entity.SJCZ_Entity;
import com.clc.learnplatform.entity.UserInfoEntity;
import com.clc.learnplatform.entity.WDCJ_Entity;
import com.clc.learnplatform.entity.XMFL_Entity;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.pager.ActualOperationPager;
import com.clc.learnplatform.pager.TheoryStudiedPager;
import com.clc.learnplatform.util.TTSUtils;
import com.clc.learnplatform.util.ToastUtil;

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

    private int mLXL;//练题率

    private AlertDialog alertDialog;//等待对话框
    private boolean dataDane;//用来表示数据是否加载完成 用于防止数据没有加载完时关闭发生的程序崩溃

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case 0x01://初始化界面数据
                    alertDialog.dismiss();//隐藏等待提示框
                    dataDane = true;
                    mMain.setVisibility(View.VISIBLE);//初始化完成数据 将所有控件全部显示出来
                    initUI();
                    break;
                case 0x02://定时更新实际操作的剩余时间
                    aop.updataTime();
                    break;
                case 0x03://未做题练习不到标准
                    //定义一个自己的dialog
                    WeiZuoTiDialog myDialog2new =
                            new WeiZuoTiDialog(StudiedActivity.this,mWdcj.MNCS,mLXL, mWdcj.ZQL);
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
    }

    private void initData() {
        tsp = new TheoryStudiedPager(this,mLXL,mUserInfoEntiry,mKSXM.ID,mWdcj,mKSXM);
        aop = new ActualOperationPager(this, mUserInfoEntiry.ZHYE);
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
                intent1.setClass(this, ChongZhiActivity.class);
                startActivity(intent1);
                break;
            case R.id.tv_binding_card://绑定学习卡
                Intent intent = new Intent();
                intent.putExtra("openid",openid);
                intent.putExtra("data_string",mDataString);
                intent.setClass(this, AddCardActivity.class);
                startActivity(intent);
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
                        Message msg = new Message();
                        msg.what = 0x01;
                        mHandler.sendMessage(msg);
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
                JSONObject fl_Object = fllist.getJSONObject(i);
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
            this.mLXL = wdcj_obj.getInt("LXL");//练题率
            JSONObject zl_obj = jsonObject.getJSONObject("zl");//获取证书种类
            mKhzl = new KHZL_Entity();
            mKhzl.ID = zl_obj.getString("ID");
            mKhzl.NAME = zl_obj.getString("NAME");
            mKhzl.KSFZ = zl_obj.getInt("KSFZ");
            mKhzl.JGFS = zl_obj.getInt("JGFS");
            mKhzl.MF = zl_obj.getInt("MF");
            mKhzl.ZT = zl_obj.getString("ZT");
        } catch (JSONException e) {
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
     * 减少金币值
     */
    public void changeCoin(int coin) {
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

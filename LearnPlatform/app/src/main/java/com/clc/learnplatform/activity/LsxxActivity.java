package com.clc.learnplatform.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.clc.learnplatform.R;
import com.clc.learnplatform.adapter.HomeItemAdapter;
import com.clc.learnplatform.entity.KSXM_Entity;
import com.clc.learnplatform.entity.UserInfoEntity;
import com.clc.learnplatform.global.Constants;

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
 * 历史学习
 */
public class LsxxActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "LsxxActivity";
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private String openid;
    private UserInfoEntity mUserInfoEntiry;//用户的信息
    private ArrayList<KSXM_Entity> mKsxmList;//项目list

    private  ImageView ivBack;
    private ImageView ivHead;
    private TextView ivName;
    private TextView ivPhoneNum;
    private TextView ivCoinNum;
    private TextView tvRecharge;//充值
    private ListView mListView;
    private HomeItemAdapter mHomeItemAdapter;
    private ArrayList<String> mHomeItemName;//项目的一级分类

    private KSXM_Entity mKSXM = null;

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case 0x01://从服务器获取数据完成
                    initUI();
                    mHomeItemAdapter.notifyDataSetChanged();
                    break;
            }
            return false;
        }
    });

    //从服务器获取完数据以后更新UI
    private void initUI() {
        Glide.with(this).load((String) mUserInfoEntiry.HEADIMGURL).into(ivHead);//显示头像
        ivName.setText(mUserInfoEntiry.NC);//显示昵称
        ivPhoneNum.setText(mUserInfoEntiry.SJH);//显示手机号码
        ivCoinNum.setText(String.valueOf(mUserInfoEntiry.ZHYE));//显示学习币数量

        mHomeItemName = new ArrayList<>();
        //全部项目
        for (int i = 0; i < mKsxmList.size(); i++) {
            String bz = mKsxmList.get(i).BZ;
            int j = 0;
            for (; j < mHomeItemName.size(); j++) {
                if (bz != null && bz.equals(mHomeItemName.get(j))) {
                    break;
                }
            }
            if (j>=mHomeItemName.size()){
                mHomeItemName.add(mKsxmList.get(i).BZ);
            }
        }
        mHomeItemAdapter = new HomeItemAdapter(this, mHomeItemName,mKsxmList,openid);
        mListView.setAdapter(mHomeItemAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lsxx);
        //隐藏标题栏,有效
        getSupportActionBar().hide();
        Intent intent = getIntent();
        openid = intent.getStringExtra("openid");

        initView();
        getDataFromService();
    }

    //从服务器获取数据
    private void getDataFromService() {
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(openid);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.LSXX_URL)//历史学习接口
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 获取历史学习失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.i(TAG, "autoSignIn.onResponse: responseInfo===" + responseInfo);
                String error = null;
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//失败
                        String message = jsonObject.getString("message");
                        Log.i(TAG, "onResponse: message===" + message);
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
            //解析用户信息
            String syzh = jsonObject.getString("syzh");
            JSONObject syzh_obj = new JSONObject(syzh);
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
            //解析项目列表
            JSONArray ksxmlist = jsonObject.getJSONArray("xmlist");
            for (int i = 0; i < ksxmlist.length(); i++) {
                JSONObject ksxm_obj = ksxmlist.getJSONObject(i);
                KSXM_Entity ke = new KSXM_Entity();
                ke.ID = ksxm_obj.getString("ID");
                ke.BZ = ksxm_obj.getString("BZ");
                ke.CTL = ksxm_obj.getInt("CTL");
                ke.DM = ksxm_obj.getString("DM");
                ke.MNXH = ksxm_obj.getInt("MNXH");
                ke.NAME = ksxm_obj.getString("NAME");
                ke.STXH = ksxm_obj.getInt("STXH");
                ke.ZLID = ksxm_obj.getString("ZLID");
                ke.ZT = ksxm_obj.getString("ZT");
                ke.ZTL = ksxm_obj.getInt("ZTL");
                mKsxmList.add(ke);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void initView() {
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
        ivHead = findViewById(R.id.iv_user_head);
        ivName = findViewById(R.id.tv_user_name);
        ivPhoneNum = findViewById(R.id.tv_phone_number);
        ivCoinNum = findViewById(R.id.tv_learn_b);
        tvRecharge = findViewById(R.id.btn_recharge);
        tvRecharge.setOnClickListener(this);
        mListView = findViewById(R.id.lv_item);

        mUserInfoEntiry = new UserInfoEntity();
        mKsxmList = new ArrayList<>();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.iv_back://返回
                if(mKSXM!=null){
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
                }else{
                    finish();
                }
                break;
            case R.id.btn_recharge://充值
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if(mKSXM!=null){
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
            }else{
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 100:
                KSXM_Entity ke = new KSXM_Entity();
                ke.ID = data.getStringExtra("zjxx_id");
                ke.NAME = data.getStringExtra("zjxx_name");
                ke.DM = data.getStringExtra("zjxx_dm");
                ke.BZ = data.getStringExtra("zjxx_bz");
                ke.ZT = data.getStringExtra("zjxx_zt");
                ke.ZLID = data.getStringExtra("zjxx_zlid");
                ke.ZTL = data.getIntExtra("zjxx_ztl", 0);
                ke.MNXH = data.getIntExtra("zjxx_mnxh", 0);
                ke.CTL = data.getIntExtra("zjxx_ctl", 0);
                ke.STXH = data.getIntExtra("zjxx_stxh", 0);
                ke.SXRQ = data.getIntExtra("zjxx_sxrq", 0);
                int coin = data.getIntExtra("coin", 0);
                mKSXM = ke;
                mUserInfoEntiry.ZHYE = coin;

                ivCoinNum.setText(String.valueOf(coin));
                break;
        }
    }
}

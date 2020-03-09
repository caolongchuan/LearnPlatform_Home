package com.clc.learnplatform.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clc.learnplatform.R;
import com.clc.learnplatform.entity.SJCZ_Entity;
import com.clc.learnplatform.entity.UserInfoEntity;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.SPUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 实际学习
 */
public class ActualActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "ActualActivity";
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private String openid;
    private String sjczid;
    private String xmid;

    private SJCZ_Entity mSjczEntity;//实际操作实体
    private UserInfoEntity mUser;//用户实体

    private ImageView ivBack;
    private TextView tvSjczTitle;//实际操作标题
    private TextView tvSjczNr;//实际操作内容

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case 0x01://获取数据成功
                    //更新账户余额（也就是学习币数量）
                    SPUtils.put(getApplicationContext(),"COIN_NUM",mUser.ZHYE);
                    tvSjczTitle.setText(mSjczEntity.TM);
                    tvSjczNr.setText(mSjczEntity.NR);
                    break;
            }
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actual);
        //隐藏标题栏,有效
        getSupportActionBar().hide();

        Intent intent = getIntent();
        openid = intent.getStringExtra("openid");
        sjczid = intent.getStringExtra("sjczid");
        xmid = intent.getStringExtra("xmid");

        initView();
        getDataFromService();
    }

    /**
     * 从服务器后台获取实际操作数据
     */
    private void getDataFromService() {
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(openid)
                .append("&id=")
                .append(sjczid)
                .append("&xmid=")
                .append(xmid);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.XX_SJCZ_URL)//实际操作查看
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 获取实际操作数据失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.i(TAG, "onResponse: responseInfo===" + responseInfo);
                String error = null;
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//失败

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

    /**
     * 解析数据
     * @param responseInfo
     */
    private void analysisData(String responseInfo) {
        try {
            JSONObject jsonObject = new JSONObject(responseInfo);
            //解析实际操作实体
            JSONObject sjcz = jsonObject.getJSONObject("sjcz");
            mSjczEntity.ID = sjcz.getString("ID");
            mSjczEntity.XMID = sjcz.getString("XMID");
            mSjczEntity.TM = sjcz.getString("TM");
            mSjczEntity.XH = sjcz.getInt("XH");
            mSjczEntity.NR = sjcz.getString("NR");
            mSjczEntity.XSFZ = sjcz.getInt("XSFZ");
            mSjczEntity.ZT = sjcz.getString("ZT");
            //解析用户实体(主要是账户余额）
            JSONObject syzh = jsonObject.getJSONObject("syzh");
            mUser.ZHYE = syzh.getInt("ZHYE");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        ivBack= findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
        tvSjczTitle= findViewById(R.id.tv_sjcz_title);
        tvSjczNr= findViewById(R.id.tv_sjcz_nr);

        mSjczEntity = new SJCZ_Entity();
        mUser = new UserInfoEntity();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_back://返回
                finish();
                break;
        }
    }
}

package com.clc.learnplatform.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.clc.learnplatform.R;
import com.clc.learnplatform.baidu.LocationUtil;
import com.clc.learnplatform.entity.KHZL_Entity;
import com.clc.learnplatform.entity.KSXM_Entity;
import com.clc.learnplatform.entity.UserInfoEntity;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.LocationUtils;
import com.clc.learnplatform.util.NetWorkUtil;
import com.clc.learnplatform.util.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 登陆
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    public static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private static final String TAG = "--LoginActivity--";
    private TextView mCityName;//所在城市名
    private TextView mWXname;//微信昵称

    private EditText mEtYqm;//邀请码
    private EditText mEtPhoneNo;//手机号
    private EditText mEtSms_Yzm;//短信验证码

    private Button mBtnGetXZM;//获取验证码按钮
    private Button mBtnDoIt;//立即体验按钮

    private RelativeLayout mRlPb;

    private String mSmsYzm = null;

    LocationUtil lu;

    private String province;//省份名称
    private String city;//城市名称
    private String nickname;//昵称
    private String headimgurl;//头像图片url
    private String openid;

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0x01://获取短信验证码
                    mSmsYzm = msg.getData().getString("yzm");
                    Log.e(TAG, "handleMessage-mSmsYzm: " + mSmsYzm);
                    break;
                case 0x02://获取到城市
                    province = msg.getData().getString("province");
                    city = msg.getData().getString("city");
                    String addrString = province + " " + city;
                    mCityName.setText(addrString);
                    Log.e(TAG, "handleMessage-addrString: " + msg.getData().getString("addrString"));
                    break;
                case 0x03://该微信号未注册 请先注册！
                    ToastUtil.getInstance().shortShow("该微信号未注册 请先注册！");
                    mRlPb.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //方式二：这句代码必须写在setContentView()方法的前面
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        nickname = (String) bundle.get("nickname");
        headimgurl = (String) bundle.get("headimgurl");
        openid = (String) bundle.get("openid");

        initView();
        //自动登录
        autoSignIn(openid);
        initData();
    }

    //自动登录
    private void autoSignIn(final String openid) {
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        sb.append("openid=").append(openid);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.FIRST_PAGER_URL)//首页URL
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
                    Thread.sleep(1000);//先歇两秒
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//登录失败
                        String message = jsonObject.getString("message");
                        Message msg = new Message();
                        msg.what = 0x03;
                        mHandler.sendMessage(msg);
                        Log.i(TAG, "onResponse: message===" + message);
                    } else if (error.equals("false")) {//登录成功
                        gotoMainActivity(openid,responseInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initView() {
        mCityName = findViewById(R.id.tv_user_addr);
        mWXname = findViewById(R.id.tv_user_name);

        mEtYqm = findViewById(R.id.et_yqm);
        mEtPhoneNo = findViewById(R.id.et_phone_number);
        mEtSms_Yzm = findViewById(R.id.et_get_yzm);
        mBtnGetXZM = findViewById(R.id.bte_get_xzm);
        mBtnGetXZM.setOnClickListener(this);
        mBtnDoIt = findViewById(R.id.btn_doit);
        mBtnDoIt.setOnClickListener(this);

        mRlPb = findViewById(R.id.rl_pb);
    }

    private void initData() {
        lu = new LocationUtil(this);//用百度地图获取省份与城市
        mWXname.setText(nickname);
    }

    /**
     * 跳转到主页面
     */
    private void gotoMainActivity( String openid,String jsonString) {
        Intent intent = new Intent();
        intent.putExtra("openid", openid);
        intent.putExtra("data_json_string",jsonString);
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 根据输入的电话号码获取短信验证码
     *
     * @param phone_nomber 手机号码
     * @return 短信验证码
     */
    private void getSMS_YZM(final String phone_nomber) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String httpUrl = Constants.GET_SMS_YZM_URL + phone_nomber;
                StringBuilder resultData = new StringBuilder();
                URL url = null;
                try {
                    url = new URL(httpUrl);
                } catch (MalformedURLException e) {
                    System.out.println(e.getMessage());
                }
                if (url != null) {
                    try {
                        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                        InputStreamReader in = new InputStreamReader(urlConn.getInputStream());
                        BufferedReader buffer = new BufferedReader(in);
                        String inputLine = null;
                        while (((inputLine = buffer.readLine()) != null)) {
                            resultData.append(inputLine).append("\n");
                        }
                        in.close();
                        urlConn.disconnect();
                        if (resultData != null) {
                            JSONObject jo = new JSONObject(resultData.toString());
                            String error = jo.getString("error");
                            if (error.equals("false")) {
                                String yzm = jo.getString("yzm");
                                Message msg = new Message();
                                msg.what = 0x01;
                                Bundle bundle = new Bundle();
                                bundle.putString("yzm", yzm);
                                msg.setData(bundle);
                                mHandler.sendMessage(msg);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "getSMS_YZM: " + e.toString());
                    }
                }
            }
        }).start();
    }

    //注册
    private void Register() {
        String yqm = mEtYqm.getText().toString();//获取验证码
        if (yqm.equals("")) {
            Toast.makeText(LoginActivity.this, "邀请码不能为空", Toast.LENGTH_SHORT).show();
            return;
        } else {
            String sms_yzm = mEtSms_Yzm.getText().toString();
            if (mSmsYzm != null && mSmsYzm.equals(sms_yzm)) {//验证成功
//                gotoMainActivity();
            } else {
                Toast.makeText(LoginActivity.this, "验证码不正确", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String sjh = mEtPhoneNo.getText().toString();
        if (sjh.equals("")) {
            Toast.makeText(LoginActivity.this, "手机号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        //开始注册
        //获取授权
        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(openid)
                .append("&sjh=")
                .append(sjh)
                .append("&sheng=")
                .append(province)
                .append("&shi=")
                .append(city)
                .append("&yqm=")
                .append(yqm)
                .append("&nc=")
                .append(nickname)
                .append("&tx=")
                .append(headimgurl);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(Constants.REGISTER_URL)
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: 注册失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.d(TAG, "onResponse: " + responseInfo);
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    String error = jsonObject.getString("error");
                    if (error.equals("false")) {//注册成功
                        Log.d(TAG, "onResponse: 注册成功");
                        //注册成功后自动登录
                        autoSignIn(openid);
                    } else if (error.equals("true")) {//注册失败
                        String message = jsonObject.getString("message");
                        Log.d(TAG, "onResponse: 注册失败--失败信息是：" + message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bte_get_xzm://获取验证码
                String phone_number = mEtPhoneNo.getText().toString();
                if (phone_number.equals("")) {
                    Toast.makeText(LoginActivity.this, "请输入手机号码", Toast.LENGTH_SHORT).show();
                } else {
                    getSMS_YZM(phone_number);//获取验证码
                }
                break;
            case R.id.btn_doit://立即体验 也就是注册
                Register();
                break;
        }
    }
}

package com.clc.learnplatform.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.clc.learnplatform.R;
import com.clc.learnplatform.entity.UserInfoEntity;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.ToastUtil;

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

public class TiYanCardActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "TiYanCardActivity";
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private String openid;

    private ImageView ivBack;
    private TextView tvCardNum;
    private TextView tvCardPassword;
    private Button btnOk;

    private UserInfoEntity mUser = null;

    private AlertDialog alertDialog;//等待对话框

    private int bangkazhuangtai = 0;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case 0x01://绑定成功
                    alertDialog.dismiss();
                    bangkazhuangtai = 1;
                    ToastUtil.getInstance().shortShow("绑定成功");
                    break;
                case 0x02://绑定失败
                    alertDialog.dismiss();
                    Bundle data = message.getData();
                    String msg = data.getString("msg");
                    ToastUtil.getInstance().shortShow(msg);
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ti_yan_card);

        //隐藏标题栏,有效
        getSupportActionBar().hide();

        Intent intent = getIntent();
        openid = intent.getStringExtra("openid");

        initView();
    }

    private void initView() {
        alertDialog = new AlertDialog
                .Builder(this).setMessage("请稍后...")
                .create();

        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
        tvCardNum = findViewById(R.id.et_card_num);
        tvCardPassword = findViewById(R.id.et_card_password);
        btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_back://返回
                Intent intent = new Intent();
                setResult(bangkazhuangtai, intent);
                finish();
                break;
            case R.id.btn_ok:
                doOk();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent intent = new Intent();
            setResult(bangkazhuangtai, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void doOk() {
        String cardNum = tvCardNum.getText().toString();
        if(cardNum.equals("")){
            ToastUtil.getInstance().shortShow("卡号不能为空");
            return;
        }
        String cardPassword = tvCardPassword.getText().toString();
        if(cardPassword.equals("")){
            ToastUtil.getInstance().shortShow("卡密不能为空");
            return;
        }

        alertDialog.show();
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(openid)
                .append("&KH=")
                .append(cardNum)
                .append("&MM=")
                .append(cardPassword);
        String s = sb.toString();
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, s);
        final Request request = new Request.Builder()
                .url(Constants.BGTYK_URL)//绑定体验卡
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 绑定失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.i(TAG, "autoSignIn.onResponse: responseInfo===" + responseInfo);
                String error = null;
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//绑定失败
                        String message = jsonObject.getString("message");
                        Message msg = new Message();
                        msg.what = 0x02;
                        Bundle bundle = new Bundle();
                        bundle.putString("msg",message);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);//通知UI线程更新界面
                        Log.i(TAG, "onResponse: message===" + message);
                    } else if (error.equals("false")) {//绑定成功
                        analysisData(responseInfo);//解析数据
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void analysisData(String responseInfo) {
        try {
            JSONObject jsonObject = new JSONObject(responseInfo);
            JSONObject syzh = jsonObject.getJSONObject("syzh");
            mUser = new UserInfoEntity();
            mUser.ID = syzh.getString("ID");
            mUser.NC = syzh.getString("NC");
            mUser.SJH = syzh.getString("SJH");
            mUser.LX = syzh.getString("LX");
            mUser.ZHYE = syzh.getInt("ZHYE");
            mUser.SSS = syzh.getString("SSS");
            mUser.SHI = syzh.getString("SHI");
            mUser.ZHDLSJ = syzh.getString("ZHDLSJ");
            mUser.KH = syzh.getString("KH");
            mUser.ZCSJ = syzh.getString("ZCSJ");
            mUser.HEADIMGURL = syzh.getString("HEADIMGURL");
            mUser.GXSJ = syzh.getString("GXSJ");
            mUser.ZJXXXM = syzh.getString("ZJXXXM");
            mUser.WXCODE = syzh.getString("WXCODE");
            mUser.SQVIP = syzh.getString("SQVIP");

            Message msg = new Message();
            msg.what = 0x01;
            mHandler.sendMessage(msg);//通知UI线程更新界面
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

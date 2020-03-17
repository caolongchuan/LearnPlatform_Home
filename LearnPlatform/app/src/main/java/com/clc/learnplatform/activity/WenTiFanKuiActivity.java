package com.clc.learnplatform.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.clc.learnplatform.R;
import com.clc.learnplatform.entity.WTFK_Entity;
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

/**
 * 问题反馈
 */
public class WenTiFanKuiActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "WenTiFanKuiActivity";
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private String openid;

    private ImageView ivBack;
    private Button btnOk;
    private Button btnCancle;

    private EditText etTitle;
    private EditText etPhoneNum;
    private EditText etNeiRong;

    private AlertDialog alertDialog;//等待对话框

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0x01:
//                    alertDialog.dismiss();
                    ToastUtil.getInstance().shortShow(msg.getData().getString("message"));
                    finish();
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wen_ti_fan_kui);
        //隐藏标题栏,有效
        getSupportActionBar().hide();

        Intent intent = getIntent();
        openid = intent.getStringExtra("openid");
        initView();
        initData();
    }

    private void initData() {

    }

    private void initView() {
        alertDialog = new AlertDialog
                .Builder(this).setMessage("正在提交，请稍等...")
                .create();
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
        btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(this);
        btnCancle = findViewById(R.id.btn_cancle);
        btnCancle.setOnClickListener(this);

        etTitle = findViewById(R.id.et_title);
        etPhoneNum = findViewById(R.id.et_phone_number);
        etNeiRong = findViewById(R.id.et_fankui_msg);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_ok:
                String s = etTitle.getText().toString();
                String s1 = etPhoneNum.getText().toString();
                String s2 = etNeiRong.getText().toString();
                if (s.equals("")) {
                    ToastUtil.getInstance().shortShow("标题不能为空");
                    return;
                }
                if (s1.equals("")) {
                    ToastUtil.getInstance().shortShow("手机号不能为空");
                    return;
                }
                if (s2.equals("")) {
                    ToastUtil.getInstance().shortShow("内容不能为空");
                    return;
                }
                doTiJiao(s, s1, s2);

                break;
            case R.id.btn_cancle:
                finish();
                break;
        }
    }

    //提交问题反馈
    private void doTiJiao(String bt, String sjh, String content) {
//        alertDialog.show();
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(openid)
                .append("&sjh=")
                .append(sjh)
                .append("&bt=")
                .append(bt)
                .append("&content=")
                .append(content);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.WTFK_URL)//问题反馈接口
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 问题反馈失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.i(TAG, "autoSignIn.onResponse: responseInfo===" + responseInfo);
                String error = null;
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//问题反馈失败
                        String message = jsonObject.getString("message");
                        Message msg = new Message();
                        msg.what = 0x01;
                        Bundle bundle = new Bundle();
                        bundle.putString("message", message);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                        Log.i(TAG, "onResponse: message===" + message);
                    } else if (error.equals("false")) {//问题反馈成功
                        String message = jsonObject.getString("message");
                        Message msg = new Message();
                        msg.what = 0x01;
                        Bundle bundle = new Bundle();
                        bundle.putString("message", message);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                        Log.i(TAG, "onResponse: message===" + message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }
}

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
import android.widget.CheckBox;
import android.widget.ImageView;

import com.clc.learnplatform.R;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.ToastUtil;

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
 * 申请VIP
 */
public class VIPActivity extends AppCompatActivity {
    private final String TAG = "WenTiFanKuiActivity";
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private String openid;

    private ImageView ivBack;
    private CheckBox mOk;
    private Button btnOk;

    private AlertDialog alertDialog;//等待对话框

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case 0x01:
                    Bundle data = msg.getData();
                    String message = data.getString("message");
                    ToastUtil.getInstance().shortShow(message);
//                    alertDialog.dismiss();
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip);
        //隐藏标题栏,有效
        getSupportActionBar().hide();

        Intent intent = getIntent();
        openid = intent.getStringExtra("openid");
        initView();
    }

    private void initView() {
        alertDialog = new AlertDialog
                .Builder(this).setMessage("正在申请，请稍等...")
                .create();

        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mOk = findViewById(R.id.cb_ok);
        btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOk.isChecked()){
                    doit();
                }else{
                    ToastUtil.getInstance().shortShow("请确认已阅读以上说明，并选中阅读复选框");
                }
            }
        });
    }

    private void doit() {
//        alertDialog.show();
        OkHttpClient okHttpClient = new OkHttpClient();

        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(openid);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.WTFK_URL)//问题反馈接口
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
                Log.i(TAG, "onResponse: responseInfo===" + responseInfo);
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    String message = jsonObject.getString("message");
                    Message msg = new Message();
                    msg.what = 0x01;
                    Bundle bundle = new Bundle();
                    bundle.putString("message", message);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


}

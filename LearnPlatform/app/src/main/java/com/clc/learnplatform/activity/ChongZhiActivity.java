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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.clc.learnplatform.R;
import com.clc.learnplatform.entity.XTCS_Entity;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.Clc_WXPayUtil;
import com.clc.learnplatform.util.IPUtil;

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
 * 充值
 */
public class ChongZhiActivity extends AppCompatActivity implements View.OnClickListener {
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private static final String TAG = "ChongZhiActivity";

    private String openid;
    private int choiceIndex = -1;

    private ArrayList<XTCS_Entity> mXtcsList;

    private ImageView ivBack;
    private RadioGroup rgMain;
    private TextView tvAdd;
    private TextView tvJian;
    private TextView tvNum;

    private TextView tvCoinNum;
    private TextView tvPrice;

    private Button btnPay;

    private AlertDialog alertDialog;//等待对话框
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case 0x01://从服务器获取到数据解析后更新
                    alertDialog.dismiss();
                    for (int i = 0; i < mXtcsList.size(); i++) {
                        RadioButton rb = new RadioButton(ChongZhiActivity.this);
                        rb.setId(i);
                        rb.setText(mXtcsList.get(i).NR);
                        rgMain.addView(rb);
                    }
                    break;
            }
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chong_zhi);
        //隐藏标题栏,有效
        getSupportActionBar().hide();
        Intent intent = getIntent();
        openid = intent.getStringExtra("openid");

        initView();
        getDataFromService();
    }

    private void initView() {
        alertDialog = new AlertDialog
                .Builder(this).setMessage("正在加载数据...")
                .create();

        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
        rgMain = findViewById(R.id.rg_main);

        tvAdd = findViewById(R.id.tv_add);
        tvAdd.setOnClickListener(this);
        tvJian = findViewById(R.id.tv_jian);
        tvJian.setOnClickListener(this);
        tvNum = findViewById(R.id.tv_num);
        tvCoinNum = findViewById(R.id.tv_coin_num);
        tvPrice = findViewById(R.id.tv_price);
        btnPay = findViewById(R.id.btn_pay);
        btnPay.setOnClickListener(this);

        mXtcsList = new ArrayList<>();
        rgMain.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                choiceIndex = checkedId;
                String num = tvNum.getText().toString();
                int integer = Integer.valueOf(num);
                int i = (mXtcsList.get(choiceIndex).LTCS * integer) + (mXtcsList.get(choiceIndex).ZSLTCS * integer);
                int i2 = mXtcsList.get(choiceIndex).FY * integer;
                tvCoinNum.setText(String.valueOf(i));
                tvPrice.setText(String.valueOf(i2));
            }
        });
    }

    private void getDataFromService() {
        alertDialog.show();

        //获取账单明细数据
        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(openid);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(Constants.CZ_URL)
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: 获取充值套餐数据失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.d(TAG, "onResponse: " + responseInfo);
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    String error = jsonObject.getString("error");
                    if (error.equals("false")) {//获取项目学习数据成功
                        Log.d(TAG, "onResponse: 获取充值套餐数据成功");
                        nalysisData(responseInfo);//解析数据
                        Message msg = new Message();
                        msg.what = 0x01;
                        mHandler.sendMessage(msg);
                    } else if (error.equals("true")) {//获取项目学习数据失败
                        String message = jsonObject.getString("message");
                        Log.d(TAG, "onResponse: 获取充值套餐数据失败--失败信息是：" + message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    //解析数据
    private void nalysisData(String responseInfo) {
        try {
            JSONObject jsonObject = new JSONObject(responseInfo);
            JSONArray cztclist = jsonObject.getJSONArray("cztclist");
            for (int i = 0; i < cztclist.length(); i++) {
                JSONObject cztc = cztclist.getJSONObject(i);
                XTCS_Entity xe = new XTCS_Entity();
                xe.BM = cztc.getString("BM");
                xe.FY = cztc.getInt("FY");
                xe.ID = cztc.getString("ID");
                xe.LTCS = cztc.getInt("LTCS");
                xe.NR = cztc.getString("NR");
                xe.XGSJ = cztc.getString("XGSJ");
                xe.ZSLTCS = cztc.getInt("ZSLTCS");
                mXtcsList.add(xe);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_add:
                String num = tvNum.getText().toString();
                Integer integer = Integer.valueOf(num);
                tvNum.setText(String.valueOf(integer + 1));
                String num11 = tvNum.getText().toString();
                int integer11 = Integer.valueOf(num11);
                int i = (mXtcsList.get(choiceIndex).LTCS * integer11)+(mXtcsList.get(choiceIndex).ZSLTCS * integer11);
                int i2 = mXtcsList.get(choiceIndex).FY * integer11;
                tvCoinNum.setText(String.valueOf(i));
                tvPrice.setText(String.valueOf(i2));
                break;
            case R.id.tv_jian:
                String num1 = tvNum.getText().toString();
                Integer integer1 = Integer.valueOf(num1);
                if(integer1>=2){
                    tvNum.setText(String.valueOf(integer1 - 1));
                    String num111 = tvNum.getText().toString();
                    int integer111 = Integer.valueOf(num111);
                    int ii = (mXtcsList.get(choiceIndex).LTCS * integer111)+(mXtcsList.get(choiceIndex).ZSLTCS * integer111);
                    int ii2 = mXtcsList.get(choiceIndex).FY * integer111;
                    tvCoinNum.setText(String.valueOf(ii));
                    tvPrice.setText(String.valueOf(ii2));
                }
                break;
            case R.id.btn_pay://支付
                doPay();
                break;
        }
    }

    /**
     * 支付
     */
    private void doPay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String netIp = IPUtil.GetNetIp();
                Log.i(TAG, "doPay: ip="+netIp);
                Clc_WXPayUtil.TongYiXiaDan(100,netIp);
            }
        }).start();
    }


}

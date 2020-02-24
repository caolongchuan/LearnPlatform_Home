package com.clc.learnplatform.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.clc.learnplatform.R;
import com.clc.learnplatform.util.ToastUtil;

/**
 * 申请VIP
 */
public class VIPActivity extends AppCompatActivity {

    private String openid;

    private ImageView ivBack;
    private CheckBox mOk;
    private Button btnOk;

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
        ToastUtil.getInstance().shortShow("申请VIP");

    }
}

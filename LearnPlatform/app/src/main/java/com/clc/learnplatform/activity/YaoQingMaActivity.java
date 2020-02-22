package com.clc.learnplatform.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.clc.learnplatform.R;

public class YaoQingMaActivity extends AppCompatActivity {

    private ImageView ivBack;
    private ImageView ivHead;
    private TextView tvID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yao_qing_ma);
        //隐藏标题栏,有效
        getSupportActionBar().hide();

        initView();
        initData(getIntent());
    }

    private void initData(Intent intent) {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Glide.with(this).load((String) intent.getStringExtra("head")).into(ivHead);//显示头像
        tvID.setText("ID:"+intent.getStringExtra("id"));
    }

    private void initView() {
        ivBack = findViewById(R.id.iv_back);
        ivHead = findViewById(R.id.iv_head);
        tvID = findViewById(R.id.tv_id);

    }
}

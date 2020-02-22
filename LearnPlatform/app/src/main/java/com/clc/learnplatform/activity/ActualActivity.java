package com.clc.learnplatform.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clc.learnplatform.R;

/**
 * 实际学习
 */
public class ActualActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivBack;
    private TextView mTest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actual);
        //隐藏标题栏,有效
        getSupportActionBar().hide();

        Intent intent = getIntent();
        String test = intent.getStringExtra("TTSUtileLisenning");
        mTest = findViewById(R.id.tv_test);
        mTest.setText(test);

        initView();
    }

    private void initView() {
        ivBack= findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
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

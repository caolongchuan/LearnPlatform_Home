package com.clc.learnplatform.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.clc.learnplatform.R;

/**
 * 查看分数
 */
public class SeeFractionActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mIvBack;
    private TextView mTvScroe;
    private TextView mTvItemName;
    private TextView mTvItemType;
    private TextView mTvStartTime;
    private TextView mTvEndTime;
    private Button mStudy;

    private String mScroe;//分数
    private String mItemName;//项目名称
    private String mItemType;//项目代号
    private String mStartTime;//开始时间
    private String mEndTime;//结束时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_fraction);
        //隐藏标题栏,有效
        getSupportActionBar().hide();

        initView();
        initData(getIntent());
    }

    private void initData(Intent intent) {
        mScroe = String.valueOf(intent.getIntExtra("scroe",0));
        mItemName = intent.getStringExtra("item_name");
        mItemType = intent.getStringExtra("item_type");
        mStartTime = intent.getStringExtra("start_time");
        mEndTime = intent.getStringExtra("end_time");

        mTvScroe.setText(mScroe);
        mTvItemName.setText(mItemName);
        mTvItemType.setText(mItemType);
        mTvStartTime.setText(mStartTime);
        mTvEndTime.setText(mEndTime);

    }

    private void initView() {
        mIvBack = findViewById(R.id.iv_back);
        mTvScroe = findViewById(R.id.tv_score);
        mTvItemName = findViewById(R.id.tv_item_name);
        mTvItemType = findViewById(R.id.tv_item_type);
        mTvStartTime = findViewById(R.id.tv_start_time);
        mTvEndTime = findViewById(R.id.tv_end_time);
        mStudy= findViewById(R.id.btn_study);
        mStudy.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_back://返回
                break;
            case R.id.btn_study://学习
                finish();
            break;
        }
    }
}

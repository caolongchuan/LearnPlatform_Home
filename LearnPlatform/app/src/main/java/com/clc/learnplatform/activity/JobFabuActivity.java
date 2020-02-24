package com.clc.learnplatform.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clc.learnplatform.R;
import com.clc.learnplatform.util.ToastUtil;

import okhttp3.MediaType;

public class JobFabuActivity extends AppCompatActivity implements View.OnClickListener  {
    private final String TAG = "JobFabuActivity";
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private String openid;

    private ImageView mBack;
    private TextView mFabuZpxx;//发布招聘信息
    private TextView mFabuQzxx;//发布求职信息
    private Button mFabu;//发布

    private boolean mZpOrQz;//标示是招聘还是求职 false为招聘 true为求职
    private LinearLayout mLlZpxx;//招聘信息编辑的布局
    private LinearLayout mLlQzxx;//求职信息编辑的布局

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_fabu);
        //隐藏标题栏,有效
        getSupportActionBar().hide();

        Intent intent = getIntent();
        openid = intent.getStringExtra("openid");

        initView();
        initData();
    }

    private void initData() {
        mZpOrQz = false;
    }

    private void initView() {
        mBack = findViewById(R.id.iv_back);
        mBack.setOnClickListener(this);
        mFabuZpxx = findViewById(R.id.tv_zpxx);
        mFabuZpxx.setOnClickListener(this);
        mFabuQzxx = findViewById(R.id.tv_qzxx);
        mFabuQzxx.setOnClickListener(this);
        mFabu = findViewById(R.id.btn_qrtj);
        mFabu.setOnClickListener(this);
        mLlZpxx = findViewById(R.id.ll_zpxx);
        mLlQzxx = findViewById(R.id.ll_qzxx);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back://返回
                finish();
            case R.id.tv_zpxx://发布招聘信息页面
                //改变页面显示为编辑招聘信息页面并改变Textview状态
                mFabuZpxx.setTextColor(getResources().getColor(R.color.clc_white));
                mFabuZpxx.setBackgroundResource(R.mipmap.icon_fabu_item_bg);
                mFabuQzxx.setTextColor(getResources().getColor(R.color.clc_black));
                mFabuQzxx.setBackground(null);
                mLlZpxx.setVisibility(View.VISIBLE);
                mLlQzxx.setVisibility(View.GONE);
                mZpOrQz = false;
                break;
            case R.id.tv_qzxx://发布求职信息页面
                //改变页面显示为编辑求职信息页面并改变Textview状态
                mFabuZpxx.setTextColor(getResources().getColor(R.color.clc_black));
                mFabuZpxx.setBackground(null);
                mFabuQzxx.setTextColor(getResources().getColor(R.color.clc_white));
                mFabuQzxx.setBackgroundResource(R.mipmap.icon_fabu_item_bg);
                mLlZpxx.setVisibility(View.GONE);
                mLlQzxx.setVisibility(View.VISIBLE);
                mZpOrQz = true;
                break;
            case R.id.btn_qrtj:
                if(!mZpOrQz){//提交招聘信息
                    ToastUtil.getInstance().shortShow("确认提交招聘信息");

                }else{//提交求职信息
                    ToastUtil.getInstance().shortShow("确认提交求职信息");

                }
                break;
        }
    }


}

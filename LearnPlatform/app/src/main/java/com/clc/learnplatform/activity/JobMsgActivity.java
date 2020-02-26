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

import okhttp3.MediaType;

/**
 * 蓝领求职信息页面
 */
public class JobMsgActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "JobMsgActivity";
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private ImageView mBack;
    private TextView mQy;//区域
    private TextView mBt;//标题
    private TextView mGzdy;//工资
    private TextView mFbsj;//发布时间
    private TextView mZW;//招聘职位或者求职意向
    private TextView mZpzw;//招聘职位
    private LinearLayout mLlZprs;//招聘人数布局
    private TextView mZprs;//招聘人数
    private TextView mSzcs;//所在城市
    private TextView mLxdh;//联系电话
    private LinearLayout mLlZwms;//职位描述布局
    private TextView mZwms;//职位描述

    private LinearLayout mLlQzyx;//求职意向布局
    private TextView mQzyx;//求职意向
    private LinearLayout mLlJims;//简历描述布局
    private TextView mJlms;//简历描述

    private Button mMsyp;//马上应聘按钮

  private String openid;

  private boolean mZpOrQz;//标示是招聘还是求职

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_msg);
        //隐藏标题栏,有效
        getSupportActionBar().hide();

        Intent intent = getIntent();
        openid = intent.getStringExtra("openid");

        initView();
        initData(intent);
    }

    private void initData(Intent intent) {
        String qy = intent.getStringExtra("qy");
        mQy.setText(qy);
        String zp_qz = intent.getStringExtra("zp_qz");
        String bt = intent.getStringExtra("bt");
        mBt.setText(bt);
        String fbsj = intent.getStringExtra("fbsj");
        mFbsj.setText("发布时间 "+fbsj);
        String zwlb = intent.getStringExtra("zwlb");
        mZpzw.setText(zwlb);
        String szcs = intent.getStringExtra("szcs");
        mSzcs.setText(szcs);
        String lxdh = intent.getStringExtra("lxdh");
        mLxdh.setText(lxdh);

        if(zp_qz.equals("0")){//招聘
            mLlZprs.setVisibility(View.VISIBLE);
            mZW.setText("招聘职位");
            int zprs = intent.getIntExtra("zprs",0);
            mZprs.setText(zprs+"个");
            mLlZwms.setVisibility(View.VISIBLE);
            mZwms.setVisibility(View.VISIBLE);
            String zwms = intent.getStringExtra("zwms");
            mZwms.setText(zwms);

            mLlJims.setVisibility(View.GONE);
            mJlms.setVisibility(View.GONE);
            mLlQzyx.setVisibility(View.GONE);
            mQzyx.setVisibility(View.GONE);

        }else if(zp_qz.equals("1")){//求职
            mLlZprs.setVisibility(View.GONE);
            mZW.setText("意向职位");
            mLlZwms.setVisibility(View.GONE);
            mZwms.setVisibility(View.GONE);

            mLlJims.setVisibility(View.VISIBLE);
            mJlms.setVisibility(View.VISIBLE);
            mLlQzyx.setVisibility(View.VISIBLE);
            mQzyx.setVisibility(View.VISIBLE);

            String jlms = intent.getStringExtra("jlms");
            mJlms.setText(jlms);
            String qzyx = intent.getStringExtra("qzyx");
            mQzyx.setText(qzyx);
        }

    }

    private void initView() {
        mBack = findViewById(R.id.iv_back);
        mBack.setOnClickListener(this);
        mQy = findViewById(R.id.tv_location);
        mBt = findViewById(R.id.tv_job_msg);
        mGzdy = findViewById(R.id.tv_gzdy);
        mFbsj = findViewById(R.id.tv_fabu_time);
        mZW = findViewById(R.id.tv_zw);
        mZpzw = findViewById(R.id.tv_zpzw);
        mLlZprs = findViewById(R.id.ll_zprs);
        mZprs = findViewById(R.id.tv_zprs);
        mSzcs = findViewById(R.id.tv_city);
        mLxdh = findViewById(R.id.tv_lxdh);
        mLlZwms = findViewById(R.id.ll_zwms);
        mZwms = findViewById(R.id.tv_zwms);
        mMsyp = findViewById(R.id.btn_msyp);

        mLlJims = findViewById(R.id.ll_jlms);
        mJlms = findViewById(R.id.tv_jlms);
        mLlQzyx = findViewById(R.id.ll_qzyx);
        mQzyx = findViewById(R.id.tv_qzyx);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back://返回
                finish();
                break;
        }
    }
}

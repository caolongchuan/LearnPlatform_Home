package com.clc.learnplatform.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.clc.learnplatform.R;
import com.clc.learnplatform.entity.SHENG_Entity;
import com.clc.learnplatform.entity.ZYLB_Entity;
import com.clc.learnplatform.util.QyZwlbUtil;
import com.clc.learnplatform.util.ToastUtil;
import com.zaaach.citypicker.CityPicker;
import com.zaaach.citypicker.adapter.OnPickListener;
import com.zaaach.citypicker.model.City;
import com.zaaach.citypicker.model.HotCity;
import com.zaaach.citypicker.model.LocatedCity;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;

/**
 * 蓝领求职发布编辑页面
 */
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
    //招聘信息的编辑界面
    private EditText mZpBtxx;//标题信息
    private TextView mZpSzcs;//所在城市
    private TextView mZpZwlb;//职位类别
    private EditText mZpLxdh;//联系电话
    private EditText mZpZprs;//招聘人数
    private EditText mZpZwms;//职位描述

    private LinearLayout mLlQzxx;//求职信息编辑的布局
    //求职信息的编辑界面
    private EditText mQzBtxx;//标题信息
    private TextView mQzSzcs;//所在城市
    private TextView mQzZwlb;//职位类别
    private EditText mQzLxdh;//联系电话
    private EditText mQzJlms;//简历描述
    private EditText mQzQzyx;//求职意向

    private ArrayList<SHENG_Entity> mShengEntity;//省名称集合
    private ArrayList<ZYLB_Entity> mZylbEntity;//职业类别集合
    private String ZpShengName = "";
    private String ZpShiName = "";
    private String QzShengName = "";
    private String QzShiName = "";

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
        mShengEntity = QyZwlbUtil.getInstance().getShengList();
        mZylbEntity =  QyZwlbUtil.getInstance().getZylbList();
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

        //招聘信息编辑的布局与控件
        mLlZpxx = findViewById(R.id.ll_zpxx);
        mZpBtxx = findViewById(R.id.et_btxx);
        mZpSzcs = findViewById(R.id.tv_szcs);
        mZpSzcs.setOnClickListener(this);
        mZpZwlb = findViewById(R.id.tv_zwlb);
        mZpZwlb.setOnClickListener(this);
        mZpLxdh = findViewById(R.id.et_lxdh);
        mZpZprs = findViewById(R.id.et_zprs);
        mZpZwms = findViewById(R.id.et_zwms);
        //求职信息编辑的布局与控件
        mLlQzxx = findViewById(R.id.ll_qzxx);
        mQzBtxx = findViewById(R.id.et_btxx1);
        mQzSzcs = findViewById(R.id.tv_szcs1);
        mQzSzcs.setOnClickListener(this);
        mQzZwlb = findViewById(R.id.tv_zwlb1);
        mQzZwlb.setOnClickListener(this);
        mQzLxdh = findViewById(R.id.et_lxdh1);
        mQzJlms = findViewById(R.id.et_jlms);
        mQzQzyx = findViewById(R.id.et_qzyx);
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
            case R.id.tv_szcs://编辑招聘信息选择城市
                final List<String> options1Items = new ArrayList<>();
                final List<List<String>> options2Items = new ArrayList<>();
                for(int i=0;i<mShengEntity.size();i++){
                    options1Items.add(mShengEntity.get(i).value);
                    List<String> temp = new ArrayList<>();
                    for (int j=0;j<mShengEntity.get(i).childs.size();j++){
                        temp.add(mShengEntity.get(i).childs.get(j).value);
                    }
                    options2Items.add(temp);
                }
                //条件选择器
                OptionsPickerView pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
                    @Override
                    public void onOptionsSelect(int options1, int option2, int options3, View v) {
                        //返回的分别是三个级别的选中位置
                        ZpShengName = options1Items.get(options1);
                        ZpShiName = options2Items.get(options1).get(option2);
                        mZpSzcs.setText(ZpShengName + " " +ZpShiName);
                    }
                }).build();
                pvOptions.setTitleText("区域");
                pvOptions.setPicker(options1Items,options2Items);
                pvOptions.show();
                break;
            case R.id.tv_zwlb://编辑招聘信息职位类别
                final List<String> options1Items11 = new ArrayList<>();
                for(int i=0; i<mZylbEntity.size();i++){
                    options1Items11.add(mZylbEntity.get(i).value);
                }
                //条件选择器
                OptionsPickerView pvOptions11 = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
                    @Override
                    public void onOptionsSelect(int options1, int option2, int options3, View v) {
                        //返回的分别是三个级别的选中位置
                        mZpZwlb.setText(options1Items11.get(options1));
                    }
                }).build();
                pvOptions11.setTitleText("职位类别");
                pvOptions11.setPicker(options1Items11);
                pvOptions11.show();
                break;

            case R.id.tv_szcs1://编辑求职信息选择城市
                final List<String> options1Items1 = new ArrayList<>();
                final List<List<String>> options2Items1 = new ArrayList<>();
                for(int i=0;i<mShengEntity.size();i++){
                    options1Items1.add(mShengEntity.get(i).value);
                    List<String> temp = new ArrayList<>();
                    for (int j=0;j<mShengEntity.get(i).childs.size();j++){
                        temp.add(mShengEntity.get(i).childs.get(j).value);
                    }
                    options2Items1.add(temp);
                }
                //条件选择器
                OptionsPickerView pvOptions1 = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
                    @Override
                    public void onOptionsSelect(int options1, int option2, int options3, View v) {
                        //返回的分别是三个级别的选中位置
                        QzShengName = options1Items1.get(options1);
                        QzShiName = options2Items1.get(options1).get(option2);
                        mQzSzcs.setText(QzShengName + " " +QzShiName);
                    }
                }).build();
                pvOptions1.setTitleText("区域");
                pvOptions1.setPicker(options1Items1,options2Items1);
                pvOptions1.show();
                break;
            case R.id.tv_zwlb1://编辑求职信息职位类别
                final List<String> options1Items12 = new ArrayList<>();
                for(int i=0; i<mZylbEntity.size();i++){
                    options1Items12.add(mZylbEntity.get(i).value);
                }
                //条件选择器
                OptionsPickerView pvOptions12 = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
                    @Override
                    public void onOptionsSelect(int options1, int option2, int options3, View v) {
                        //返回的分别是三个级别的选中位置
                        mZpZwlb.setText(options1Items12.get(options1));
                    }
                }).build();
                pvOptions12.setTitleText("职位类别");
                pvOptions12.setPicker(options1Items12);
                pvOptions12.show();
                break;

        }
    }

}

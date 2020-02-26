package com.clc.learnplatform.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.QyZwlbUtil;
import com.clc.learnplatform.util.ToastUtil;
import com.zaaach.citypicker.CityPicker;
import com.zaaach.citypicker.adapter.OnPickListener;
import com.zaaach.citypicker.model.City;
import com.zaaach.citypicker.model.HotCity;
import com.zaaach.citypicker.model.LocatedCity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0x01:
                    Bundle data = msg.getData();
                    ToastUtil.getInstance().shortShow(data.getString("msg"));
                    break;
            }
            return false;
        }
    });



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
            case R.id.btn_qrtj://提交信息
                if(!mZpOrQz){//提交招聘信息
                    String bt = mZpBtxx.getText().toString();
                    String sjh = mZpLxdh.getText().toString();
                    String zprs = mZpZprs.getText().toString();
                    String zwlb = mZpZwlb.getText().toString();
                    String zwms = mZpZwms.getText().toString();

                    if(bt.isEmpty()||sjh.isEmpty()||zprs.isEmpty()||zwlb.isEmpty()
                            ||zwms.isEmpty()||ZpShengName.isEmpty()||ZpShiName.isEmpty()){
                        ToastUtil.getInstance().shortShow("请保证必填项都已填写");
                    }else{
                        TiJiaoZpxx(bt,ZpShengName,ZpShiName,sjh,zprs,zwlb,zwms);
                    }

                }else{//提交求职信息
                    String bt = mQzBtxx.getText().toString();
                    String sjh = mQzLxdh.getText().toString();
                    String zwlb = mQzZwlb.getText().toString();
                    String jlms = mQzJlms.getText().toString();
                    String qzyx = mQzQzyx.getText().toString();
                    if(bt.isEmpty()||sjh.isEmpty()||zwlb.isEmpty()||jlms.isEmpty()
                            ||qzyx.isEmpty()||QzShengName.isEmpty()||QzShiName.isEmpty()){
                        ToastUtil.getInstance().shortShow("请保证必填项都已填写");
                    }else{
                        TiJiaoQzxx(bt,QzShengName,QzShiName,sjh,jlms,zwlb,qzyx);
                    }
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
                        mQzZwlb.setText(options1Items12.get(options1));
                    }
                }).build();
                pvOptions12.setTitleText("职位类别");
                pvOptions12.setPicker(options1Items12);
                pvOptions12.show();
                break;

        }
    }

    /**
     * 提交发布求职信息
     */
    private void TiJiaoQzxx(String bt, String sss, String shi, String sjh, String jlms, String zwlb, String qzyx) {
        OkHttpClient okHttpClient = new OkHttpClient();
        StringBuffer sb = new StringBuffer();
        sb.append("openid=").append(openid).append("&BT=").append(bt).append("&SSS=").append(sss)
                .append("&SHI=").append(shi).append("&SJH=").append(sjh).append("&JLMS=").append(jlms)
                .append("&ZWLB=").append(zwlb).append("&QZYX=").append(qzyx);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.ZP_FIND_URL)//蓝领求职首页
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 提交发布求职信息失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.i(TAG, "JobFragment.onResponse: responseInfo===" + responseInfo);
                String error = null;
                try {
                    Message msg = new Message();
                    msg.what = 0x01;
                    Bundle bundle = new Bundle();
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//失败
                        String message = jsonObject.getString("message");
                        Log.i(TAG, "JobFragment: message===" + message);
                        bundle.putString("msg","提交失败");
                    } else if (error.equals("false")) {//成功
                        bundle.putString("msg","提交成功");
                    }
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);//通知UI线程更新界面
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * 提交发布招聘信息
     */
    private void TiJiaoZpxx(String bt,String sss,String shi,String sjh,String zprs,String zwlb,String zwms) {
        OkHttpClient okHttpClient = new OkHttpClient();
        StringBuffer sb = new StringBuffer();
        sb.append("openid=").append(openid).append("&BT=").append(bt).append("&SSS=").append(sss)
            .append("&SHI=").append(shi).append("&SJH=").append(sjh).append("&ZPRS=").append(zprs)
            .append("&ZWLB=").append(zwlb).append("&ZWMS=").append(zwms);
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.ZP_FIND_URL)//蓝领求职首页
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 提交发布招聘信息失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.i(TAG, "JobFragment.onResponse: responseInfo===" + responseInfo);
                String error = null;
                try {
                    Message msg = new Message();
                    msg.what = 0x01;
                    Bundle bundle = new Bundle();
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//失败
                        String message = jsonObject.getString("message");
                        Log.i(TAG, "JobFragment: message===" + message);
                        bundle.putString("msg","提交失败");
                    } else if (error.equals("false")) {//成功
                        bundle.putString("msg","提交成功");
                    }
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);//通知UI线程更新界面
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

}

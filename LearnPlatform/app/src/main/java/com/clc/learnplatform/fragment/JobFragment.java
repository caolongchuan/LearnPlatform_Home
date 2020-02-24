package com.clc.learnplatform.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.clc.learnplatform.R;
import com.clc.learnplatform.activity.JobFabuActivity;
import com.clc.learnplatform.adapter.JobItemAdapter;
import com.clc.learnplatform.entity.QZXX_Entity;
import com.clc.learnplatform.entity.SHENG_Entity;
import com.clc.learnplatform.entity.SHI_Entity;
import com.clc.learnplatform.entity.UserInfoEntity;
import com.clc.learnplatform.entity.ZPXX_Entity;
import com.clc.learnplatform.entity.ZYLB_Entity;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.pager.HomeMainPager;
import com.clc.learnplatform.util.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
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
 * 蓝领求职
 */
public class JobFragment extends Fragment implements View.OnClickListener {
    private final String TAG = "JobFragment";
    private static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private Activity mActivty;
    private View mView;

    private String openid;
    private String qy;      //区域（例：邢台市）（不传默认为用户所属城市）
    private String lx;      //类别（01 招聘 02 求职；不传默认为01）

    private LinearLayout mMain;
    private HomeMainPager mmp;

    private TextView mCityName;
    private LinearLayout mFabu;

    private TextView tvQuyu;//区域选择下拉列表
    private TextView tvZwlb;//职位类别选择下拉列表
    private TextView tvLb;//招聘于应聘选择列表

    private TextView tvZwxxTs;//暂无信息提示
    private ListView mJobList;//内容列表
    private JobItemAdapter mAdapter;//Adapter

    private UserInfoEntity mUserInfoEntiry;//用户的信息

    private ArrayList<SHENG_Entity> mShengEntity;//省名称集合
    private ArrayList<ZPXX_Entity> mZpxxEntity;//招聘信息集合
    private ArrayList<QZXX_Entity> mQzxxEntity;//求职信息集合

    private ArrayList<ZYLB_Entity> mZylbEntity;//职业类别集合

    public JobFragment(Activity activity, String openid, UserInfoEntity userInfoEntity) {
        mActivty = activity;
        this.openid = openid;
        this.mUserInfoEntiry = userInfoEntity;
        qy = mUserInfoEntiry.SHI;
    }

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0x01:
                    uptadaUI();
                    break;
            }
            return false;
        }
    });

    /**
     * 更新UI
     */
    private void uptadaUI() {
        if (mZpxxEntity.size() <= 0 && mQzxxEntity.size() <= 0) {
            tvZwxxTs.setVisibility(View.VISIBLE);
            mJobList.setVisibility(View.GONE);
        }else {
            tvZwxxTs.setVisibility(View.GONE);
            mJobList.setVisibility(View.VISIBLE);
        }

//        mAdapter.updataData(mZpxxEntity,mQzxxEntity);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_job, container, false);
        initView();
        initData();
        getDataFromService();
        return mView;
    }

    /**
     * 从服务器获取蓝领求职数据
     */
    private void getDataFromService() {
        OkHttpClient okHttpClient = new OkHttpClient();
        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(openid)
                .append("&qy=")
                .append(qy)
                .append("&lx=")
                .append("01");
        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
        final Request request = new Request.Builder()
                .url(Constants.ZP_FIND_URL)//蓝领求职首页
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: 获取蓝领求职数据失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.i(TAG, "JobFragment.onResponse: responseInfo===" + responseInfo);
                String error = null;
                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    error = jsonObject.getString("error");
                    if (error.equals("true")) {//失败
                        String message = jsonObject.getString("message");
                        Log.i(TAG, "JobFragment: message===" + message);
                    } else if (error.equals("false")) {//成功
                        analysisData(responseInfo);//解析数据
                        Message msg = new Message();
                        msg.what = 0x01;
                        mHandler.sendMessage(msg);//通知UI线程更新界面
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initView() {
        mMain = mView.findViewById(R.id.ll_main_job);
        mCityName = mView.findViewById(R.id.tv_location);
        mFabu = mView.findViewById(R.id.ll_fabu);
        tvQuyu = mView.findViewById(R.id.tv_quye);
        tvQuyu.setOnClickListener(this);
        tvZwlb = mView.findViewById(R.id.tv_zwlb);
        tvZwlb.setOnClickListener(this);
        tvLb = mView.findViewById(R.id.tv_lb);
        tvLb.setOnClickListener(this);
        mJobList = mView.findViewById(R.id.lv_job);

        tvZwxxTs = mView.findViewById(R.id.tv_zwxx);
    }

    private void initData() {
        mCityName.setText(mUserInfoEntiry.SHI);

        mShengEntity = new ArrayList<>();
        mZpxxEntity = new ArrayList<>();
        mQzxxEntity = new ArrayList<>();
        mZylbEntity = new ArrayList<>();

        mAdapter = new JobItemAdapter(mActivty, mZpxxEntity, mQzxxEntity, openid);
        mJobList.setAdapter(mAdapter);

        //进入发布信息界面
        mFabu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("openid", openid);
                intent.setClass(mActivty, JobFabuActivity.class);
                mActivty.startActivity(intent);
            }
        });
    }

    /**
     * 解析数据
     * @param data_json
     */
    private void analysisData(String data_json) {
        try {
            JSONObject jsonObject = new JSONObject(data_json);
            //解析区域数据 包括省市数据
            String qystr = jsonObject.getString("qystr");
            JSONArray jsonArray_qy = new JSONArray(qystr);
            for (int i = 0; i < jsonArray_qy.length()-1; i++) {
                SHENG_Entity sheng = new SHENG_Entity();
                JSONObject jsonObject_qy = jsonArray_qy.getJSONObject(i);
                sheng.id = jsonObject_qy.getString("id");
                sheng.value = jsonObject_qy.getString("value");
                sheng.childs = new ArrayList<>();
                String childs = jsonObject_qy.getString("childs");
                JSONArray jsonArray_childs = new JSONArray(childs);
                for (int j = 0; j < jsonArray_childs.length()-1; j++) {
                    SHI_Entity shi = new SHI_Entity();
                    JSONObject jsonObject_childs = jsonArray_childs.getJSONObject(j);
                    shi.id = jsonObject_childs.getString("id");
                    shi.value = jsonObject_childs.getString("value");
                    sheng.childs.add(shi);
                }
                mShengEntity.add(sheng);
            }
            //解析职位类别
            String zwlbstr = jsonObject.getString("zwlbstr");
            JSONArray jsonArray_zylb = new JSONArray(zwlbstr);
            for(int i = 0; i<jsonArray_zylb.length();i++){
                ZYLB_Entity ze = new ZYLB_Entity();
                JSONObject jsonObject_zylb = jsonArray_zylb.getJSONObject(i);
                ze.id = jsonObject_zylb.getString("id");
                ze.value = jsonObject_zylb.getString("value");
                mZylbEntity.add(ze);
            }
            //解析招聘信息
            JSONArray zplist = jsonObject.getJSONArray("zplist");
            for(int i = 0;i<zplist.length();i++){
                ZPXX_Entity ze = new ZPXX_Entity();
                JSONObject jsonObject1 = zplist.getJSONObject(i);
                ze.BT = jsonObject1.getString("BT");
                ze.ID = jsonObject1.getString("ID");
                ze.SHI = jsonObject1.getString("SHI");
                ze.SJH = jsonObject1.getString("SJH");
                ze.SSS = jsonObject1.getString("SSS");
                ze.TJSJ = jsonObject1.getString("TJSJ");
                ze.YHID = jsonObject1.getString("YHID");
                ze.ZPRS = jsonObject1.getInt("ZPRS");
                ze.ZT = jsonObject1.getString("ZT");
                ze.ZWLB = jsonObject1.getString("ZWLB");
                ze.ZWMS = jsonObject1.getString("ZWMS");
                mZpxxEntity.add(ze);
            }
            //解析求职信息
            JSONArray qzlist = jsonObject.getJSONArray("qzlist");
            for(int i=0;i<qzlist.length();i++){
                QZXX_Entity qe = new QZXX_Entity();
                JSONObject jsonObject1 = qzlist.getJSONObject(i);
                qe.ID = jsonObject1.getString("ID");
                qe.YHID = jsonObject1.getString("YHID");
                qe.TJSJ = jsonObject1.getString("TJSJ");
                qe.SJH = jsonObject1.getString("SJH");
                qe.BT = jsonObject1.getString("BT");
                qe.ZWLB = jsonObject1.getString("ZWLB");
                qe.JLMS = jsonObject1.getString("JLMS");
                qe.QZYX = jsonObject1.getString("QZYX");
                qe.ZT = jsonObject1.getString("ZT");
                qe.SSS = jsonObject1.getString("SSS");
                qe.SHI = jsonObject1.getString("SHI");
                mQzxxEntity.add(qe);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_quye://选择区域
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
                OptionsPickerView pvOptions = new OptionsPickerBuilder(mActivty, new OnOptionsSelectListener() {
                    @Override
                    public void onOptionsSelect(int options1, int option2, int options3, View v) {
                        //返回的分别是三个级别的选中位置
                        mCityName.setText(options2Items.get(options1).get(option2));
//                        ToastUtil.getInstance().shortShow(options1Items.get(options1)+"--"+options2Items.get(options1).get(option2));
                    }
                }).build();
                pvOptions.setTitleText("区域");
                pvOptions.setPicker(options1Items,options2Items);
                pvOptions.show();
                break;
            case R.id.tv_zwlb://选择职业类别
                final List<String> optionsItems = new ArrayList<>();
                for(int i= 0;i<mZylbEntity.size();i++){
                    optionsItems.add(mZylbEntity.get(i).value);
                }
                //条件选择器
                OptionsPickerView pvOptions1 = new OptionsPickerBuilder(mActivty, new OnOptionsSelectListener() {
                    @Override
                    public void onOptionsSelect(int options1, int option2, int options3, View v) {
                        tvZwlb.setText(optionsItems.get(options1));
                    }
                }).build();
                pvOptions1.setTitleText("职业类别");
                pvOptions1.setPicker(optionsItems);
                pvOptions1.show();
                break;
            case R.id.tv_lb://选择类别
                final List<String> optionsItems1 = new ArrayList<>();
                optionsItems1.add("招聘");
                optionsItems1.add("求职");
                //条件选择器
                OptionsPickerView pvOptions2 = new OptionsPickerBuilder(mActivty, new OnOptionsSelectListener() {
                    @Override
                    public void onOptionsSelect(int options1, int option2, int options3, View v) {
                        tvLb.setText(optionsItems1.get(options1));
                        mZpxxEntity.clear();
                        mAdapter.notifyDataSetChanged();
                    }
                }).build();
                pvOptions2.setTitleText("类别");
                pvOptions2.setPicker(optionsItems1);
                pvOptions2.show();
                break;
        }
    }
}

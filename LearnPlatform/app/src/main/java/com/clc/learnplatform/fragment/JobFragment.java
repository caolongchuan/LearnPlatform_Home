package com.clc.learnplatform.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.clc.learnplatform.util.QyZwlbUtil;

import org.json.JSONArray;
import org.json.JSONException;
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

    private LinearLayout mMain;
    private HomeMainPager mmp;

    private TextView mCityName;
    private LinearLayout mFabu;

    private EditText mSearchText;//搜索用到的关键字

    private TextView tvQuyu;//区域选择下拉列表
    private TextView tvZwlb;//职位类别选择下拉列表
    private TextView tvLb;//招聘于应聘选择列表

    private TextView tvZwxxTs;//暂无信息提示
    private ListView mJobList;//内容列表
    private JobItemAdapter mAdapter;//Adapter

    private UserInfoEntity mUserInfoEntiry;//用户的信息

    private ArrayList<SHENG_Entity> mShengEntity;//省名称集合
    private ArrayList<ZYLB_Entity> mZylbEntity;//职业类别集合

    private ArrayList<ZPXX_Entity> mZpxxEntity;//招聘信息集合（用于给Adapter设置 可以通过条件查询进行过滤）
    private ArrayList<QZXX_Entity> mQzxxEntity;//求职信息集合（用于给Adapter设置 可以通过条件查询进行过滤）


    private AlertDialog alertDialog;//等待对话框


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
                    updataUI();
                    break;
                case 0x02:
                    updataUI();
                    break;
                case 0x03://根据参数获取到招聘数据以后
                    Bundle data = msg.getData();
                    String py = data.getString("py");
                    String zwlb = data.getString("zwlb");
                    String gjc = data.getString("gjc");
                    getDataFromService(py,zwlb,gjc,"02");
                    break;
                case 0x04://即获取到招聘数据又获取到求职数据以后
                    alertDialog.dismiss();
                    updataUI();
                    break;
            }
            return false;
        }
    });

    /**
     * 更新UI
     */
    private void updataUI() {
        if(!mAdapter.getShowZpOrQz()){//如果选择的是招聘信息
            if(mZpxxEntity.size() <= 0){//如果没有符合要求的招聘信息
                tvZwxxTs.setVisibility(View.VISIBLE);
                mJobList.setVisibility(View.GONE);
            }else{
                tvZwxxTs.setVisibility(View.GONE);
                mJobList.setVisibility(View.VISIBLE);
            }
        }else {//如果选择的是求职信息
            if(mQzxxEntity.size() <= 0){//如果没有符合要求的求职信息
                tvZwxxTs.setVisibility(View.VISIBLE);
                mJobList.setVisibility(View.GONE);
            }else{
                tvZwxxTs.setVisibility(View.GONE);
                mJobList.setVisibility(View.VISIBLE);
            }
        }
        //将更新完的省名称集合与职业类别集合同步到QyUtil中
        QyZwlbUtil.getInstance().setShengList(mShengEntity);
        QyZwlbUtil.getInstance().setZwlbList(mZylbEntity);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_job, container, false);
        initView();
        initData();
        getZpDataFromService();//从服务器获取招聘信息
        getQzDataFromService();//从服务器获取求职信息
        return mView;
    }

    /**
     * 从服务器获取蓝领求职的求职信息数据
     */
    private void getQzDataFromService() {
        OkHttpClient okHttpClient = new OkHttpClient();
        StringBuffer sb = new StringBuffer();
        sb.append("openid=")
                .append(openid)
                .append("&qy=")
                .append(qy)
                .append("&lx=")
                .append("02");
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
                        analysisQzData(responseInfo);//解析数据
                        Message msg = new Message();
                        msg.what = 0x02;
                        mHandler.sendMessage(msg);//通知UI线程更新界面
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 解析求职信息数据
     * @param responseInfo
     */
    private void analysisQzData(String responseInfo) {
        try {
            JSONObject jsonObject = new JSONObject(responseInfo);
            //解析求职信息
            mQzxxEntity.clear();
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

    /**
     * 从服务器获取蓝领求职的招聘信息数据
     */
    private void getZpDataFromService() {
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
                        analysisZpData(responseInfo);//解析招聘信息数据
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
        mSearchText = mView.findViewById(R.id.et_search_text);
        mCityName = mView.findViewById(R.id.tv_location);
        mFabu = mView.findViewById(R.id.ll_fabu);
        mFabu.setOnClickListener(this);
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
        alertDialog = new AlertDialog
                .Builder(mActivty).setMessage("请稍等...")
                .create();

        mCityName.setText(mUserInfoEntiry.SHI);

        mShengEntity = new ArrayList<>();

        mZpxxEntity = new ArrayList<>();
        mQzxxEntity = new ArrayList<>();

        mZylbEntity = new ArrayList<>();

        mAdapter = new JobItemAdapter(mActivty,mZylbEntity, mZpxxEntity, mQzxxEntity, openid,qy);
        mJobList.setAdapter(mAdapter);

        //点击了搜索后的事件
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //点击搜索的时候隐藏软键盘
                    hideKeyboard(mSearchText);
                    // 在这里写搜索的操作,一般都是网络请求数据
                    String qy1 = mCityName.getText().toString();
                    String zwlb = "";
                    for(int i=0;i<mZylbEntity.size();i++){
                        if(mZylbEntity.get(i).value.equals(tvZwlb.getText().toString())){
                            zwlb = mZylbEntity.get(i).id;
                        }
                    }
                    String gjc = mSearchText.getText().toString();
                    getDataFromService(qy1,zwlb,gjc,"01");
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 隐藏软键盘
     * @param view    :一般为EditText
     */
    public void hideKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 解析招聘数据（包含区域与职业类别数据）
     * @param data_json
     */
    private void analysisZpData(String data_json) {
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
            mZylbEntity.clear();
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
            mZpxxEntity.clear();
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
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 根据提供的参数从服务器获取招聘与求职数据
     * @param qy1 区域 （例如 邢台市）
     * @param zwlb1 职位类别
     * @param gjc 关键字 可不传
     */
    private void getDataFromService(final String qy1, final String zwlb1, final String gjc, final String lx){
        alertDialog.show();

        OkHttpClient okHttpClient = new OkHttpClient();
        StringBuffer sb = new StringBuffer();
        if(null == gjc){//如果没有关键词
            sb.append("openid=").append(openid).append("&qy=").append(qy1)
                    .append("&zwlb=").append(zwlb1).append("&lx=").append(lx);
        }else{
            sb.append("openid=").append(openid).append("&qy=").append(qy1)
                    .append("&zwlb=").append(zwlb1).append("&gjc=").append(gjc).append("&lx=").append(lx);
        }
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
                        if(lx.equals("01")){//如果是招聘信息
                            analysisZpData(responseInfo);//解析招聘信息数据
                            Message msg = new Message();
                            msg.what = 0x03;
                            Bundle bundle = new Bundle();
                            bundle.putString("qy",qy1);
                            bundle.putString("zwlb",zwlb1);
                            if(null != gjc){
                                bundle.putString("gjc",gjc);
                            }
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);//通知UI线程更新界面
                        }else if(lx.equals("02")){//如果是求职信息
                            analysisQzData(responseInfo);//解析求职信息数据
                            Message msg = new Message();
                            msg.what = 0x04;
                            mHandler.sendMessage(msg);//通知UI线程更新界面
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


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
                        String zwlb = "";
                        for(int i=0;i<mZylbEntity.size();i++){
                            if(mZylbEntity.get(i).value.equals(tvZwlb.getText().toString())){
                                zwlb = mZylbEntity.get(i).id;
                            }
                        }
                        String gjc = mSearchText.getText().toString();
                        if(gjc.equals("")){//没有输入关键字
                            getDataFromService(options2Items.get(options1).get(option2),zwlb,null,"01");
                        }else{//输入了关键字
                            getDataFromService(options2Items.get(options1).get(option2),zwlb,gjc,"01");
                        }
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

                        String qy = mCityName.getText().toString();
                        String zwlb = "";
                        for(int i=0;i<mZylbEntity.size();i++){
                            if(mZylbEntity.get(i).value.equals(tvZwlb.getText().toString())){
                                zwlb = mZylbEntity.get(i).id;
                            }
                        }
                        String gjc = mSearchText.getText().toString();
                        if(gjc.equals("")){//没有输入关键字
                            getDataFromService(qy,zwlb,null,"01");
                        }else{//输入了关键字
                            getDataFromService(qy,zwlb,gjc,"01");
                        }
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
                        if(mAdapter.getShowZpOrQz()){
                            mAdapter.setShowZpOrQz(false);
                        }else{
                            mAdapter.setShowZpOrQz(true);
                        }
                        Message msg = new Message();
                        msg.what = 0x04;
                        mHandler.sendMessage(msg);
                    }
                }).build();
                pvOptions2.setTitleText("类别");
                pvOptions2.setPicker(optionsItems1);
                pvOptions2.show();
                break;
            case R.id.ll_fabu://发布
                Intent intent = new Intent();
                intent.putExtra("openid", openid);
                intent.setClass(mActivty, JobFabuActivity.class);
                mActivty.startActivity(intent);
                break;
        }
    }
}

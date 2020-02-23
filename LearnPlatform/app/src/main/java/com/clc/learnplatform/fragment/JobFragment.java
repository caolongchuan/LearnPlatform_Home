package com.clc.learnplatform.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.baidu.location.LLSInterface;
import com.clc.learnplatform.R;
import com.clc.learnplatform.activity.JobFabuActivity;
import com.clc.learnplatform.adapter.JobItemAdapter;
import com.clc.learnplatform.entity.LLQZ_Entity;
import com.clc.learnplatform.pager.HomeMainPager;
import com.clc.learnplatform.util.ToastUtil;

import java.util.ArrayList;

/**
 * 蓝领求职
 */
public class JobFragment extends Fragment {
    private Activity mActivty;
    private View mView;

    private String openid;

    private LinearLayout mMain;
    private HomeMainPager mmp;

    private LinearLayout mFabu;

    private Spinner spinner1;//区域选择下拉列表
    private Spinner spinner2;//职位类别选择下拉列表
    private Spinner spinner3;//招聘于应聘选择列表

    private ListView mJobList;//内容列表
    private JobItemAdapter mAdapter;//Adapter

    public JobFragment(Activity activity,String openid) {
        mActivty = activity;
        this.openid = openid;
    }

    //从服务器获取蓝领求职数据
    private void getDataFromService() {

        String data_json = "data_json";
        analysisData(data_json);//解析数据

    }

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 0x01:
                    initData();
                    break;
            }
            return false;
        }
    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_job, container, false);
        initView();
        getDataFromService();
        return mView;
    }

    private void initView() {
        mMain = mView.findViewById(R.id.ll_main_job);
        mFabu = mView.findViewById(R.id.ll_fabu);
        spinner1 = mView.findViewById(R.id.spinner1);
        spinner2 = mView.findViewById(R.id.spinner2);
        spinner3 = mView.findViewById(R.id.spinner3);
        mJobList = mView.findViewById(R.id.lv_job);
    }

    private void initData() {
        mJobList.setAdapter(mAdapter);

        //进入发布信息界面
        mFabu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("openid",openid);
                intent.setClass(mActivty, JobFabuActivity.class);
                mActivty.startActivity(intent);
            }
        });
    }

    //解析数据
    private void analysisData(String data_json) {
        String[] arrayStrings = new String[]{"区域", "区域1", "区域2", "区域3"};
        String[] arrayStrings1 = new String[]{"职业类别", "职业类别1", "职业类别2", "职业类别3"};
        String[] arrayStrings2 = new String[]{"招聘", "求职"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivty, android.R.layout.simple_spinner_item, arrayStrings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(mActivty, android.R.layout.simple_spinner_item, arrayStrings1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(mActivty, android.R.layout.simple_spinner_item, arrayStrings2);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        spinner1.setAdapter(adapter);
        spinner2.setAdapter(adapter1);
        spinner3.setAdapter(adapter2);

        //临时数据
        ArrayList<LLQZ_Entity> llqz_entities = new ArrayList<>();
        LLQZ_Entity llqz_1 = new LLQZ_Entity();
        llqz_1.a = "3";
        llqz_1.b = "锅炉作业";
        llqz_1.c = "18888889999";
        llqz_1.d = "邢台市桥西区";
        llqz_1.e = "技术工人";
        llqz_1.f = "职位描述职位描述职位描述职位描述职位描述职位描述职位描述职位描述职位描述职位描述";
        llqz_1.g = "2020.01.29";
        llqz_1.h = "2000-5000";
        LLQZ_Entity llqz_2 = new LLQZ_Entity();
        llqz_2.a = "3";
        llqz_2.b = "锅炉作业";
        llqz_2.c = "18888889999";
        llqz_2.d = "邢台市桥西区";
        llqz_2.e = "技术工人";
        llqz_2.f = "职位描述职位描述职位描述职位描述职位描述职位描述职位描述职位描述职位描述职位描述";
        llqz_2.g = "2020.01.29";
        llqz_2.h = "2000-5000";
        LLQZ_Entity llqz_3 = new LLQZ_Entity();
        llqz_3.a = "3";
        llqz_3.b = "锅炉作业";
        llqz_3.c = "18888889999";
        llqz_3.d = "邢台市桥西区";
        llqz_3.e = "技术工人";
        llqz_3.f = "职位描述职位描述职位描述职位描述职位描述职位描述职位描述职位描述职位描述职位描述";
        llqz_3.g = "2020.01.29";
        llqz_3.h = "2000-5000";
        llqz_entities.add(llqz_1);
        llqz_entities.add(llqz_2);
        llqz_entities.add(llqz_3);
        mAdapter = new JobItemAdapter(mActivty,llqz_entities,openid);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Message msg = new Message();
                msg.what = 0x01;
                mHandler.sendMessage(msg);
            }
        }).start();
    }


}

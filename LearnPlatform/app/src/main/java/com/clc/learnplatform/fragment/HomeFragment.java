package com.clc.learnplatform.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.clc.learnplatform.R;
import com.clc.learnplatform.activity.StudiedActivity;
import com.clc.learnplatform.entity.KSXM_Entity;
import com.clc.learnplatform.pager.HomeMainPager;

public class HomeFragment extends Fragment {
    private View mView;
    private LinearLayout mMain;

    //View
    private HomeMainPager mHomeMainPager;
    private String openid;
    private String mDataString;//首页传过来的数据 需要在HomeMainPager中进行解析

    public HomeFragment(String openid, String data_String) {
        this.openid = openid;
        this.mDataString = data_String;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_home, container, false);
        initView();
        initData();
        return mView;
    }

    private void initView() {
        mMain = mView.findViewById(R.id.ll_home_main);
    }

    private void initData() {
        mHomeMainPager = new HomeMainPager(this, openid, mDataString);
        mMain.addView(mHomeMainPager.getmView());
    }

    /**
     * 跳转到学习页面
     */
    public void gotoStudiedPager(String xmid) {
        Intent intent = new Intent();
        intent.putExtra("openid", openid);
        intent.putExtra("xmid", xmid);
        intent.putExtra("data_string",mDataString);
        intent.setClass(this.getContext(), StudiedActivity.class);
        startActivityForResult(intent, 100);
    }

    //设置金币值与最近学习项目
    public void setCoinAndZuijingStudy(KSXM_Entity ke,int coin){
        mHomeMainPager.setCoinAndZuijingStudy(ke, coin);
    }


}

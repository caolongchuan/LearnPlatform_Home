package com.clc.learnplatform.pager;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.clc.learnplatform.R;
import com.clc.learnplatform.adapter.HomeItemAdapter;
import com.clc.learnplatform.fragment.HomeFragment;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;

import java.util.ArrayList;

public class HomeStudiedPager {
    private Fragment mFragment;
    private View mView;

    private TextView tv;

    public HomeStudiedPager(Fragment fragment){
        mFragment = fragment;
        // TODO 动态添加布局(xml方式)
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);       //LayoutInflater inflater1=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //      LayoutInflater inflater2 = getLayoutInflater();
        LayoutInflater inflater = LayoutInflater.from(mFragment.getContext());
        mView = inflater.inflate(R.layout.fragment_home_studied, null);
        mView.setLayoutParams(lp);

        initView();
        initData();
    }

    private void initView() {
        tv = mView.findViewById(R.id.tv);

    }

    private void initData() {
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mFragment.getContext(),"学习页面",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public View getmView(){
        return mView;
    }
}

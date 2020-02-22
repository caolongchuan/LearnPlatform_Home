package com.clc.learnplatform.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.baidu.location.LLSInterface;
import com.clc.learnplatform.R;
import com.clc.learnplatform.pager.HomeMainPager;

public class JobFragment extends Fragment {
    private Activity mActivty;
    private View mView;

    private LinearLayout mMain;
    private Button button;

    private HomeMainPager mmp;

    public JobFragment(Activity activity) {
        mActivty = activity;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_job, container, false);
        initView();
        initData();
        return mView;
    }

    private void initView() {
        mMain = mView.findViewById(R.id.ll_main_job);
        button = mView.findViewById(R.id.btn_btn);
    }

    private void initData() {



    }



}

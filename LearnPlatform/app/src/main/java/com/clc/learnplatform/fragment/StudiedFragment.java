package com.clc.learnplatform.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.clc.learnplatform.R;

public class StudiedFragment extends Fragment {
    private Activity mActivty;
    private View mView;

    public StudiedFragment(Activity activity){
        mActivty= activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_studied, container, false);
        initView();
        initData();
        return mView;
    }

    private void initView() {

    }

    private void initData() {

    }
}

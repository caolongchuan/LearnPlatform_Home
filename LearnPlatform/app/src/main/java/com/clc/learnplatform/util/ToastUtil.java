package com.clc.learnplatform.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.ImageView;
import android.widget.Toast;

public class ToastUtil {
    @SuppressLint("StaticFieldLeak")
    private static ToastUtil Instance = null;
    private Context mContext;

    private ToastUtil(){
    }

    public static ToastUtil getInstance(){
        if(null == Instance){
            Instance = new ToastUtil();
        }
        return Instance;
    }

    public void init(Context context){
        mContext =context;
    }

    public void shortShow(String msg){
        Toast.makeText(mContext,msg,Toast.LENGTH_SHORT).show();
    }

    public void longShow(String msg){
        Toast.makeText(mContext,msg,Toast.LENGTH_LONG).show();
    }
}

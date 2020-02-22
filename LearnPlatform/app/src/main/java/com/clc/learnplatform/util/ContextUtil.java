package com.clc.learnplatform.util;

import android.content.Context;

public class ContextUtil {

    private Context mContext;
    private static ContextUtil instance = null;

    private ContextUtil(){

    }

    public static ContextUtil getInstance(){
        if(null == instance){
            synchronized (TTSUtils.class) {
                if (instance == null) {
                    instance = new ContextUtil();
                }
            }
        }
        return instance;
    }

    public void init(Context context){
        mContext = context;
    }

    public Context getmContext(){
        return mContext;
    }
}

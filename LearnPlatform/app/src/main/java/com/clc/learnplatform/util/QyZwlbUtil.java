package com.clc.learnplatform.util;

import com.clc.learnplatform.entity.SHENG_Entity;
import com.clc.learnplatform.entity.ZYLB_Entity;

import java.util.ArrayList;

/**
 * 区域与职位类别util 用于选择所在城市与职位类别
 */
public class QyZwlbUtil {
    private static QyZwlbUtil Instance = null;
    private ArrayList<SHENG_Entity> mShengEntityList;//省名称集合
    private ArrayList<ZYLB_Entity> mZylbEntityList;//职业类别集合

    private QyZwlbUtil(){
//        mShengEntityList = new ArrayList<>();
//        mZylbEntityList = new ArrayList<>();
    }

    public static QyZwlbUtil getInstance(){
        if(null == Instance){
            Instance = new QyZwlbUtil();
        }
        return Instance;
    }

    public void setShengList(ArrayList<SHENG_Entity> list){
        mShengEntityList = list;
    }

    public ArrayList<SHENG_Entity> getShengList(){
        return mShengEntityList;
    }

    public void setZwlbList(ArrayList<ZYLB_Entity> list){
        mZylbEntityList = list;
    }

    public ArrayList<ZYLB_Entity> getZylbList(){
        return mZylbEntityList;
    }

}

package com.clc.learnplatform.baidu;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.clc.learnplatform.activity.LoginActivity;

public class LocationUtil {
    private static final String TAG = "LocationUtil------";
    private Context mContext;
    private Handler mHandler;
    private LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();

    private String mProvince = null;//省
    private String mCity = null;//市
    private String mAddrString = null;//省加市

    //BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口
    //原有BDLocationListener接口暂时同步保留。具体介绍请参考后文中的说明

    public LocationUtil(Context context,Handler handler) {
        mContext = context;
        mHandler = handler;
        init();
    }

    public void startLocation(){
        if (null != mLocationClient) {
            mLocationClient.start();
        }
    }

    /**
     * 返回省
     * @return
     */
    public String getmProvince() {
        return mProvince;
    }

    /**
     * 返回市
     * @return
     */
    public String getmCity(){
        return mCity;
    }

    public String getmAddrString(){
        return mAddrString;
    }

    private void init() {
        mLocationClient = new LocationClient(mContext);
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数

        LocationClientOption option = new LocationClientOption();

        option.setIsNeedAddress(true);
        //可选，是否需要地址信息，默认为不需要，即参数为false
        //如果开发者需要获得当前点的地址信息，此处必须为true

        mLocationClient.setLocOption(option);
        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
    }


    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取地址相关的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            String addr = location.getAddrStr();    //获取详细地址信息
            String country = location.getCountry();    //获取国家
            String province = location.getProvince();    //获取省份
            mProvince = province;
            String city = location.getCity();    //获取城市
            mCity = city;
            String district = location.getDistrict();    //获取区县
            String street = location.getStreet();    //获取街道信息
            Log.i(TAG, "onReceiveLocation: province" + province);
            Log.i(TAG, "onReceiveLocation: city" + city);
            mAddrString = province + " " + city;
            Message msg = new Message();
            msg.what = 0x02;
            Bundle bundle = new Bundle();
            bundle.putString("province", province);
            bundle.putString("city", city);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            mLocationClient.stop();
        }
    }
}
/*
输入密钥库口令:
        密钥库类型: jks
        密钥库提供方: SUN

        您的密钥库包含 1 个条目

        别名: androiddebugkey
        创建日期: 2019-9-30
        条目类型: PrivateKeyEntry
        证书链长度: 1
        证书[1]:
        所有者: C=US, O=Android, CN=Android Debug
        发布者: C=US, O=Android, CN=Android Debug
        序列号: 1
        有效期为 Mon Sep 30 16:30:32 CST 2019 至 Wed Sep 22 16:30:32 CST 2049
        证书指纹:
        MD5:  AC:C1:A9:97:BF:0D:BE:34:CC:12:D7:DA:EA:DD:E0:89
        SHA1: 6B:52:49:22:90:13:58:30:E0:95:2B:0A:7C:0A:BF:20:22:AC:97:30
        SHA256: 7C:1F:C5:B9:F5:35:AA:90:BA:4A:61:FD:B3:0E:4E:A4:78:D7:77:40:C6:
        1:66:06:33:68:B5:34:AC:60:94:92
        签名算法名称: SHA1withRSA
        主体公共密钥算法: 1024 位 RSA 密钥
        版本: 1


        ******************************************
        ******************************************
        安全码：6B:52:49:22:90:13:58:30:E0:95:2B:0A:7C:0A:BF:20:22:AC:97:30;com.clc.learnplatform
        应用AK：aHCwXw5pfhBj6bM2GjTPsggubM7FzIOA
        应用名称：
        LearnPlatform
        应用类型：Android端
*/

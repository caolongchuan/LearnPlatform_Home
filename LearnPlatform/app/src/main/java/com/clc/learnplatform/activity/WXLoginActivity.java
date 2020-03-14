package com.clc.learnplatform.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.clc.learnplatform.R;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.ContextUtil;
import com.clc.learnplatform.util.SPUtils;
import com.clc.learnplatform.util.TTSUtils;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechUtility;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 微信登录界面
 */
public class WXLoginActivity extends AppCompatActivity {
    private static final String TAG = "WXLoginActivity";
    private IWXAPI wxapi;

    private RelativeLayout rlMain;
    private TextView mWXLogin;//微信登陆按钮
    //讯飞语音合成 权限
    private List<String> permissionList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //方式二：这句代码必须写在setContentView()方法的前面
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_wxlogin);

//        //临时初始化微信用户信息 假数据 用于调试
//        SPUtils.put(this, "nickname", "魔鬼的羽毛");
//        SPUtils.put(this, "headimgurl", "http://thirdwx.qlogo.cn/mmopen/vi_32/pHLZ8R6Qs4piaLcYyHIEVGOiax6uKVmmYBtzCwZ9jCY3SgZ3IFBsTZlatUbo21IwzztJOVOlOydtHXsOxVUKnQSw/132");
//        SPUtils.put(this, "openid", "oGVIZxFp2DOIPKoZM8cVxMbGwXjI");
//        SPUtils.put(this, "unionid", "okBs11VZKzoiNzOiPHT1Q7CXufqU");
//        String nickname = (String) SPUtils.get(this, "nickname", "");
//        String headimgurl = (String) SPUtils.get(this, "headimgurl", "");
//        String openid = (String) SPUtils.get(this, "openid", "");
//        String unionid = (String) SPUtils.get(this, "unionid", "");
//        Log.i(TAG, "onCreate: nickname=" + nickname);
//        Log.i(TAG, "onCreate: headimgurl=" + headimgurl);
//        Log.i(TAG, "onCreate: openid=" + openid);
//        Log.i(TAG, "onCreate: unionid=" + unionid);
//
//        if (!nickname.equals("") && !headimgurl.equals("") && !unionid.equals("")) {//已经登陆过直接进入下一个页面
//            gotoLoginActivity(nickname,headimgurl,unionid);
//        } else {
//            regToWx();//进入微信登录授权页面
//        }

        initView();
        initData();

    }

    private void initData() {
        SharedPreferences.Editor editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
        editor.putString("responseInfo", "");

        initTTSPermissions();//初始化讯飞语音合成
        mWXLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlMain.setVisibility(View.INVISIBLE);
                regToWx();//进入微信登录授权页面
            }
        });
    }

    private void initView() {
        rlMain =  findViewById(R.id.rl_main);
        mWXLogin = findViewById(R.id.tv_login);
    }

    //初始化讯飞动态申请权限
    private void initTTSPermissions() {
        SystemClock.sleep(1000);//延时加载 讯飞语音合成权限动态申请
        requestPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //获取授权状态
        String shouquan = (String) SPUtils.get(getApplicationContext(), "shouquan", "true");
        if(shouquan.equals("false")){
            finish();
        }

        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        String responseInfo = sp.getString("responseInfo", "");

        if (!responseInfo.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(responseInfo);
                String nickname = jsonObject.getString("nickname");
                String headimgurl = jsonObject.getString("headimgurl");
                String openid = jsonObject.getString("openid");
                String unionid = jsonObject.getString("unionid");
//                String openid = (String) SPUtils.get(this, "openid", "");
                SPUtils.put(this, "nickname", nickname);
                SPUtils.put(this, "headimgurl", headimgurl);
                SPUtils.put(this, "openid", openid);
                SPUtils.put(this, "unionid", unionid);
                Log.i(TAG, "onResume: nickname=====" + nickname);
                Log.i(TAG, "onResume: headimgurl=====" + headimgurl);
                Log.i(TAG, "onResume: openid=====" + openid);
                Log.i(TAG, "onResume: unionid=====" + unionid);
                //跳转到登录页面
                gotoLoginActivity(nickname,headimgurl,unionid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SharedPreferences.Editor editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
            editor.clear();
            editor.commit();
        }
    }

    //微信登录
    private void regToWx() {
        wxapi = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);
        wxapi.registerApp(Constants.APP_ID);
        if (!wxapi.isWXAppInstalled()) {
            Toast.makeText(WXLoginActivity.this, "您的设备未安装微信客户端", Toast.LENGTH_SHORT).show();
        } else {
            final SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = "wechat_sdk_demo_test";
            wxapi.sendReq(req);
        }
    }

    //权限申请
    private void requestPermissions() {
        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addListPermission();
            boolean isGranted = false;//是否全部授权
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            Iterator<String> iterator = permissionList.iterator();
            while (iterator.hasNext()) {
                // 检查该权限是否已经获取
                int granted = ContextCompat.checkSelfPermission(this, iterator.next());
                if (granted == PackageManager.PERMISSION_GRANTED) {
                    iterator.remove();//已授权则remove
                }
            }
            if (permissionList.size() > 0) {
                // 如果没有授予该权限，就去提示用户请求
                //将List转为数组
                String[] permissions = permissionList.toArray(new String[permissionList.size()]);
                // 开始提交请求权限
                ActivityCompat.requestPermissions(this, permissions, 0x10);
            } else {
                Log.i("zhh", "权限已申请");
                Toast.makeText(this, "权限已申请", Toast.LENGTH_SHORT).show();
//                openActivity(MainActivity.class);
                initTTS(ContextUtil.getInstance().getmContext());
            }

        } else {
//            openActivity(MainActivity.class);
            initTTS(ContextUtil.getInstance().getmContext());
        }
    }

    private boolean ifGrantResult(int[] grants) {
        boolean isGrant = true;
        for (int grant : grants) {
            if (grant == PackageManager.PERMISSION_DENIED) {
                isGrant = false;
                break;
            }
        }
        return isGrant;
    }

    /**
     * 权限申请返回结果
     *
     * @param requestCode  请求码
     * @param permissions  权限数组
     * @param grantResults 申请结果数组，里面都是int类型的数
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0x10:
                if (grantResults.length > 0 && ifGrantResult(grantResults)) {
                    Toast.makeText(this, "同意权限申请", Toast.LENGTH_SHORT).show();
                    initTTS(ContextUtil.getInstance().getmContext());
//                    openActivity(MainActivity.class);
                } else {
                    Toast.makeText(this, "权限被拒绝了", Toast.LENGTH_SHORT).show();
//                    finish();
                }
                break;
            default:
                break;
        }
    }

    //敏感权限添加
    private void addListPermission() {
        if (null == permissionList) {
            permissionList = new ArrayList<>();
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }
    }

    //初始化语音合成
    private void initTTS(Context context) {
        //讯飞语音播报平台
        SpeechUtility.createUtility(this, "appid=" + Constants.XF_APP_ID);//=号后面写自己应用的APPID
        Setting.setShowLog(true); //设置日志开关（默认为true），设置成false时关闭语音云SDK日志打印
        TTSUtils.getInstance().init(context); //初始化工具类
    }

    //跳转到登录页面 (此方法所接收到的参数openid已经是unionid了)
    private void gotoLoginActivity(String nickname,String headimgurl,String openid){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("nickname", nickname);
        bundle.putString("headimgurl", headimgurl);
        bundle.putString("openid", openid);
        intent.putExtras(bundle);
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}

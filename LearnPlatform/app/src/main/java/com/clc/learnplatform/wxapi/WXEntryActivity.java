package com.clc.learnplatform.wxapi;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.clc.learnplatform.R;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.SPUtils;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 微信授权登陆回调
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private static final String TAG = "WXEntryActivity";

    private Context mContext;
//    private ProgressDialog mProgressDialog;
    private IWXAPI wxapi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wxentry);
//        getSupportActionBar().hide();
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //接收到分享以及登录的intent传递handleIntent方法，处理结果
        wxapi = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);
        wxapi.handleIntent(getIntent(), this);
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    //请求回调结果处理
    @Override
    public void onResp(BaseResp baseResp) {
        //这里的话我就去拿到BaseResp.getType().去判断去做处理
        //当然我这里判断拿出来的 分享的返回值是
        //BaseResp.getType() == 1;则为微信登陆，
        //BaseResp.getType() == 2;则为微信分享。
        int type = baseResp.getType();
        if(baseResp.getType() == 1){
            Log.i(TAG, "onResp: 登陆的回调调用了");
            //登录回调
            switch (baseResp.errCode) {
                case BaseResp.ErrCode.ERR_OK:
                    String code = ((SendAuth.Resp) baseResp).code;
                    SPUtils.put(getApplicationContext(),"shouquan","true");
                    //获取accesstoken
                    getAccessToken(code);
                    Log.d("fantasychongwxlogin", code.toString() + "");
                    break;
                case BaseResp.ErrCode.ERR_AUTH_DENIED://用户拒绝授权
                    SPUtils.put(getApplicationContext(),"shouquan","false");
                    finish();
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL://用户取消
                    SPUtils.put(getApplicationContext(),"shouquan","false");
                    finish();
                    break;
                default:
                    finish();
                    break;
            }
        }else if(baseResp.getType() == 2) {
            Log.i(TAG, "onResp: 分享的回调调用了");
            //分享回调
            switch (baseResp.errCode) {
                case BaseResp.ErrCode.ERR_OK:
                    finish();
                    break;
                case BaseResp.ErrCode.ERR_AUTH_DENIED://用户拒绝
                    finish();
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL://用户取消
                    finish();
                    break;
                default:
                    finish();
                    break;
            }
        }

    }

    private void getAccessToken(String code) {
        createProgressDialog();
        //获取授权
        StringBuffer loginUrl = new StringBuffer();
        loginUrl.append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=")
                .append(Constants.APP_ID)
                .append("&secret=")
                .append(Constants.SECRET)
                .append("&code=")
                .append(code)
                .append("&grant_type=authorization_code");
        Log.d("urlurl", loginUrl.toString());

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(loginUrl.toString())
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("fan12", "onFailure: ");
//                mProgressDialog.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.d("fan12", "onResponse: " + responseInfo);
                String openId = null;
                String access = null;
                String unionid = null;

                try {
                    JSONObject jsonObject = new JSONObject(responseInfo);
                    access = jsonObject.getString("access_token");
                    openId = jsonObject.getString("openid");
                    unionid = jsonObject.getString("unionid");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                getUserInfo(access, openId);
            }
        });
    }

    //这里我们在请求之前新建一个progressDialog，避免长时间白屏（因为在进行多次网络请求）造成卡死的假象
    private void createProgressDialog() {
        mContext = this;
//        mProgressDialog = new ProgressDialog(mContext);
//        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//转盘
//        mProgressDialog.setCancelable(false);
//        mProgressDialog.setCanceledOnTouchOutside(false);
//        mProgressDialog.setTitle("提示");
//        mProgressDialog.setMessage("登录中，请稍后");
//        mProgressDialog.show();
    }

    //如果请求成功，我们通过JSON解析获取access和token值，再通过getUserInfo(access, openId)方法获取用户信息
    private void getUserInfo(String access, String openid) {
        //将openid写入sp里边
//        SPUtils.put(this,"openid",openid);
        String getUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=" + access + "&openid=" + openid;
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(getUserInfoUrl)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("fan12", "onFailure: ");
//                mProgressDialog.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseInfo = response.body().string();
                Log.d("fan123", "onResponse: " + responseInfo);
                SharedPreferences.Editor editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                editor.putString("responseInfo", responseInfo);
                editor.commit();
                finish();
//                mProgressDialog.dismiss();
            }
        });
    }


}

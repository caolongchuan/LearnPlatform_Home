package com.clc.learnplatform.wxapi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.clc.learnplatform.R;
import com.clc.learnplatform.global.Constants;
import com.clc.learnplatform.util.SPUtils;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * 微信支付的回调
 */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "WXPayEntryActivity";

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);

        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    @Override
    public void onResp(BaseResp resp) {
        Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK://支付成功
                //夺取出支付前保存起来的学习币值
                String coin_num = (String) SPUtils.get(getApplicationContext(), "coin_num", "");
                int coin_num1 = Integer.valueOf(coin_num);//
                Log.i(TAG, "onResp: coin_num1=" + coin_num1);
                //添加进sp里边
                int coin_num2 = (int) SPUtils.get(getApplicationContext(), "COIN_NUM", 0);
                SPUtils.put(getApplicationContext(), "COIN_NUM", coin_num2 + coin_num1);
                SPUtils.put(getApplicationContext(), "coin_num", "");//重置为空字符串
                finish();
                break;
            case BaseResp.ErrCode.ERR_COMM://错误
                finish();
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL://用户取消
                finish();
                break;
        }
    }
}
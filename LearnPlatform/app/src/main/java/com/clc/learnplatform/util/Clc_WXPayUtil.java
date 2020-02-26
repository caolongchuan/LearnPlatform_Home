package com.clc.learnplatform.util;

import android.os.Message;
import android.util.Log;

import com.clc.learnplatform.global.Constants;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 自己生成的微信支付工具类
 */
public class Clc_WXPayUtil {
    private static final String TAG = "--Clc_WXPayUtil--";
    public static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    public static String TYXD_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";//统一下单接口

    /**
     * 统一下单
     * @param total_fee 支付金额（单位是分）
     * @param spbill_create_ip 外网IP
     */
    public static void TongYiXiaDan(int total_fee,String spbill_create_ip){
        String param = geneParam(total_fee,spbill_create_ip);
        if(null != param){
            String result = httpsRequest(TYXD_URL, "POST", param);
        }
    }

    /**
     * 生成统一下单xml参数
     * @param total_fee 支付金额（单位是分）
     * @param spbill_create_ip 外网IP
     * @return 生成的参数
     */
    public static String geneParam(int total_fee,String spbill_create_ip){
        String sign = "";
        Map<String, String> params = new HashMap<String, String>();
        params.put("appid", Constants.APP_ID);
        params.put("mch_id", Constants.MCH_ID);
//        params.put("nonce_str", gene_nonceStr());
        params.put("nonce_str", "e26ebc14178674225bce44d3010fc8ea");
        params.put("body", Constants.BODY);
//        params.put("out_trade_no", gene_out_trade_no());
        params.put("out_trade_no", "1582728837152");
        params.put("total_fee", String.valueOf(total_fee));
//        params.put("spbill_create_ip", spbill_create_ip);
        params.put("spbill_create_ip", "106.9.129.85");
        params.put("notify_url", Constants.NOTIFY_URL);
        params.put("trade_type", Constants.TRADE_TYPE);

        try {
            sign = WXPaySignUtil.Sign(params, Constants.SECRET);
            params.put("sign", sign);
            String XMLStr = GetMapToXML(params);

            return XMLStr;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Map转xml数据
     * @param param
     * @return
     */
    public static String GetMapToXML(Map<String, String> param) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        for (Map.Entry<String, String> entry : param.entrySet()) {
            sb.append("<" + entry.getKey() + ">");
            sb.append(entry.getValue());
            sb.append("</" + entry.getKey() + ">");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    //发起微信支付请求
    private static String httpsRequest(String requestUrl, String requestMethod, String outputStr) {
        String r = "httpsRequest-error";
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            // 设置请求方式（GET/POST）
            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            // 当outputStr不为null时向输出流写数据
            if (null != outputStr) {
                OutputStream outputStream = conn.getOutputStream();
                // 注意编码格式
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }
            // 从输入流读取返回内容
            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            StringBuffer buffer = new StringBuffer();
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            // 释放资源
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            inputStream = null;
            conn.disconnect();
            return buffer.toString();
        } catch (ConnectException ce) {
            System.out.println("连接超时：{}" + ce);
        } catch (Exception e) {
            System.out.println("https请求异常：{}" + e);
        }
        return r;
    }


    /**
     * 生成随机字符串
     * @return
     */
    private static String gene_nonceStr() {
        return MD5Utils.digest(String.valueOf(Math.random()));
    }

    /**
     * 生成商户系统内部订单号（暂时用时间戳作为订单号）
     * @return
     */
    private static String gene_out_trade_no(){
        return String.valueOf(new Date().getTime());
    }

}

package com.clc.learnplatform.util;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.clc.learnplatform.global.Constants;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
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
public class ClcWXPayUtil {
    private static final String TAG = "--ClcWXPayUtil--";
    public static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    public static String TYXD_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";//统一下单接口

    /**
     * 统一下单
     * @param total_fee 支付金额（单位是分）
     * @param spbill_create_ip 外网IP
     */
    public static void TongYiXiaDan(Context context, int total_fee,String spbill_create_ip){
        String param = geneParam(total_fee,spbill_create_ip);
        if(null != param){
            String result_xml = httpsRequest(TYXD_URL, "POST", param);
            if(null != result_xml){
                HashMap hashMap = readStringXmlOut(result_xml);
                if(hashMap.size()>0){//统一下单成功 获取到了prepay_id
                    String prepay_id = (String) hashMap.get("prepay_id");
                    //调起支付接口
                    ZhiFu(context, prepay_id);
                }
                int i=0;
                int j=1;
                int k = i+j;
            }
        }
    }

    /**
     * 支付
     * @param prepay_id 预支付交易会话ID
     */
    private static void ZhiFu(Context context, String prepay_id) {
        IWXAPI wxapi = WXAPIFactory.createWXAPI(context, Constants.APP_ID, false);
        wxapi.registerApp(Constants.APP_ID);
        PayReq request = new PayReq();
        request.appId = Constants.APP_ID;
        request.partnerId = Constants.MCH_ID;
        request.prepayId= prepay_id;
        request.packageValue = "Sign=WXPay";
        request.nonceStr= gene_nonceStr();
        request.timeStamp= gene_out_trade_no();

        Map<String, String> params = new HashMap<String, String>();
        params.put("appid", Constants.APP_ID);
        params.put("partnerid", Constants.MCH_ID);
        params.put("prepayid", request.prepayId);
        params.put("package", request.packageValue);
        params.put("noncestr", request.nonceStr);
        params.put("timestamp", request.timeStamp);

        try {
            request.sign = WXPaySignUtil.Sign(params, Constants.SECRET_KEY);
            wxapi.sendReq(request);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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
        params.put("nonce_str", gene_nonceStr());
        params.put("body", Constants.BODY);
        params.put("out_trade_no", gene_out_trade_no());
        params.put("total_fee", String.valueOf(total_fee));
        params.put("spbill_create_ip", spbill_create_ip);
        params.put("notify_url", Constants.NOTIFY_URL);
        params.put("trade_type", Constants.TRADE_TYPE);

        try {
            sign = WXPaySignUtil.Sign(params, Constants.SECRET_KEY);
            params.put("sign", sign);

            return GetMapToXML(params);
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

    /**
     * @param xml
     * @return Map
     * @description 将xml字符串转换成map
     */
    public static HashMap readStringXmlOut(String xml) {
        HashMap map = new HashMap();
        Document doc = null;
        try {
            // 将字符串转为XML
            doc = DocumentHelper.parseText(xml);
            // 获取根节点
            Element rootElt = doc.getRootElement();
            String name = rootElt.getName();
            // 拿到根节点的名称

            String return_code = rootElt.elementText("return_code");
            String return_msg = rootElt.elementText("return_msg");

            if (return_code.equals("SUCCESS")) {
//                String nonce_str = rootElt.elementText("nonce_str");
//                map.put("nonce_str", nonce_str);
//                String sign = rootElt.elementText("sign");
//                map.put("sign", sign);
                String result_code = rootElt.elementText("result_code");
                if (result_code.equals("SUCCESS")) {
                    String prepay_id = rootElt.elementText("prepay_id");
                    map.put("prepay_id", prepay_id);
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
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
     * 生成商户系统内部订单号（暂时用时间戳作为订单号 单位是秒）
     * @return
     */
    private static String gene_out_trade_no(){
        return String.valueOf(new Date().getTime()/1000);
    }

}

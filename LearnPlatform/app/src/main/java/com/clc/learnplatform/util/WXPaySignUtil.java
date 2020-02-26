package com.clc.learnplatform.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class WXPaySignUtil {
    /**
     * 构造签名
     * @param params
     * @param encode
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String createSign(Map<String, String> params, boolean encode) throws UnsupportedEncodingException {
        Set<String> keysSet = params.keySet();
        Object[] keys = keysSet.toArray();
        Arrays.sort(keys);
        StringBuffer temp = new StringBuffer();
        boolean first = true;
        for (Object key : keys) {
            if (first) {
                first = false;
            } else {
                temp.append("&");
            }
            temp.append(key).append("=");
            Object value = params.get(key);
            String valueString = "";
            if (null != value) {
                valueString = value.toString();
            }
            if (encode) {
                temp.append(URLEncoder.encode(valueString, "UTF-8"));
            } else {
                temp.append(valueString);
            }
        }
        return temp.toString();
    }

    /**
     * 构造签名
     * @param params
     * @param paternerKey
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String Sign(Map<String, String> params, String paternerKey) throws UnsupportedEncodingException {
        String string1 = createSign(params, false);
        String stringSignTemp = string1 + "&key=" + paternerKey;
        //String signValue = MD5Tools.encode(stringSignTemp).toUpperCase();
        //String signValue = DigestUtils.md5Hex(stringSignTemp).toUpperCase();

        return MD5Utils.digest(stringSignTemp).toUpperCase();
    }

    public static String WXShapeSign(Map<String, String> params) throws UnsupportedEncodingException {
        String string1 = createSign(params,false);
        return Sha1Util.getSha1(string1);
    }

}

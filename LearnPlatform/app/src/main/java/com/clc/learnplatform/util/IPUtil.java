package com.clc.learnplatform.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * 获取ip地址的工具类
 */
public class IPUtil {

    /**
     * 获取外网的IP(必须放到子线程里处理)
     */
    public static String GetNetIp() {
        String ip = "";
        InputStream inputStream = null;
        try {
            URL infoUrl = new URL("http://pv.sohu.com/cityjson?ie=utf-8");
            URLConnection connection = infoUrl.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "gb2312"));
                StringBuilder builder = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                inputStream.close();
                int start1=builder.indexOf(":");
                String str1=builder.substring(start1+1);
                int start2=str1.indexOf("\"");
                String str2=str1.substring(start2+1);
                int start3=str2.indexOf("\"");
                ip=str2.substring(0,start3);
                return ip;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}

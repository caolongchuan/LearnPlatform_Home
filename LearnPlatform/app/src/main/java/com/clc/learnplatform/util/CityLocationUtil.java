package com.clc.learnplatform.util;

import android.content.Context;
import android.graphics.Point;

import com.clc.learnplatform.fragment.MapFragment;
import com.clc.learnplatform.global.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

public class CityLocationUtil {

    /**
     * 根据城市名获取经纬度
     *
     * @param context
     * @param sheng 省的名字
     * @param shi_ 城市名（不带最后的市字）
     * @return 经纬度
     */
    public static double[] getCoordinate(Context context, String sheng, String shi_) {
        String shi = shi_.replace("市","");
        double d[] = new double[2];

        try {
            InputStreamReader isr = new InputStreamReader(
                    context.getAssets().open("city_location.txt"),"UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line;
            StringBuilder builder = new StringBuilder();
            while((line = br.readLine()) != null){
                builder.append(line);
            }
            br.close();
            isr.close();
            JSONArray shengJson = new JSONArray(builder.toString());//builder读取了JSON中的数据。
            for (int i=0;i<shengJson.length();i++){
                JSONObject sheng_json = shengJson.getJSONObject(i);
                String sheng_name = sheng_json.getString("name");
                if(sheng_name.equals(sheng)){
                    JSONArray children = sheng_json.getJSONArray("children");
                    for(int j=0;j<children.length();j++){
                        JSONObject shi_json = children.getJSONObject(j);
                        String shi_name = shi_json.getString("name");
                        if(shi_name.equals(shi)){
                            String log = shi_json.getString("log");
                            String lat = shi_json.getString("lat");
                            d[0] = Double.valueOf(log);
                            d[1] = Double.valueOf(lat);
                            return d;
                        }
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}

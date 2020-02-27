package com.clc.learnplatform.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.clc.learnplatform.R;
import com.clc.learnplatform.global.Constants;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * 自己的微信分享的工具类
 */
public class ClcWXShareUtil {

    /**
     * 分享网页 shape_type为1时是分享朋友圈 为2时是分享朋友
     */
    public static void ShareWebPage(Context context,String openid,
                                    String url,String title,
                                    String description,int bmp_id,int shape_type){
        IWXAPI wxapi = WXAPIFactory.createWXAPI(context, Constants.APP_ID, false);
        wxapi.registerApp(Constants.APP_ID);
        //初始化一个WXWebpageObject，填写url
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl =url;

        //用 WXWebpageObject 对象初始化一个 WXMediaMessage 对象
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title =title;
        msg.description =description;
        Bitmap thumbBmp = BitmapFactory.decodeResource(context.getResources(), bmp_id);
        msg.thumbData =WXUtil.bmpToByteArray(thumbBmp, true);

        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message =msg;
        //发送到聊天界面——WXSceneSession
        //发送到朋友圈——WXSceneTimeline
        if(shape_type == 1){//分享朋友圈
            req.scene = SendMessageToWX.Req.WXSceneTimeline;
        }else if(shape_type == 2){//分享朋友
            req.scene = SendMessageToWX.Req.WXSceneSession;
        }
        req.userOpenId = openid;
        //调用api接口，发送数据到微信
        wxapi.sendReq(req);
    }

    private static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
}

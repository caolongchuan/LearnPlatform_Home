package com.clc.learnplatform.global;

import com.clc.learnplatform.R;

public class Constants {

    public static final String BASE_URL = "http://www.zixiawangluo.com/zxexam/";
    //用于获取短信验证码的接口
    public static final String GET_SMS_YZM_URL = BASE_URL + "yh_fsdx?sjh=";
    //登录
    public static final String SIGN_IN_URL= BASE_URL + "yh_login";
    //注册
    public static final String REGISTER_URL = BASE_URL + "yh_add";
    //首页
    public static final String FIRST_PAGER_URL = BASE_URL + "yh_index";
    //项目学习
    public static final String ITEM_STUDIED_URL = BASE_URL + "xx_xx";
    //题库学习
    public static final String ITEM_BANK_URL = BASE_URL + "xx_tkxx";
    //模拟考试
    public static final String MNKS_URL = BASE_URL + "xx_mnks";
    //模拟考试答题保存
    public static final String MNKS_DTBC_URL = BASE_URL + "xx_dtbc";
    //模拟考试交卷
    public static final String JIAOJUAN_URL = BASE_URL + "xx_jiaojuan";
    //错题练习
    public static final String CTLX_URL = BASE_URL + "xx_ctlx";
    //错题答题正确
    public static final String CTLXDD_URL = BASE_URL + "xx_ctlxdd";
    //未做题练习
    public static final String WZTLX_URL = BASE_URL + "xx_wztlx";
    //未做题答题保存
    public static final String WZBC_URL = BASE_URL + "xx_wzbc";
    //搜题找答案
    public static final String XX_TOST_URL = BASE_URL + "xx_tost";
    //历史学习
    public static final String LSXX_URL = BASE_URL +"xx_lsxx";
    //账单明细
    public static final String ZDMX_URL = BASE_URL + "yh_zdmx";
    //获取充值选择
    public static final String CZ_URL = BASE_URL + "yh_cz";
    //绑定体验卡
    public static final String BGTYK_URL = BASE_URL + "yh_bdtyk";
    //学习卡列表
    public static final String XXK_URL = BASE_URL + "yh_xxk";
    //绑定学习卡
    public static final String BDXXK_URL = BASE_URL + "yh_bdxxk";
    //证书查询
    public static final String ZHENG_QUERY_URL = "http://www.cnse.gov.cn/Publicity/Staff";
    //问题反馈
    public static final String WTFK_URL = BASE_URL + "yh_wtfk";
    //考试机构
    public static final String KSJG_URL = BASE_URL + "yh_ksjg";
    //蓝领求职首页
    public static final String ZP_FIND_URL = BASE_URL + "zp_find";
    //实际操作查看
    public static final String XX_SJCZ_URL = BASE_URL + "xx_sjcz";
    //绑定学习卡页切换证书种类，查询此种类下的项目
    public static final String YH_GETXM_URL = BASE_URL + "yh_getxm";


    //百度地图 应用AK
    public static final String BAIDU_AK = "aHCwXw5pfhBj6bM2GjTPsggubM7FzIOA";//在AndroidManifest.xml文件夹中已使用

    //微信登录所使用的常量
    public static final String APP_ID = "wxfe78e87df435fca9";//appid
    public static final String SECRET = "5bfc713e6d516492fcdbaed9d61a5e6c";//
    public static final String MCH_ID = "1566319061";//商户号
    public static final String BODY = "特种设备考证学习平台-学习币充值";//商品描述
    public static final String NOTIFY_URL = "http://www.zixiawangluo.com/zxexam/yh_czreturn";//接收微信支付异步通知回调地址，通知url必须为直接可访问的url，不能携带参数
    public static final String TRADE_TYPE = "APP";
    public static final String SECRET_KEY = "sxzxsxzxsxzxsxzxsxzxsxzxsxzxsxzx";//用于支付的秘钥key

    //微信分享
    public static final String WX_SHAPE_URL = "www.baidu.com";//微信分享的url
    public static final String WX_SHAPE_TITLE = "微信分享的title";
    public static final String WX_SHAPE_DESCRIPTION = "微信分享的描述";
    public static final int WX_SHAPE_BMP = R.mipmap.ic_launcher;

    //讯飞语音合成APPID
    public static final String XF_APP_ID = "5e0d9d44";

    //运费语音时间系数 需要阅读的字数*0.241 = 读完所需要的时间（秒）
    public static final double TIME_XISHU = 0.241;

    //验证码的有效期 在有效期内设这获取验证码按钮不可以（默认是5分钟 也就是300秒）
    public static final int YZM_TIME = 300;
}

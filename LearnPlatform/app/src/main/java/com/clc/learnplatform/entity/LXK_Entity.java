package com.clc.learnplatform.entity;

/**
 * 学习卡
 */
public class LXK_Entity {
    public String KH;//卡号
    public String MM;//密码
    public String ZT;//状态 00未绑定 01已绑定
    public String BDSJ;//绑定时间 精确到时分秒
    public int XXB;//学习币数量
    public String YHID;//用户id
    public String LX;//类型 01体验卡 02学习卡
    public String ZDTGY;//指定推广员
    public int YXTS;//有效天数
    public String XMID;//项目id
    public String YXQ;//有效期
    public int STCS;//搜题次数
}

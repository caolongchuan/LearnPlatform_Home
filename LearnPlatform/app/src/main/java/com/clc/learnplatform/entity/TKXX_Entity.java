package com.clc.learnplatform.entity;

/**
 * 实际操作list（SJCZ）与项目分类list(XMFL)(实体包含附加属性TKXX，
 * 不为null即存在已支付并未到期的查看记录)
 */
public class TKXX_Entity {
    public String ID;//
    public String XMID;//项目ID
    public String YHID;//用户ID
    public String FLID;//分类ID
    public String KSSJ;//开始时间
    public String JSSJ;//结束时间
    public int DQYS;//当前页数
    public String LX;//类型 00知识点 01实际操作
    public String ZT;//状态 00未过期 01已过期
}

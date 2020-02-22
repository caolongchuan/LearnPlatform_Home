package com.clc.learnplatform.entity;

/**
 * 账单明细
 */
public class ZDMX_Entity {
    public String ID;//
    public String YHID;//用户ID
    public String ZFSJ;//支付时间
    public int FY;//费用
    public String LX;//类型 00充值 01消耗
    public String SFKP;//是否已开票 00未开票 01已开票
    public String FPH;//发票号
    public String DDH;//
    public String SM;//说明（工业锅炉 法律知识消耗）
    public String ZFZT;//支付状态 00未支付 01已支付
}

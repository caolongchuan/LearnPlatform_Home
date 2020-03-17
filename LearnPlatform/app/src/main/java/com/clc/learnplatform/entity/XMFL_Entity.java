package com.clc.learnplatform.entity;

/**
 * 项目分类
 */
public class XMFL_Entity {
    public int ID;//
    public String XMID;//项目ID
    public String FLNC;//分类名称
    public int CTS;//抽题数
    public int ZTL;//总题量
    public int ZFZ;//总分值
    public String ZT;//状态 00可用 01禁用
    public int XSFZ;//限时分钟
    public int ZZDTS;//知识点条数
    public int ZZDXH;//知识点消耗

    public TKXX_Entity TKXX;//不为null即存在已支付并未到期的查看记录

}

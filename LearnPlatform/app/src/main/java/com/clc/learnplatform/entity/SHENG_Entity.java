package com.clc.learnplatform.entity;

import java.util.ArrayList;

/**
 * 省
 */
public class SHENG_Entity {
    public String id;//id
    public String value;//值 也就是省的名称
    public ArrayList<SHI_Entity> childs;//所包含的市
}

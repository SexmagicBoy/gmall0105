package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;

import java.util.List;
import java.util.Set;

public interface PmsBaseAttrService {

    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    /**
     * 保存平台属性和属性值列表或平台商品属性列表
     * @param pmsBaseAttrInfo
     * @return
     */
    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    List<PmsBaseAttrInfo> getAttrValueListByValueIds(Set<String> valueIdSet);
}

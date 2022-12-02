package com.atguigu.gmall.manage.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsSkuAttrValue;
import com.atguigu.gmall.bean.PmsSkuImage;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuImageMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    private PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    private PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    private PmsSkuImageMapper pmsSkuImageMapper;

    @Override
    @Transactional
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo) {

        // 处理默认图片
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        if (CollectionUtils.isEmpty(skuImageList)) {
            return "请设置默认图片后再提交";
        }

        String skuDefaultImg = pmsSkuInfo.getSkuDefaultImg();
        if (StringUtils.isEmpty(skuDefaultImg)) {
            pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getSkuImageList().get(0).getImgUrl());
            pmsSkuInfo.getSkuImageList().get(0).setIsDefault("1");
        }

        // 将 spuId 封装给 productId
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());


        // 插入sku信息
        int count = pmsSkuInfoMapper.insert(pmsSkuInfo);
        String pmsSkuInfoId = pmsSkuInfo.getId();

        // 插入平台属性关联
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                pmsSkuAttrValue.setSkuId(pmsSkuInfoId);
                pmsSkuAttrValueMapper.insert(pmsSkuAttrValue);
            }
        }

        // 插入销售属性关联
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                pmsSkuSaleAttrValue.setSkuId(pmsSkuInfoId);
                pmsSkuSaleAttrValueMapper.insert(pmsSkuSaleAttrValue);
            }
        }

        // 插入图片信息

        if (!CollectionUtils.isEmpty(skuImageList)) {
            for (PmsSkuImage pmsSkuImage : skuImageList) {
                pmsSkuImage.setProductImgId(pmsSkuImage.getSpuImgId());
                pmsSkuImage.setSkuId(pmsSkuInfoId);
                pmsSkuImageMapper.insert(pmsSkuImage);
            }
        }
        return "success";
    }
}

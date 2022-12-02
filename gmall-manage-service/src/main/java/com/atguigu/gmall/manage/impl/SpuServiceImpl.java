package com.atguigu.gmall.manage.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsProductImage;
import com.atguigu.gmall.bean.PmsProductInfo;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.bean.PmsProductSaleAttrValue;
import com.atguigu.gmall.manage.mapper.PmsProductImageMapper;
import com.atguigu.gmall.manage.mapper.PmsProductInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsProductSaleAttrMapper;
import com.atguigu.gmall.manage.mapper.PmsProductSaleAttrValueMapper;
import com.atguigu.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private PmsProductInfoMapper pmsProductInfoMapper;

    @Autowired
    private PmsProductImageMapper pmsProductImageMapper;

    @Autowired
    private PmsProductSaleAttrMapper pmsProductSaleAttrMapper;

    @Autowired
    private PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;

    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {
        Example example = new Example(PmsProductInfo.class);
        example.createCriteria().andEqualTo("catalog3Id", catalog3Id);
        return pmsProductInfoMapper.selectByExample(example);
    }

    @Override
    @Transactional
    public String saveSpuInfo(PmsProductInfo pmsProductInfo) {
        // 保存 spu
        pmsProductInfoMapper.insert(pmsProductInfo);

        // 保存 spuImages
        List<PmsProductImage> spuImageList = pmsProductInfo.getSpuImageList();
        if (!CollectionUtils.isEmpty(spuImageList)) {
            for (PmsProductImage pmsProductImage : spuImageList) {
                pmsProductImage.setProductId(pmsProductInfo.getId());
                pmsProductImageMapper.insert(pmsProductImage);
            }
        }

        // 保存销售属性和销售属性值
        List<PmsProductSaleAttr> spuSaleAttrList = pmsProductInfo.getSpuSaleAttrList();
        if (!CollectionUtils.isEmpty(spuSaleAttrList)) {
            for (PmsProductSaleAttr pmsProductSaleAttr : spuSaleAttrList) {
                pmsProductSaleAttr.setProductId(pmsProductInfo.getId());
                pmsProductSaleAttrMapper.insert(pmsProductSaleAttr);

                List<PmsProductSaleAttrValue> spuSaleAttrValueList = pmsProductSaleAttr.getSpuSaleAttrValueList();
                if (!CollectionUtils.isEmpty(spuSaleAttrValueList)) {
                    for (PmsProductSaleAttrValue pmsProductSaleAttrValue : spuSaleAttrValueList) {
                        pmsProductSaleAttrValue.setProductId(pmsProductInfo.getId());
                        pmsProductSaleAttrValueMapper.insert(pmsProductSaleAttrValue);
                    }
                }
            }
        }

        return "success";
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        // 查询销售属性
        Example saleAttrExample = new Example(PmsProductSaleAttr.class);
        saleAttrExample.createCriteria().andEqualTo("productId", spuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.selectByExample(saleAttrExample);

        // 根据销售属性查询值列表并封装
        List<PmsProductSaleAttr> vos = null;
        if (!CollectionUtils.isEmpty(pmsProductSaleAttrs)) {
            vos = new ArrayList<>();
            for (PmsProductSaleAttr pmsProductSaleAttr : pmsProductSaleAttrs) {
                Example saleAttrValueExample = new Example(PmsProductSaleAttrValue.class);
                saleAttrValueExample.createCriteria()
                        .andEqualTo("productId", spuId)
                        .andEqualTo("saleAttrId", pmsProductSaleAttr.getSaleAttrId());
                List<PmsProductSaleAttrValue> pmsProductSaleAttrValues = pmsProductSaleAttrValueMapper.selectByExample(saleAttrValueExample);
                pmsProductSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValues);
                vos.add(pmsProductSaleAttr);
            }
        }

        return vos;
    }

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {
        Example example = new Example(PmsProductImage.class);
        example.createCriteria().andEqualTo("productId", spuId);
        return pmsProductImageMapper.selectByExample(example);
    }
}

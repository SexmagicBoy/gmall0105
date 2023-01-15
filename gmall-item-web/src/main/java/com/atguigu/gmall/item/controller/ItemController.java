package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.PmsBaseAttr;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@CrossOrigin
@Controller
public class ItemController {

    @Reference
    private SkuService skuService;

    @Reference
    private PmsBaseAttr spuService;

    /**
     * 测试方法
     *
     * @param modelMap
     * @return
     */
    @RequestMapping("index")
    public String index(ModelMap modelMap) {
        modelMap.put("hello", "hello thymeleaf !!");

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("循环数据" + i);
        }

        modelMap.put("list", list);

        modelMap.put("check", "1");
        return "index";
    }

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap modelMap, HttpServletRequest request) {
        // 从请求中获取 ip
        String ip = request.getRemoteAddr();

        // 在页面封装 sku 对象
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId, ip);
        modelMap.put("skuInfo", pmsSkuInfo);

        // 在页面封装销售属性列表
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.getSpuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(), pmsSkuInfo.getId());
        modelMap.put("spuSaleAttrListCheckBySku", pmsProductSaleAttrs);

        // 封装当前 sku 的 spu 下所有的 sku 的集合的 hash 表
        HashMap<String, String> skuSaleAttrHash = new HashMap<>();
        List<PmsSkuInfo> pmsSkuInfos = skuService.getSkuSaleAttrValueListBySpu(pmsSkuInfo.getProductId());
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String entryValue = skuInfo.getId();
            StringBuilder entryKey = new StringBuilder();
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                entryKey.append(pmsSkuSaleAttrValue.getSaleAttrValueId()).append("|");
            }
            skuSaleAttrHash.put(entryKey.toString(), entryValue);
        }
        // 将 sku 销售属性 hash 表放到页面
        String skuSaleAttrHashJsonString = JSON.toJSONString(skuSaleAttrHash);
        modelMap.put("skuSaleAttrHashJsonString", skuSaleAttrHashJsonString);
        return "item";
    }
}

package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.PmsBaseAttrService;
import com.atguigu.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Arrays;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.*;

@Controller
public class SearchController {

    @Reference
    private SearchService searchService;

    @Reference
    private PmsBaseAttrService pmsBaseAttrService;

    @RequestMapping("index")
    @LoginRequired(mustLogin = false)
    public String index() {
        return "index";
    }

    /**
     * 查询 spu 列表
     * 可能出现的参数：三级分类 id | 关键字 | sku 属性值列表
     * 其中三级分类 id 和关键字不会同时出现
     *
     * @param pmsSearchParam 查询参数
     * @return 平台属性列表页面
     */
    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap) throws IOException {
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList", pmsSearchSkuInfos);

        // 抽取检索结果所包含的平台属性的集合
        Set<String> valueIdSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }

        // 根据 valueId 将属性列表查询出来
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrService.getAttrValueListByValueIds(valueIdSet);
        // 平台属性组
        String[] delValueIds = pmsSearchParam.getValueId();
        if (!Arrays.isNullOrEmpty(delValueIds)) {
            // 如果当前请求中包含属性的参数，每一个属性参数，都会生成一个面包屑
            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
            for (String delValueId : delValueIds) {
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                // 生成面包屑的参数
                pmsSearchCrumb.setValueId(delValueId);
                pmsSearchCrumb.setUrlParam(getUrlParam(pmsSearchParam, modelMap, delValueId));

                // 排除被选中的平台属性组
                Iterator<PmsBaseAttrInfo> pmsBaseAttrInfoIterator = pmsBaseAttrInfos.iterator();
                while (pmsBaseAttrInfoIterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = pmsBaseAttrInfoIterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String valueId = pmsBaseAttrValue.getId();
                        if (delValueId.equals(valueId)) {
                            pmsBaseAttrInfoIterator.remove();
                            // 补全面包屑参数名称
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            break;
                        }
                    }
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
            modelMap.put("attrValueSelectedList", pmsSearchCrumbs);
        }
        modelMap.put("attrList", pmsBaseAttrInfos);

        // 生成 url
        String urlParam = getUrlParam(pmsSearchParam, modelMap, null);
        modelMap.put("urlParam", urlParam);
        return "list";
    }

    /**
     * 完成 url 属性列表的拼接
     *
     * @param pmsSearchParam 查询参数
     * @param modelMap       给 thymeleaf 传递对象模型
     * @param delValueId     仅制作删除面包屑之后的 url 需要这个步骤
     * @return 链接上的平台属性参数列表
     */
    private String getUrlParam(PmsSearchParam pmsSearchParam, ModelMap modelMap, String delValueId) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] valueIds = pmsSearchParam.getValueId();

        StringBuilder urlParam = new StringBuilder();
        if (StringUtils.isNotBlank(keyword)) {
            urlParam.append("keyword=").append(keyword);
            modelMap.put("keyword", keyword);
        }

        if (StringUtils.isNotBlank(catalog3Id)) {
            urlParam.append("catalog3Id=").append(catalog3Id);
        }

        if (!Arrays.isNullOrEmpty(valueIds)) {
            if (StringUtils.isNotBlank(delValueId)) {
                for (String valueId : valueIds) {
                    if (!delValueId.equals(valueId)) {
                        urlParam.append("&valueId=").append(valueId);
                    }
                }
            } else {
                for (String valueId : valueIds) {
                    urlParam.append("&valueId=").append(valueId);
                }
            }
        }

        return urlParam.toString();
    }
}

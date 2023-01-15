package com.atguigu.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsSearchParam;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.bean.PmsSkuAttrValue;
import com.atguigu.gmall.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Arrays;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private JestClient jestClient;

    @Override
    public List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam) throws IOException {
        // 1、准备 dsl
        SearchSourceBuilder searchSourceBuilder = getSearchSourceBuilder(pmsSearchParam);

        // 2、发送请求
        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex("gmall0105pms").addType("pmsSkuInfo").build();
        SearchResult searchResult = jestClient.execute(search);

        // 3、解析结果并返回
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = searchResult.getHits(PmsSearchSkuInfo.class);
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            // 处理高亮
            PmsSearchSkuInfo searchSkuInfo = hit.source;
            Map<String, List<String>> highlight = hit.highlight;
            if (highlight != null){
                searchSkuInfo.setSkuName(highlight.get("skuName").toString());
            }
            pmsSearchSkuInfos.add(searchSkuInfo);
        }
        return pmsSearchSkuInfos;
    }

    private SearchSourceBuilder getSearchSourceBuilder(PmsSearchParam pmsSearchParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        // must
        if (StringUtils.isNoneEmpty(pmsSearchParam.getKeyword())) {
            // 关键字
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", pmsSearchParam.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);
            // 高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            // 将原始高量标签替换为红色
            highlightBuilder.preTags("<span style='color:red;'>");
            highlightBuilder.postTags("</span style='color:red;'>");
            highlightBuilder.field("skuName");
            searchSourceBuilder.highlight(highlightBuilder);
        }

        // filter
        String[] valueIds = pmsSearchParam.getValueId();
        if (!Arrays.isNullOrEmpty(valueIds)){
            for (String valueId : valueIds) {
                // sku 属性 id
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        if (StringUtils.isNoneEmpty(pmsSearchParam.getCatalog3Id())) {
            // 3 级分类 id
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", pmsSearchParam.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }
        searchSourceBuilder.query(boolQueryBuilder);
        // sort
        searchSourceBuilder.sort("hotScore", SortOrder.ASC);
        // from
        searchSourceBuilder.from(0);
        // size
        searchSourceBuilder.size(20);
        return searchSourceBuilder;
    }
}

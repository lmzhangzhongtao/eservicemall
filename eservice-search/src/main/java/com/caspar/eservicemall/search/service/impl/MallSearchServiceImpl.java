package com.caspar.eservicemall.search.service.impl;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.NestedAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.ObjectBuilder;
import com.alibaba.fastjson.JSON;
import com.caspar.eservicemall.search.constant.EsConstant;
import com.caspar.eservicemall.search.service.MallSearchService;
import com.caspar.eservicemall.search.vo.SearchParam;
import com.caspar.eservicemall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    private ElasticsearchClient client;
    private Aggregation brandAgg;

    /**
     * 准备检索请求
     * #模糊匹配、过滤（按照属性、分类、品牌、价格区间、库存），排序，分页，高亮，聚合分析
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) throws IOException {
        List<Query> queryList = new ArrayList<>();
        /**
         * 模糊匹配、过滤（按照属性、分类、品牌、价格区间、库存）
         */
        //1.构建boolquery
        //1.1 must---模糊匹配
        List<Query> mustQuery = new ArrayList<>();
        if (!StringUtils.isEmpty(param.getKeyword())) {
            Query matchQuery = MatchQuery.of(
                    m -> m.field("skuTitle").query(param.getKeyword())
            )._toQuery();
            mustQuery.add(matchQuery);
        }

        List<Query> filterQuery = new ArrayList<>();
        //1.2 bool -filter -按照三级分类id查询
        if (param.getCatalog3Id() != null) {
            Query catalog3IdTermQuery = TermQuery.of(
                    t -> t.field("catalogId").value(param.getCatalog3Id())
            )._toQuery();
            filterQuery.add(catalog3IdTermQuery);
        }
        //1.2 bool -filter -按照品牌id进行查询
        List<FieldValue> brandIdFields = new ArrayList<>();
        for (Long brandId : param.getBrandId()) {
            brandIdFields.add(FieldValue.of(brandId));
            Query termsQuery = TermsQuery.of(t -> t.field("brandId").terms(terms -> terms.value(brandIdFields)))._toQuery();
            filterQuery.add(termsQuery);
        }
        //1.2 bool -filter -按照所有指定的属性进行查询
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {

            for (String attrStr : param.getAttrs()) {
                //attrs=1_5寸:8寸&attrs=2_8G:16G
                String[] s = attrStr.split("_");
                String attrId = s[0];//检索的属性id
                String[] attrValues = s[1].split(":");//这个属性检索用的值

                List<Query> nestedboolqueryMust = new ArrayList<Query>();

                Query attrIdTermQuery = TermQuery.of(
                        t -> t.field("attrs.attrId").value(attrId)
                )._toQuery();
                List<FieldValue> attrValueField = new ArrayList<>();
                for (String s1 : attrValues) {
                    attrValueField.add(FieldValue.of(s1));
                }
                Query attrValusTermsQuery = TermsQuery.of(
                        t -> t.terms(terms -> terms.value(attrValueField)).field("attrs.attrValue")
                )._toQuery();
                nestedboolqueryMust.add(attrIdTermQuery);
                nestedboolqueryMust.add(attrValusTermsQuery);
                //每一个必须都生成一个nested查询
                Query nestedQuery = NestedQuery.of(
                        n -> n.path("attrs").query(
                                nestquerybuilder -> nestquerybuilder.bool(
                                        nestboolquerybuilder -> nestboolquerybuilder.must(nestedboolqueryMust)
                                )
                        )
                )._toQuery();
                filterQuery.add(nestedQuery);
            }

        }
        //1.2 bool -filter -按照是否有库存进行查询
        if (param.getHasStock() != null) {
            Query hasStockTermQuery = TermQuery.of(
                    t -> t.field("hasStock").value(param.getHasStock() == 1)
            )._toQuery();
            filterQuery.add(hasStockTermQuery);
        }
        //1.2 bool -filter -按照价格区间进行查询
        //1_500/_500/500_
        /**
         * "rang":{
         *     "skuPrice":{
         *         "gte":0,
         *         "lte::6000
         *     }
         * }
         */

        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                //区间
                Query rangeQuery = RangeQuery.of(
                        r -> r.field("skuPrice").gte(JsonData.of(s[0])).lte(JsonData.of(s[1]))
                )._toQuery();
                filterQuery.add(rangeQuery);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    Query rangeQuery = RangeQuery.of(
                            r -> r.field("skuPrice").lte(JsonData.of(s[1]))
                    )._toQuery();
                    filterQuery.add(rangeQuery);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    Query rangeQuery = RangeQuery.of(
                            r -> r.field("skuPrice").gte(JsonData.of(s[0]))
                    )._toQuery();
                    filterQuery.add(rangeQuery);
                }
            }
        }
        //构建bool查询，将所有条件都放进去
        BoolQuery boolQuery = BoolQuery.of(
                b -> b.must(mustQuery).filter(filterQuery)
        )._toQuery().bool();
        //把以前所有的条件都拿来进行封装
        Query query = new Query.Builder().bool(boolQuery)
                .build();
        /**
         * 排序，分页，高亮
         */
//        //2.1 排序
        List<SortOptions> sortOptionsList = new ArrayList<SortOptions>();
        if (!StringUtils.isEmpty(param.getSort())) {

            String sort = param.getSort();
            //sort=saleCount_asc/desc
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.Asc : SortOrder.Desc;
            SortOptions sortOption = SortOptions.of(
                    sortOptionBuilder -> sortOptionBuilder.field(FieldSort.of(
                            f -> f.field(s[0]).order(order)
                    ))
            );
            sortOptionsList.add(sortOption);
        }
        //2.2分页
        //2.3高亮
        Highlight skuTitleHighLight = null;
        if (!StringUtils.isEmpty(param.getKeyword())) {
//            skuTitleHighLight = Highlight.of(
//                    highlightBuilder -> highlightBuilder
//                            .preTags("<b style='color:red'>")
//                            .postTags("</b>")
//                            .fields("skuTitle", highlightFieldBuilder -> highlightFieldBuilder)
//            );
             skuTitleHighLight=Highlight.of(
                    h->h.fields("test", HighlightField.of(
                            field->field.preTags("<span color='red'>").postTags("</span>")
                    ))
            );
        }
        /**
         * 聚合分析
         */
        //1.品牌聚合
        Map<String,Aggregation> aggregationMap=new HashMap<String,Aggregation>();
        Aggregation brandAgg = Aggregation.of(
                aggregationBuilder -> aggregationBuilder.terms(
                        t -> t.size(10).field("brand_id")
                        ////品牌聚合的子聚合
                ).aggregations("brand_name_agg",brandNameAggBuilder->brandNameAggBuilder.terms(
                        brandNameTermsBuilder->brandNameTermsBuilder.field("brandName").size(1)
                )).aggregations("brand_img_agg",brandImgAggBuilder->brandImgAggBuilder.terms(
                        brandImgAggTermsBuilder->brandImgAggTermsBuilder.size(1).field("brandImg")
                ))
        );
        aggregationMap.put("brand_agg",brandAgg);

        //2.分类聚合
//        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
//        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
//        sourceBuilder.aggregation(catalog_agg);
        Aggregation catalog_agg=Aggregation.of(
                catalogAggBuilder->catalogAggBuilder.terms(
                        catalogAggTermsBuilder->catalogAggTermsBuilder.field("catalogId").size(20)
                ).aggregations("catalog_name_agg",catalogNameAggBuilder->catalogNameAggBuilder.terms(
                        catalogNameAggTermsBuilder->catalogNameAggTermsBuilder.field("catalogName").size(1)
                ))   // 子聚合
        );
        aggregationMap.put("catalog_agg",catalog_agg);
        //3.属性聚合
        NestedAggregation attr_agg=NestedAggregation.of(
                nestedAttrAggbuilder->nestedAttrAggbuilder.path("attrs").name("attr_agg")
        );
//        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
//        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
//        //聚合分析出当前attr_id对应的名字
//        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
//        //聚合分析出当前attr_id对应的所有可能的属性值attrValue
//        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
//        attr_agg.subAggregation(attr_id_agg);


        SearchRequest searchRequest = new SearchRequest.Builder()
                .query(query) // 传递条件
                .sort(sortOptionsList) //排序
                .from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGE_SIZE)
                .size(EsConstant.PRODUCT_PAGE_SIZE) //分页
                .highlight(skuTitleHighLight)
                .aggregations(aggregationMap) //聚合分析
                .build();
        return searchRequest;
    }


    @Override
    public SearchResult search(SearchParam param) {
        SearchResult result = null;
//        //1.动态构建出查询需要的dsl语句
//        //1.准备检索请求
       // SearchRequest searchRequest = buildSearchRequest(param);
//        try {
//            //2.执行检索请求
//            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
//            //3.分析响应数据封装成我们需要的格式
//            result =buildSearchResult(response,param);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


       // public final <TDocument> SearchResponse<TDocument> search(Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>> fn, Class<TDocument> tDocumentClass)
      //  client.search()

//        SearchResponse<Poet> response = client.search(searchRequestBuilder -> searchRequestBuilder
//                        .index(INDEX_NAME)
//                        .suggest(suggesterBuilder -> suggesterBuilder
//                                .suggesters("success_suggest", fieldSuggesterBuilder -> fieldSuggesterBuilder
//                                        .text("思考")
//                                        .term(termSuggesterBuilder -> termSuggesterBuilder
//                                                .field("success")
//                                                .suggestMode(SuggestMode.Always)
//                                                .minWordLength(2)
//                                        )
//                                )
//                        )
//                , Poet.class);

//        SearchResponse<Poet> response = client.search(searchRequestBuilder -> searchRequestBuilder
//                        .index(INDEX_NAME)
//                        .query(queryBuilder -> queryBuilder
//                                .matchAll(matchAllQueryBuilder -> matchAllQueryBuilder))
//                , Poet.class);
//        System.out.println(response.toString());
        return result;
    }

}
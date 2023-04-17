package com.caspar.eservicemall.search.service.impl;

import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.NestedAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.util.ObjectBuilder;
import jakarta.json.stream.JsonGenerator;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiQueryTest {
    /**
     * 打印ES8执行语句（序列化）。
     * @param searchRequest
     * @return
     */
    public static String printEsBySearchRequest(SearchRequest searchRequest) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        JsonpMapper mapper=new JacksonJsonpMapper();
        JsonGenerator generator =mapper.jsonProvider().createGenerator(byteArrayOutputStream);
        mapper.serialize(searchRequest, generator);
        generator.close();
        return byteArrayOutputStream.toString();
    }
    public static void main(String[] args) {
        // 构建查询条件
        List<Query> queryList = new ArrayList<>();


        List<Query> mustQuery = new ArrayList<>();

        // matchquery
        Query matchQuery= MatchQuery.of(
                m->m.field("skuTitle").query("iphone")
        )._toQuery();

        mustQuery.add(matchQuery);

        List<Query> filterQuery = new ArrayList<>();
        //1.2  termQuery
        Query catalog3IdTermQuery=TermQuery.of(
                t->t.field("catalogId").value(225)
        )._toQuery();
        filterQuery.add(catalog3IdTermQuery);
        // TermsQuery
        List<FieldValue> brandIds = new ArrayList<>();
        List<Long> brandIdParams=new ArrayList<Long>();
        brandIdParams.add(8L);
        brandIdParams.add(9L);
        for(Long id:brandIdParams){
            brandIds.add(FieldValue.of(id));

        }
        Query termsQuery = TermsQuery.of(t ->t.field("brandId").terms(terms ->terms.value(brandIds)))._toQuery();
        filterQuery.add(termsQuery);
        // nested  attr
        List<String> attrs=new ArrayList<>();
        attrs.add("1_5寸:8寸");
        attrs.add("2_8G:16G");

        List<NestedQuery> nestedQueries=new ArrayList<NestedQuery>();
        for (String attrStr:attrs){
            //attrs=1_5寸:8寸&attrs=2_8G:16G
            String[] s = attrStr.split("_");
            String attrId = s[0];//检索的属性id
            String[] attrValues = s[1].split(":");//这个属性检索用的值
//            nestedboolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
//            nestedboolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
         //   BoolQueryBuilder nestedboolQuery = QueryBuilders.boolQuery();
            //每一个必须都生成一个nested查询
            List<Query> nestedboolqueryMust=new ArrayList<Query>();
            Query attrIdTermQuery=TermQuery.of(
                    t->t.field("attrs.attrId").value(attrId)
            )._toQuery();
            List<FieldValue> attrValueField = new ArrayList<>();
            for(String s1:attrValues){
                attrValueField.add(FieldValue.of(s1));
            }
            Query attrValusTermsQuery=TermsQuery.of(
                    t->t.terms(terms->terms.value(attrValueField)).field("attrs.attrValue")
            )._toQuery();

            nestedboolqueryMust.add(attrIdTermQuery);
            nestedboolqueryMust.add(attrValusTermsQuery);
            Query nestedQuery= NestedQuery.of(
                  n->n.path("attrs").query(
                          nestquerybuilder->nestquerybuilder.bool(
                                  nestboolquerybuilder->nestboolquerybuilder.must(nestedboolqueryMust)
                          )
                  )
            )._toQuery();

            filterQuery.add(nestedQuery);
        }
        BoolQuery boolQuery=BoolQuery.of(
                b->b.must(mustQuery).filter(filterQuery)
        )._toQuery().bool();


        Query query = new Query.Builder().bool(boolQuery)
                .build();


        //2.1 排序
        String sort = "saleCount_asc";
        //sort=saleCount_asc/desc
        String[] s = sort.split("_");
        SortOrder order = s[1].equalsIgnoreCase("asc")?SortOrder.Asc:SortOrder.Desc;
        SortOptions s1 = SortOptions.of(
               sortOption->sortOption.field(FieldSort.of(
                       f->f.field(s[0]).order(order)
               ))
        );
        List<SortOptions> sortOptionsList=new ArrayList<SortOptions>();
        sortOptionsList.add(s1);


        //高亮
        Highlight skuTitleHighLight=Highlight.of(
                h->h.fields("test", HighlightField.of(
                        field->field.preTags("<span color='red'>").postTags("</span>")
                ))
        );
      //聚合
//        SearchResponse<Poet> response = client.search(searchRequestBuilder -> searchRequestBuilder
//                        .index(INDEX_NAME)
//                        .aggregations("age_sum", aggregationBuilder -> aggregationBuilder
//                                .sum(sumAggregationBuilder -> sumAggregationBuilder
//                                        .field("age")))
//                , Poet.class);
        //TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId").size(10);
        Map<String,Aggregation> aggregationMap=new HashMap<String,Aggregation>();
        Aggregation brandAggs = Aggregation.of(
                aggregationBuilder -> aggregationBuilder.terms(
                        t -> t.size(10).field("brand_id")
                ).aggregations("brand_name_agg",brandNameAggBuilder->brandNameAggBuilder.terms(
                        brandNameTermBuilder->brandNameTermBuilder.size(2).field("brand_name")
                )).aggregations("brand_img_agg",brandImgAggBuilder->brandImgAggBuilder.terms(
                        brandImgAggTermBuilder->brandImgAggTermBuilder.size(5).field("brand_img")
                ))
        );
        aggregationMap.put("brand_agg",brandAggs);
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



        SearchRequest searchRequest = new SearchRequest.Builder()
                .query(query) // 传递条件
                .sort(sortOptionsList)
                .highlight(skuTitleHighLight)
                .aggregations(aggregationMap)
                .build();


        String resultDsl=printEsBySearchRequest(searchRequest);
        System.out.println(resultDsl);






    }
}

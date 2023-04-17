package com.caspar.eservicemall.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.util.ObjectBuilder;
import jakarta.json.stream.JsonGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class testEsSearchDSL {
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
//        SearchRequest searchRequest = new SearchRequest.Builder().build();
//        String index_name="eservice_product";

        MatchQuery matchQuery = new MatchQuery.Builder()
                .field("age").query(30)
                .build();
        NestedQuery nestedQuery=new NestedQuery.Builder().query(
                nestquerybuilder->nestquerybuilder.bool(
                        nestboolquerybuilder->nestboolquerybuilder.must(
                                nestquerybuilder2->nestquerybuilder2.term(
                                        termquerybuilder->termquerybuilder.field("attrs.attrId").value("1")
                                )
                        )
                )
        ).path("attrs").build();
        BoolQuery boolQuery=new BoolQuery.Builder().must(matchQuery2->matchQuery2.match(matchQuery)).filter(
                nestquerybuilder->nestquerybuilder.nested(nestedQuery)
        ).build();

     //   RangeQuery rangeQuery=new RangeQuery.Builder().build();

        RangeQuery rangeQuery=new RangeQuery.Builder().field("skuPrice").gt(JsonData.of("20")).lte(JsonData.of("40")).build();

        Query query = new Query.Builder().bool(boolQuery)
                .build();

        SearchRequest searchRequest = new SearchRequest.Builder()
                .query(query) // 传递条件

                .build();

     //   String s= searchRequest.toString();
      //  System.out.println(s);
        String s=printEsBySearchRequest(searchRequest);
        System.out.println(s);


    }
    @Autowired
    private ElasticsearchClient client;

    private SearchRequest buildSearchRequest() throws IOException {
        MatchQuery matchQuery = new MatchQuery.Builder()
                .field("age").query(30)
                .build();


        BoolQuery boolQuery=new BoolQuery.Builder().must(matchQuery2->matchQuery2.match(matchQuery)).build();




        Query query = new Query.Builder().bool(boolQuery)
                .build();




        SearchRequest searchRequest = new SearchRequest.Builder()
                .query(query) // 传递条件
                .build();
        return null;
    }


}

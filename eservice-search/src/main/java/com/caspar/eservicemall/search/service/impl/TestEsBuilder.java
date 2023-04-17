package com.caspar.eservicemall.search.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import jakarta.json.stream.JsonGenerator;

import java.io.ByteArrayOutputStream;

public class TestEsBuilder {
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
        BoolQuery boolQuery = new BoolQuery.Builder().build();
        boolean flag=true;
        if(flag){
            MatchQuery matchQuery = new MatchQuery.Builder()
                    .field("skuTitle").query("22222")
                    .build();
            boolQuery = new BoolQuery.Builder().must(matchQuery2 -> matchQuery2.match(matchQuery)).build();
        }
        boolean flag2=true;
        if (flag2) {

        }
        //Function<Builder, ObjectBuilder<BoolQuery>> fn
        Query byMaxPrice = RangeQuery.of(r -> r
                .field("price")
                .gte(JsonData.of("888"))
        )._toQuery();


        byMaxPrice= RangeQuery.of(r -> r
                .field("price")
                .gte(JsonData.of("999999"))
        )._toQuery();

//        Query query = new Query.Builder().bool(boolQuery)
//                .build();

        SearchRequest searchRequest = new SearchRequest.Builder()
                .query(byMaxPrice) // 传递条件
                .build();
        String s=printEsBySearchRequest(searchRequest);
        System.out.println(s);

    }
}

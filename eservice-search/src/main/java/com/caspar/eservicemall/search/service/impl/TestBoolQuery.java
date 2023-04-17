package com.caspar.eservicemall.search.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.alibaba.nacos.shaded.com.google.common.base.Supplier;
import jakarta.json.stream.JsonGenerator;

import java.io.ByteArrayOutputStream;

public class TestBoolQuery {
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



        Query query = new Query.Builder().bool(boolQuery)
                .build();




        SearchRequest searchRequest = new SearchRequest.Builder()
                .query(query) // 传递条件
                .build();
        String s1=printEsBySearchRequest(searchRequest);
        System.out.println(s1);
    }
}

package com.caspar.eservicemall.search.util;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import jakarta.json.stream.JsonGenerator;

import java.io.ByteArrayOutputStream;

public class ElasticsearchUtil {
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
}

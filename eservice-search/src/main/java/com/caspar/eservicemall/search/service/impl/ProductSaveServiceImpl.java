package com.caspar.eservicemall.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.caspar.eservicemall.common.to.es.SkuEsModel;
import com.caspar.eservicemall.search.constant.EsConstant;
import com.caspar.eservicemall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {
    @Autowired
    private ElasticsearchClient client;
    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        //        //保存到es
        //        //1.给es中建立索引，product 建立好映射关系
        //        //2.给es中保存这些数据

        // 此处创建须用   index请求，不能使用其他请求
        List<BulkOperation> list=skuEsModels.stream().map(model->{
            return new BulkOperation.Builder().index(builder -> builder.index(EsConstant.PRODUCT_INDEX).id(model.getSkuId().toString()).document(model)).build();
        }).collect(Collectors.toList());
        BulkResponse response = client.bulk(builder -> builder.index(EsConstant.PRODUCT_INDEX).operations(list));
        boolean b = response.errors();
        List<String> collect = response.items().stream().map(item->{
            return item.id();
        }).collect(Collectors.toList());
        log.info("商品上架成功：{}",collect);
        return b;
    }
}

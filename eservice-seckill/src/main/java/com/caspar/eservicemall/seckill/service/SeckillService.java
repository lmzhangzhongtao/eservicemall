package com.caspar.eservicemall.seckill.service;

import com.caspar.eservicemall.common.to.seckill.SeckillSkuRedisTO;

import java.util.List;

public interface SeckillService {
    /**
     * 上架最近三天需要秒杀的商品
     */
    void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTO> getCurrentSeckillSkus();

    public SeckillSkuRedisTO getSkuSeckilInfo(Long skuId);

    public String kill(String killId, String key, Integer num) throws InterruptedException ;
}

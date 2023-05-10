package com.caspar.eservicemall.seckill.feign;

import com.caspar.eservicemall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 商品服务
 **/

@FeignClient("eservice-product")
public interface ProductFeignService {


    @RequestMapping("/product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);

    /**
     * 查询商品详情
     */
    @PostMapping("/product/skuinfo/infos")
    R getSkuInfos(@RequestBody List<Long> skuIds);
}

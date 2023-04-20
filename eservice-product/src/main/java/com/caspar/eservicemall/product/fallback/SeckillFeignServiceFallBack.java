package com.caspar.eservicemall.product.fallback;

import com.caspar.eservicemall.common.exception.BizCodeEnum;
import com.caspar.eservicemall.common.utils.R;
import com.caspar.eservicemall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: casparZheng
 * 熔断方法的具体实现，也可以是降级方法的具体实现
 **/
@Slf4j
public class SeckillFeignServiceFallBack implements SeckillFeignService {
    @Override
    public R getSkuSeckilInfo(Long skuId) {
        log.debug("熔断方法调用...getSkuSeckilInfo，获取秒杀商品详情");
        return R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(), BizCodeEnum.TO_MANY_REQUEST.getMsg());
    }

}

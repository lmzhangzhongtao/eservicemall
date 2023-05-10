package com.caspar.eservicemall.seckill.feign;

import com.caspar.eservicemall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("eservice-coupon")
public interface CouponFeignService {
    @GetMapping("/coupon/seckillsession/lates3DaySession")
    R getLates3DaySession();
}

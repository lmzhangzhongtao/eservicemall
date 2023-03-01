package com.caspar.eservicemall.member.feign;

import com.caspar.eservicemall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("eservice-coupon")
public interface CouponFeignService {

    /**
     * 表示远程调用eservice-coupon里面的/coupon/coupon/member/list方法
     * **/
    @RequestMapping("/coupon/coupon/member/list")
    public R memberCoupons();

}

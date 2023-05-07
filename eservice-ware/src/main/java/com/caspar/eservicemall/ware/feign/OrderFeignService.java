package com.caspar.eservicemall.ware.feign;

import com.caspar.eservicemall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("eservice-order")
public interface OrderFeignService {

    /**
     * 获取订单状态
     */
    @GetMapping("/order/order/status/{orderSn}")
    R getOrderByOrderSn(@PathVariable("orderSn") String orderSn);
}

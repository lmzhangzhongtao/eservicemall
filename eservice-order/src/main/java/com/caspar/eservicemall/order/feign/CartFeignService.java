package com.caspar.eservicemall.order.feign;

import com.caspar.eservicemall.common.vo.order.OrderItemVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 购物车系统
 */
@FeignClient("eservice-cart")
public interface CartFeignService {

    /**
     * 查询当前用户购物车选中的商品项
     */
    @GetMapping(value = "/currentUserCartItems")
    List<OrderItemVO> getCurrentCartItems();

}

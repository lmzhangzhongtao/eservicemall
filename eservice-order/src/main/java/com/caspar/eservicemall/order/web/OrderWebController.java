package com.caspar.eservicemall.order.web;

import com.caspar.eservicemall.order.service.OmsOrderService;
import com.caspar.eservicemall.order.vo.OrderConfirmVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    private OmsOrderService orderService;
    /**
     * 跳转结算页
     * 购物车页cart.html点击去结算跳转confirm.html结算页
     */
    @GetMapping(value = "/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        // 查询结算页VO
        OrderConfirmVO confirmVo = null;
        try {
            confirmVo = orderService.getOrderConfirmData();
            model.addAttribute("confirmOrderData", confirmVo);
            // 跳转结算页
            return "confirm";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "toTrade";
    }
}

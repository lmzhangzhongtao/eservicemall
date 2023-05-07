package com.caspar.eservicemall.order.web;

import com.caspar.eservicemall.common.exception.NoStockException;
import com.caspar.eservicemall.common.exception.VerifyPriceException;
import com.caspar.eservicemall.common.vo.order.OrderConfirmVO;
import com.caspar.eservicemall.common.vo.order.OrderSubmitVO;
import com.caspar.eservicemall.common.vo.order.SubmitOrderResponseVO;
import com.caspar.eservicemall.order.annotation.TokenVerify;
import com.caspar.eservicemall.order.service.OmsOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    /**
     * 创建订单
     * 创建成功，跳转订单支付页
     * 创建失败，跳转结算页
     * 无需提交要购买的商品，提交订单时会实时查询最新的购物车商品选中数据提交
     */
    @TokenVerify
    @PostMapping(value = "/submitOrder")
    public String submitOrder(OrderSubmitVO vo, Model model, RedirectAttributes attributes) {
        try {
            SubmitOrderResponseVO orderVO = orderService.submitOrder(vo);
            // 创建订单成功，跳转收银台
            model.addAttribute("submitOrderResp", orderVO);// 封装VO订单数据，供页面解析[订单号、应付金额]
            return "pay";
        } catch (Exception e) {
            // 下单失败回到订单结算页
            if (e instanceof VerifyPriceException) {
                String message = ((VerifyPriceException) e).getMessage();
                attributes.addFlashAttribute("msg", "下单失败" + message);
            } else if (e instanceof NoStockException) {
                String message = ((NoStockException) e).getMessage();
                attributes.addFlashAttribute("msg", "下单失败" + message);
            }
            return "redirect:http://order.eservicemall.com/toTrade";
        }
    }
}

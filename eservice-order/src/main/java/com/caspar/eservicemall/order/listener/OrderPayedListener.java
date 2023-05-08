package com.caspar.eservicemall.order.listener;

import com.alipay.api.AlipayApiException;
import com.caspar.eservicemall.common.constant.order.PaymentConstant;
import com.caspar.eservicemall.common.vo.order.alipay.AliPayAsyncVO;
import com.caspar.eservicemall.order.service.impl.PayContextStrategy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

/**
 * 订单支付成功监听器
 *
 */
@RestController
public class OrderPayedListener {

    @Autowired
    PayContextStrategy payContextStrategy;

    /**
     * 支付宝支付异步通知
     * 只有支付成功会触发
     * @param request
     * @param asyncVo
     */
    @PostMapping(value = "/payed/ali/notify")
    public String handleAliNotify(HttpServletRequest request, AliPayAsyncVO asyncVo) throws AlipayApiException, UnsupportedEncodingException {
        asyncVo.setPayCode(PaymentConstant.PayType.ALI_PAY.getCode());// 封装付款类型
        Boolean result = payContextStrategy.notify(PaymentConstant.PayType.ALI_PAY, request, asyncVo);
        //模拟始终返回true
        result=true;
        if (result) {
            return "success";// 返回success，支付宝将不再异步回调
        }
        return "error";
    }

    //@PostMapping(value = "/pay/notify")
    //public String asyncNotify(@RequestBody String notifyData) {
    //    //异步通知结果
    //    return orderService.asyncNotify(notifyData);
    //}

}

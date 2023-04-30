package com.caspar.eservicemall.cart.controller;

import com.caspar.eservicemall.cart.service.impl.CartServiceImpl;
import com.caspar.eservicemall.common.vo.cart.CartItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
public class CartInfoController {
    @Autowired
    CartServiceImpl cartService;

    @GetMapping(value = "/currentUserCartItems")
    @ResponseBody
    public List<CartItemVO> getCurrentCartItems() {
        return cartService.getUserCartItems();
    }
}

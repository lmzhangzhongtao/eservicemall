package com.caspar.eservicemall.thirdparty.controller;

import com.caspar.eservicemall.common.utils.R;
import com.caspar.eservicemall.thirdparty.service.impl.SmsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
/**
 * 短信服务
 * @Author: caspar
 * @Date: 2023/4/22
 */
@RestController
@RequestMapping("/sms")
public class SmsController {
    @Autowired
    SmsServiceImpl smsService;
    /**
     * 发送短信验证码
     * 提供其他模块调用
     * @param phone 号码
     * @param code  验证码
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code,@RequestParam("templateCode") String templateCode) throws ExecutionException, InterruptedException {
        smsService.sendCode(phone,code,templateCode);
        return R.ok();
    }
}

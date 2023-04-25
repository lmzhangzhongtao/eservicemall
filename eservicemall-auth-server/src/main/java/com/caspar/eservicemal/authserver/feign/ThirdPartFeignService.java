package com.caspar.eservicemal.authserver.feign;

import com.caspar.eservicemall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 第三方服务
 * @Author: wanzenghui
 * @Date: 2021/11/28 10:40
 */
@FeignClient("eservice-third-party")
public interface ThirdPartFeignService {

    @GetMapping("/sms/sendCode")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code,@RequestParam("templateCode") String templateCode);
}
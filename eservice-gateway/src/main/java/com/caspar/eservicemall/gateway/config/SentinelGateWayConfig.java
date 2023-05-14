package com.caspar.eservicemall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.fastjson.JSON;
import com.caspar.eservicemall.common.exception.BizCodeEnum;
import com.caspar.eservicemall.common.utils.R;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Configuration
public class SentinelGateWayConfig {
    public SentinelGateWayConfig(){
        GatewayCallbackManager.setBlockHandler((exchange, t) ->{
            // 网关限流了请求 就会回调这个方法
            R error = R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(), BizCodeEnum.TO_MANY_REQUEST.getMsg());
            String errJson = JSON.toJSONString(error);
            Mono<ServerResponse> body = ServerResponse.ok().body(Mono.just(errJson), String.class);
            return body;
        });
    }
}

package com.caspar.eservicemall.cart.config;

import com.caspar.eservicemall.cart.interceptor.CartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/***
 * 拦截器配置类
 * **/
@Configuration
public class EservicemallWebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CartInterceptor())// 注册拦截器
                .addPathPatterns("/**");
    }
}

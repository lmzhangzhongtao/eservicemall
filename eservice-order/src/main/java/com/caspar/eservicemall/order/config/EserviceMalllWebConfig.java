package com.caspar.eservicemall.order.config;

import com.caspar.eservicemall.order.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class EserviceMalllWebConfig implements WebMvcConfigurer {
    @Autowired
    private LoginUserInterceptor loginUserInterceptor;

    /**
     * 配置拦截器生效
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**");// 访问任何订单请求需要拦截校验登录
    }
}

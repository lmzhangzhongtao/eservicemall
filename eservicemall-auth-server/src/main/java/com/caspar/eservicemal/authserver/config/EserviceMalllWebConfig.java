package com.caspar.eservicemal.authserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class EserviceMalllWebConfig implements WebMvcConfigurer {
    /**
     * 视图映射：请求映射视图
     * 使用GET请求访问
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/reg.html").setViewName("reg");
    }
}

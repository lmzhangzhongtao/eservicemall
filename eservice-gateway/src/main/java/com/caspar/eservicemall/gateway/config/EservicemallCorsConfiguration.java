package com.caspar.eservicemall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/***
 *
 * 网关统一处理跨域请求配置类
 *
 * 在网关处集成了跨域请求之后，我们需要去查看下网关路由的那些服务是否也有跨域请求，因为若是其他服务也有跨域请求处理的话，
 * 那么就会在响应头部分添加两次允许跨域的响应头信息，在renren-fast服务中就有，我们需要将其进行注释掉：
 * **/
@Configuration
public class EservicemallCorsConfiguration {
//GulimallCorsConfiguration
    @Bean
    public CorsWebFilter  corsWebFilter(){
         //基于url跨域，选择reactive包下
        UrlBasedCorsConfigurationSource source =new UrlBasedCorsConfigurationSource();
        //跨域配置信息
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //允许跨域的头
        corsConfiguration.addAllowedHeader("*");
        //允许跨域的请求方式
        corsConfiguration.addAllowedMethod("*");
        //允许跨域的请求来源
//        corsConfiguration.addAllowedOrigin("*");
        //允许所有域名进行跨域调用
        corsConfiguration.addAllowedOriginPattern("*");//替换这个
        //是否允许携带cookie跨域
        corsConfiguration.setAllowCredentials(true);
        //任意url都需要进行跨域请求
        source.registerCorsConfiguration("/**", corsConfiguration);
        System.out.println("网关统一处理跨域请求配置类 配置成功");

        return  new CorsWebFilter(source);
    }



}

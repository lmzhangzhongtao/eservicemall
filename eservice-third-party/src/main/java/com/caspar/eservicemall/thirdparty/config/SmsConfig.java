package com.caspar.eservicemall.thirdparty.config;

import com.caspar.eservicemall.thirdparty.service.impl.SmsServiceImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 短信配置类
 * @Author: caspar
 */
@Configuration
public class SmsConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
    public SmsServiceImpl smsService() {
        return new SmsServiceImpl();
    }
}

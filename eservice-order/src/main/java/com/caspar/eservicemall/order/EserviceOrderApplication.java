package com.caspar.eservicemall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
// 扫描组件
@ComponentScan(basePackages = {"com.caspar.eservicemall.common.utils", "com.caspar.eservicemall.order"})
@SpringBootApplication
@MapperScan("com/caspar/eservicemall/order/dao")
@EnableDiscoveryClient
// 开启rabbit监听（不监听可以不加）
@EnableRabbit
// 开启feign
@EnableFeignClients
// 开启动态代理，使用aspectj作动态代理
@EnableAspectJAutoProxy(exposeProxy = true)
// 开启SpringSession
@EnableRedisHttpSession

public class EserviceOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(EserviceOrderApplication.class, args);
	}

}

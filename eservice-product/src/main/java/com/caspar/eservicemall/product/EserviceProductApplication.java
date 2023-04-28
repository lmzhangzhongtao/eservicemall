package com.caspar.eservicemall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableRedisHttpSession
@MapperScan("com/caspar/eservicemall/product/dao")
@EnableDiscoveryClient
// @EnableCaching // 开启缓存   在配置类MyCacheConfig开启  一样的效果
@EnableFeignClients("com/caspar/eservicemall/product/feign")  //开启远程过程调用扫描
public class EserviceProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(EserviceProductApplication.class, args);
	}

}

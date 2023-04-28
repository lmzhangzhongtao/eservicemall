package com.caspar.eservicemall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients("com/caspar/eservicemall/search/feign")  //开启远程过程调用扫描
@EnableRedisHttpSession //开启spring session管理，session存储在redis中。
public class EserviceSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(EserviceSearchApplication.class, args);
	}

}

package com.caspar.eservicemall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableRedisHttpSession
@EnableFeignClients
@EnableDiscoveryClient
public class EservicemallCartApplication {

	public static void main(String[] args) {
		SpringApplication.run(EservicemallCartApplication.class, args);
	}

}

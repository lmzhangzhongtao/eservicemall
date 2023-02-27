package com.caspar.eservicemall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan("com/caspar/eservicemall/order/dao")
@EnableDiscoveryClient
public class EserviceOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(EserviceOrderApplication.class, args);
	}

}

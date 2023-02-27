package com.caspar.eservicemall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan("com/caspar/eservicemall/product/dao")
@EnableDiscoveryClient
public class EserviceProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(EserviceProductApplication.class, args);
	}

}

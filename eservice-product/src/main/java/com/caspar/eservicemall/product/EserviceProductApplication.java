package com.caspar.eservicemall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com/caspar/eservicemall/product/dao")
@EnableDiscoveryClient
@EnableFeignClients("com/caspar/eservicemall/product/feign")  //开启远程过程调用扫描
public class EserviceProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(EserviceProductApplication.class, args);
	}

}

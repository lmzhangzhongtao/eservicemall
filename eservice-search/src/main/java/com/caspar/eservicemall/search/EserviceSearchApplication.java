package com.caspar.eservicemall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients("com/caspar/eservicemall/search/feign")  //开启远程过程调用扫描
public class EserviceSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(EserviceSearchApplication.class, args);
	}

}

package com.caspar.eservicemall.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com/caspar/eservicemall/ware/dao")
@EnableDiscoveryClient
@EnableFeignClients("com/caspar/eservicemall/ware/feign")  //开启远程过程调用扫描
public class EserviceWareApplication {

	public static void main(String[] args) {
		SpringApplication.run(EserviceWareApplication.class, args);
	}

}

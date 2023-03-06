package com.caspar.eservicemall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

//开启服务发现注册
@EnableDiscoveryClient
//不需要数据源相关配置，故去除掉。 因为common工程引入了相关数据源的依赖
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class EserviceGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(EserviceGatewayApplication.class, args);
	}

}

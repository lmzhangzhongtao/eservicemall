package com.caspar.eservicemall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan("com/caspar/eservicemall/coupon/dao")
@EnableDiscoveryClient
public class EserviceCouponApplication {

	public static void main(String[] args) {
		SpringApplication.run(EserviceCouponApplication.class, args);
	}

}

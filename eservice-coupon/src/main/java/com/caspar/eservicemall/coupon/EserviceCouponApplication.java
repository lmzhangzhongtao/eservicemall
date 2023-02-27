package com.caspar.eservicemall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com/caspar/eservicemall/coupon/dao")
public class EserviceCouponApplication {

	public static void main(String[] args) {
		SpringApplication.run(EserviceCouponApplication.class, args);
	}

}

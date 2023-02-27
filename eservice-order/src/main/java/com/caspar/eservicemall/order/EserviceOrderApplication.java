package com.caspar.eservicemall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com/caspar/eservicemall/order/dao")
public class EserviceOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(EserviceOrderApplication.class, args);
	}

}

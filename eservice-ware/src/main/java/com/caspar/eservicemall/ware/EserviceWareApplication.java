package com.caspar.eservicemall.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com/caspar/eservicemall/ware/dao")
public class EserviceWareApplication {

	public static void main(String[] args) {
		SpringApplication.run(EserviceWareApplication.class, args);
	}

}

package com.caspar.eservicemall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com/caspar/eservicemall/member/dao")
public class EserviceMemberApplication {

	public static void main(String[] args) {
		SpringApplication.run(EserviceMemberApplication.class, args);
	}

}

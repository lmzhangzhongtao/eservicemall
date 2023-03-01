package com.caspar.eservicemall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com/caspar/eservicemall/member/dao") //扫描mybatisplus实现类
@EnableDiscoveryClient  //开启服务注册发现
@EnableFeignClients("com/caspar/eservicemall/member/feign")  //开启远程过程调用扫描
public class EserviceMemberApplication {

	public static void main(String[] args) {
		SpringApplication.run(EserviceMemberApplication.class, args);
	}

}

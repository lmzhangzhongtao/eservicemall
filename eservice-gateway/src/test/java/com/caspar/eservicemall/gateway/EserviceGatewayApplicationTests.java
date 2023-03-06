package com.caspar.eservicemall.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

@SpringBootTest
class EserviceGatewayApplicationTests {
	@Autowired
	private ApplicationContext applicationContext;
	@Test
	void contextLoads() throws Exception {
//		String[] beanNames = applicationContext.getBeanDefinitionNames();
//		Arrays.sort(beanNames);
//		for (String beanName : beanNames) {
//			//全部bean
//			System.out.println(beanName);
//		}
		try {
			applicationContext.getBean("corsWebFilter");
		} catch (Exception e) {
			throw new Exception("corsWebFilter Bean 不存在");
		}
	}

}

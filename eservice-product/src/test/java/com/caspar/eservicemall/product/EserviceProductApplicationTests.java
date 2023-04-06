package com.caspar.eservicemall.product;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;

@SpringBootTest
class EserviceProductApplicationTests {


    @Autowired
	StringRedisTemplate stringRedisTemplate;

	@Autowired
	RedissonClient redissonClient;
	@Test
	void testStringRedisTemplate(){
        // ctrl+alt+v 生成返回类型快捷键
		ValueOperations<String, String> opsValue = stringRedisTemplate.opsForValue();

		opsValue.set("hello","world"+ UUID.randomUUID());
		System.out.println("key:hello存入的值:"+opsValue.get("hello"));
	}



	@Test
	void testRedissonClient(){
		System.out.println(redissonClient);
	}



	@Test
	void contextLoads() {
	}

}

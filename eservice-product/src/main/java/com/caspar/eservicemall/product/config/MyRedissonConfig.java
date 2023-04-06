package com.caspar.eservicemall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
@Configuration
public class MyRedissonConfig {
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() throws IOException {
        // 默认连接地址 127.0.0.1:6379
        // RedissonClient redisson = Redisson.create();
        //1.创建配置
        Config config = new Config();
        // 可以用"rediss://"来启用SSL连接
        config.useSingleServer().setAddress("redis://192.168.218.2:6379");
        //2.根据config创建出redissonClient示例
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}

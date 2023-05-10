package com.caspar.eservicemall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MyRedissonConfig {

    /**
     * 注入客户端实例对象
     */
//    @Bean(destroyMethod="shutdown")
//    public RedissonClient redisson(@Value("${spring.redis.host}") String host, @Value("${spring.redis.port}")String port) throws IOException {
//        // 1.创建配置
//        Config config = new Config();
//        config.useSingleServer().setAddress("redis://" + host + ":" + port);// 单节点模式
////        config.useSingleServer().setAddress("rediss://" + host + ":" + port);// 使用安全连接
////        config.useClusterServers().addNodeAddress("127.0.0.1:7004", "127.0.0.1:7001");// 集群模式
//        // 2.创建redisson客户端实例
//        RedissonClient redissonClient = Redisson.create(config);
//        return redissonClient;
//    }
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
package com.cl.yupao.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
* Redisson配置
* */

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String host;

    private String port;

    private String password;


    @Bean
    public RedissonClient redissonClient(){
        // 1.创建配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        // 2.根据 Config 创建出 RedissonClient 实例
        config.useSingleServer().setAddress(redisAddress).setDatabase(3).setPassword(password);
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}

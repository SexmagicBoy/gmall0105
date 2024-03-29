package com.atguigu.gmall.config;

import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JedisConfig {
    // 读取配置文件中的 redis 的 ip 地址
    @Value("${spring.redis.host:disabled}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;

    @Value("${spring.redis.password:0}")
    private String password;

    @Bean
    public RedisUtil redisUtil() {
        if (host.equals("disabled")) {
            return null;
        }

        RedisUtil redisUtil = new RedisUtil();
        redisUtil.initPool(host, port, database,password);
        return redisUtil;
    }
}

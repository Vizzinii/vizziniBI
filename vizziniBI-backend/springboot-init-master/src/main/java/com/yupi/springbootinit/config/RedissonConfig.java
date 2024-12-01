package com.yupi.springbootinit.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis配置
 *
 * @author <a href="https://github.com/Vizzinii">济楚</a>
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private Integer dataBase;

    private String host;

    private Integer port;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                //.setAddress("redis://localhost:6379")
                .setDatabase(2); // 0是苍穹外卖用的，1给chart对象，2给限流模块
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}

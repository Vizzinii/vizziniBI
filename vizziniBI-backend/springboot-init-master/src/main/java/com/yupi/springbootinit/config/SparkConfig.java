package com.yupi.springbootinit.config;

import io.github.briqt.spark4j.SparkClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * api配置
 *
 * @author <a href="https://github.com/Vizzinii">济楚</a>
 */
@Configuration
@ConfigurationProperties(prefix = "xunfei.client")
@Data
public class SparkConfig {
    private String appid;
    private String apiSecret;
    private String apiKey;

    @Bean
    public SparkClient sparkClient() {
        SparkClient sparkClient = new SparkClient();
        sparkClient.appid = this.appid;
        sparkClient.apiSecret = this.apiSecret;
        sparkClient.apiKey = this.apiKey;
        return sparkClient;
    }
}

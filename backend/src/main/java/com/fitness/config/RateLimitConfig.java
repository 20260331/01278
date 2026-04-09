package com.fitness.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 请求频率限制配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitConfig {
    
    /**
     * 是否启用限流
     */
    private boolean enabled = true;
    
    /**
     * 令牌桶容量
     */
    private int capacity = 100;
    
    /**
     * 每次补充的令牌数
     */
    private int refillTokens = 100;
    
    /**
     * 令牌补充间隔（秒）
     */
    private int refillDuration = 60;
}

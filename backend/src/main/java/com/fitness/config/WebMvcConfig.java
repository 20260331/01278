package com.fitness.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    private final LoginRateLimitInterceptor loginRateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录接口专用限流（更严格）
        registry.addInterceptor(loginRateLimitInterceptor)
                .addPathPatterns("/api/v1/auth/login");
        
        // 通用API限流
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/auth/login",  // 登录接口单独限流
                        "/swagger-ui/**",
                        "/api-docs/**",
                        "/v3/api-docs/**"
                );
    }
}

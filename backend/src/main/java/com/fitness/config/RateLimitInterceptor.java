package com.fitness.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求频率限制拦截器
 * 基于IP地址进行限流
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitConfig rateLimitConfig;
    
    /**
     * IP地址对应的令牌桶缓存
     */
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 如果未启用限流，直接放行
        if (!rateLimitConfig.isEnabled()) {
            return true;
        }

        String clientIp = getClientIp(request);
        Bucket bucket = bucketCache.computeIfAbsent(clientIp, this::createBucket);

        if (bucket.tryConsume(1)) {
            // 添加限流相关响应头
            response.addHeader("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            return true;
        } else {
            log.warn("请求频率超限，IP: {}, URI: {}", clientIp, request.getRequestURI());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
            return false;
        }
    }

    /**
     * 创建令牌桶
     */
    private Bucket createBucket(String key) {
        Bandwidth limit = Bandwidth.classic(
                rateLimitConfig.getCapacity(),
                Refill.greedy(
                        rateLimitConfig.getRefillTokens(),
                        Duration.ofSeconds(rateLimitConfig.getRefillDuration())
                )
        );
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

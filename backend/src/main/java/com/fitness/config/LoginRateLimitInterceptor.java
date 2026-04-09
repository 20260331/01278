package com.fitness.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
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
 * 登录接口专用限流拦截器
 * 防止暴力破解密码
 */
@Slf4j
@Component
public class LoginRateLimitInterceptor implements HandlerInterceptor {

    /**
     * IP地址对应的令牌桶缓存
     * 登录接口限制更严格：每分钟最多10次尝试
     */
    private final Map<String, Bucket> loginBucketCache = new ConcurrentHashMap<>();

    /**
     * 用户名对应的令牌桶缓存
     * 防止针对特定用户的暴力破解
     */
    private final Map<String, Bucket> usernameBucketCache = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 只处理POST请求（登录请求）
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String clientIp = getClientIp(request);
        
        // 检查IP限流
        Bucket ipBucket = loginBucketCache.computeIfAbsent(clientIp, this::createIpBucket);
        if (!ipBucket.tryConsume(1)) {
            log.warn("登录请求频率超限（IP限制），IP: {}", clientIp);
            sendTooManyRequestsResponse(response, "登录尝试过于频繁，请1分钟后再试");
            return false;
        }

        return true;
    }

    /**
     * 创建IP限流令牌桶
     * 每分钟最多10次登录尝试
     */
    private Bucket createIpBucket(String key) {
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * 创建用户名限流令牌桶
     * 每分钟最多5次登录尝试
     */
    private Bucket createUsernameBucket(String key) {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * 发送429响应
     */
    private void sendTooManyRequestsResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":429,\"message\":\"" + message + "\"}");
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
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

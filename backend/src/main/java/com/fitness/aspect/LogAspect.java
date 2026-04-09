package com.fitness.aspect;

import com.fitness.entity.SysLog;
import com.fitness.mapper.SysLogMapper;
import com.fitness.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final SysLogMapper sysLogMapper;

    @Pointcut("execution(* com.fitness.controller..*.*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 执行方法
        Object result = point.proceed();
        
        long duration = System.currentTimeMillis() - startTime;
        
        // 记录日志
        try {
            saveLog(point, duration);
        } catch (Exception e) {
            log.error("保存日志失败", e);
        }
        
        return result;
    }

    private void saveLog(ProceedingJoinPoint point, long duration) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        
        HttpServletRequest request = attributes.getRequest();
        String method = request.getMethod();
        
        // 只记录写操作
        if (!"POST".equals(method) && !"PUT".equals(method) && !"DELETE".equals(method)) {
            return;
        }
        
        SysLog sysLog = new SysLog();
        sysLog.setUserId(SecurityUtil.getCurrentUserId());
        sysLog.setUsername(SecurityUtil.getCurrentUsername());
        sysLog.setOperation(getOperation(point));
        sysLog.setMethod(point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName());
        sysLog.setParams(getParams(point));
        sysLog.setIp(getIpAddress(request));
        sysLog.setDuration(duration);
        
        sysLogMapper.insert(sysLog);
    }

    private String getOperation(ProceedingJoinPoint point) {
        String methodName = point.getSignature().getName();
        String className = point.getTarget().getClass().getSimpleName();
        
        // 简单的操作描述映射
        if (methodName.startsWith("create") || methodName.startsWith("add") || methodName.startsWith("save")) {
            return className.replace("Controller", "") + " - 新增";
        } else if (methodName.startsWith("update") || methodName.startsWith("edit")) {
            return className.replace("Controller", "") + " - 修改";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return className.replace("Controller", "") + " - 删除";
        } else if (methodName.equals("login")) {
            return "用户登录";
        }
        
        return className.replace("Controller", "") + " - " + methodName;
    }

    private String getParams(ProceedingJoinPoint point) {
        Object[] args = point.getArgs();
        if (args == null || args.length == 0) {
            return "";
        }
        try {
            String params = Arrays.toString(args);
            // 限制长度
            if (params.length() > 500) {
                params = params.substring(0, 500) + "...";
            }
            return params;
        } catch (Exception e) {
            return "";
        }
    }

    private String getIpAddress(HttpServletRequest request) {
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
        return ip;
    }
}

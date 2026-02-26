package com.prm.module.log.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.prm.module.log.annotation.OperLog;
import com.prm.module.log.entity.OperationLog;
import com.prm.module.log.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志 AOP 切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint point, OperLog operLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = point.proceed();
            return result;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            saveLog(operLog, duration);
        }
    }

    @Async
    protected void saveLog(OperLog operLog, long durationMs) {
        try {
            OperationLog log = new OperationLog();
            log.setModule(operLog.module());
            log.setAction(operLog.action());
            log.setBizType(operLog.bizType());
            log.setDurationMs(durationMs);
            log.setCreatedAt(LocalDateTime.now());

            try {
                log.setUserId(StpUtil.getLoginIdAsLong());
            } catch (Exception ignored) {
            }

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                log.setIp(getClientIp(request));
                log.setUserAgent(request.getHeader("User-Agent"));
            }

            operationLogMapper.insert(log);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

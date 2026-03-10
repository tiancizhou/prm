package com.prm.module.log.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.prm.common.result.R;
import com.prm.module.log.annotation.OperLog;
import com.prm.module.log.entity.OperationLog;
import com.prm.module.log.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
            saveLog(operLog, duration, result);
        }
    }

    protected void saveLog(OperLog operLog, long durationMs, Object result) {
        try {
            OperationLog log = new OperationLog();
            log.setModule(operLog.module());
            log.setAction(operLog.action());
            log.setBizType(operLog.bizType());
            log.setDurationMs(durationMs);
            log.setCreatedAt(LocalDateTime.now());

            try {
                log.setUserId(StpUtil.getLoginIdAsLong());
                log.setUsername(StpUtil.getLoginIdAsString());
            } catch (Exception ignored) {
            }

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                log.setIp(getClientIp(request));
                log.setUserAgent(request.getHeader("User-Agent"));
                log.setBizId(resolveBizId(request, result));
            }

            log.setAfterData(extractAfterData(result));
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

    private Long resolveBizId(HttpServletRequest request, Object result) {
        Long fromPath = extractBizIdFromPathVariables(request);
        if (fromPath != null) {
            return fromPath;
        }
        return extractBizIdFromResult(result);
    }

    @SuppressWarnings("unchecked")
    private Long extractBizIdFromPathVariables(HttpServletRequest request) {
        Object attribute = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(attribute instanceof Map<?, ?> rawMap)) {
            return null;
        }
        Map<String, String> pathVariables = (Map<String, String>) rawMap;
        for (String key : List.of("id", "projectId", "requirementId", "reqId", "taskId", "bugId", "sprintId", "releaseId")) {
            Long value = parseLong(pathVariables.get(key));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Long extractBizIdFromResult(Object result) {
        Object data = unwrapResponseData(result);
        if (data instanceof Number number) {
            return number.longValue();
        }
        if (data == null) {
            return null;
        }
        try {
            Method method = data.getClass().getMethod("getId");
            Object value = method.invoke(data);
            if (value instanceof Number number) {
                return number.longValue();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Object unwrapResponseData(Object result) {
        if (result instanceof R<?> response) {
            return response.getData();
        }
        return result;
    }

    private String extractAfterData(Object result) {
        Object data = unwrapResponseData(result);
        if (data == null) {
            return null;
        }
        String text = String.valueOf(data);
        return text.length() > 2000 ? text.substring(0, 2000) : text;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}

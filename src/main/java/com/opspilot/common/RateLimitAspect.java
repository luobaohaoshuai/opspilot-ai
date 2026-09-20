package com.opspilot.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    private final StringRedisTemplate redis;

    public RateLimitAspect(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Around("@annotation(rateLimit)")
    public Object check(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return joinPoint.proceed();  // 非 Web 请求直接放行
        }

        HttpServletRequest request = attrs.getRequest();
        HttpServletResponse response = attrs.getResponse();

        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();
        String key = "rate:" + ip + ":" + path;
        int max = rateLimit.maxRequests();

        Long count;
        try {
            count = redis.opsForValue().increment(key);
            if (count == 1) {
                redis.expire(key, 60, TimeUnit.SECONDS);
            }
        } catch (DataAccessException e) {
            log.warn("Redis rate limit is unavailable; allowing request path={}", path, e);
            return joinPoint.proceed();
        }

        if (count != null && count > max) {
            if (response != null) {
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后重试\",\"data\":null}");
            }
            return null;  // 拦截
        }

        return joinPoint.proceed();  // 放行
    }
}

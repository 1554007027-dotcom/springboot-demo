package com.example.demo.aspect;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private static volatile long WINDOW_START = System.currentTimeMillis();
    private static final int LIMIT = 3;
    private static final long WINDOW_MS = 10000;

    @Around("execution(* com.example.demo.controller.AuthController.login(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long now = System.currentTimeMillis();
        if (now - WINDOW_START > WINDOW_MS) {
            WINDOW_START = now;
            COUNTER.set(0);
        }
        int count = COUNTER.incrementAndGet();
        System.out.println("===== 第 " + count + " 次请求 =====");

        if (count > LIMIT) {
            log.warn("限流触发: count={}", count);
            throw new RuntimeException("请求过于频繁，请稍后再试");
        }
        return joinPoint.proceed();
    }
}
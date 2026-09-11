package com.example.demo.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * 定义切点：拦截 com.example.demo.controller 包下所有类的所有方法
     */
    @Pointcut("execution(* com.example.demo.controller..*(..))")
    public void controllerMethods() {
    }

    /**
     * 环绕通知：在方法执行前后都做点事情
     */
    @Around("controllerMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取请求信息
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        String method = request != null ? request.getMethod() : "?";
        String uri = request != null ? request.getRequestURI() : "?";
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String args = Arrays.toString(joinPoint.getArgs());

        // 2. 打印请求
        log.info(">>> [{} {}] {}.{}() 参数: {}",
                method, uri, className, methodName, args);

        long start = System.currentTimeMillis();
        try {
            // 3. 执行目标方法
            Object result = joinPoint.proceed();
            long cost = System.currentTimeMillis() - start;

            // 4. 打印响应
            log.info("<<< [{} {}] 耗时: {}ms 返回: {}",
                    method, uri, cost, result);
            return result;
        } catch (Throwable e) {
            long cost = System.currentTimeMillis() - start;
            // 5. 打印异常
            log.error("!!! [{} {}] 耗时: {}ms 异常: {}",
                    method, uri, cost, e.getMessage());
            throw e;
        }
    }
}
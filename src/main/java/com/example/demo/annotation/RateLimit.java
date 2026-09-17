package com.example.demo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 每秒允许的请求数（默认 10）
     */
    int permitsPerSecond() default 10;

    /**
     * 突发容量（默认和每秒请求数相同）
     */
    int capacity() default -1;
}
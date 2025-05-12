package com.oj.aop;



import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PerformanceMonitorAspect {

    @Around("execution(* com.oj.controller.*.*(..))")
    public Object monitorControllerPerformance(ProceedingJoinPoint point) throws Throwable {
        String methodName = point.getSignature().getName();
        String className = point.getTarget().getClass().getSimpleName();

        long startTime = System.currentTimeMillis();
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        try {
            Object result = point.proceed();

            long endTime = System.currentTimeMillis();
            long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long memoryUsed = endMemory - startMemory;

            log.info("性能监控 - 接口: {}.{}", className, methodName);
            log.info("执行时间: {}ms", (endTime - startTime));
            log.info("内存消耗: {}KB", memoryUsed / 1024);

            return result;
        } catch (Throwable e) {
            log.error("接口异常 - {}.{}: {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}
package com.fts.tenantbasededuportal.aop.aspect;

import com.fts.tenantbasededuportal.aop.annotation.TrackExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class ExecutionTimeAspect {

    @Value("${app.logging.slow-execution-threshold}")
    private long slowExecutionThreshold;

    @Pointcut("@within(com.fts.tenantbasededuportal.aop.annotation.TrackExecutionTime) || " +
            "@annotation(com.fts.tenantbasededuportal.aop.annotation.TrackExecutionTime)")
    public void trackExecutionTimePointcut() {
    }

    @Around("trackExecutionTimePointcut()")
    public Object trackExecutionTime(final ProceedingJoinPoint joinPoint) throws Throwable {

        final long startTime = System.nanoTime();
        try {
            return joinPoint.proceed();
        }
        finally {
            final long endTime = System.nanoTime();
            final long executionTime = (endTime - startTime) / 1_000_000;
            final String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
            final String methodName = joinPoint.getSignature().getName();
            if (executionTime >= slowExecutionThreshold) {
                log.warn("Slow method detected: {}.{}() executed in {} ms", className, methodName, executionTime);
            }
            else {
                log.info("{}.{}() executed in {} ms", className, methodName, executionTime);
            }
        }
    }
}

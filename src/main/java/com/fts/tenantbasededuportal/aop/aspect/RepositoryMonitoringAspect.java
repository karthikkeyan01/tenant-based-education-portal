package com.fts.tenantbasededuportal.aop.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class RepositoryMonitoringAspect {

    @Before("execution(* com.fts.tenantbasededuportal.repository.*.*(..))")
    public void beforeRepositoryMethod(final JoinPoint joinPoint) {

        final String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        final String methodName = joinPoint.getSignature().getName();
        log.debug("Started repository method: {}.{}()", className, methodName);
    }

    @AfterReturning("execution(* com.fts.tenantbasededuportal.repository.*.*(..))")
    public void afterRepositoryMethod(final JoinPoint joinPoint) {

        final String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        final String methodName = joinPoint.getSignature().getName();
        log.debug("Completed repository method: {}.{}()", className, methodName);
    }

    @AfterThrowing(pointcut = "execution(* com.fts.tenantbasededuportal.repository.*.*(..))", throwing = "exception")
    public void afterRepositoryMethodThrows(final JoinPoint joinPoint, final Exception exception) {

        final String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        final String methodName = joinPoint.getSignature().getName();
        log.debug("Repository method failed: {}.{}() - {}", className, methodName, exception.getMessage());
    }
}

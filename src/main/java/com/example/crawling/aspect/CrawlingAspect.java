package com.example.crawling.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class CrawlingAspect { // 크롤링 시간 측정

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        log.info("=== 크롤링 시작 ===");

        long start = System.nanoTime();
        Object result = null;

        try {
            result = joinPoint.proceed();

            return result;
        } catch (Exception e) {
            log.error("크롤링 중 오류 발생: {}", e.getMessage(), e);

            throw e;
        } finally {
            long elapsed = System.nanoTime() - start;
            double seconds = elapsed / 1_000_000_000.0;
            String formatted = seconds >= 1
                    ? String.format("%.2f초", seconds)
                    : String.format("%dms", elapsed / 1_000_000);

            log.info("=== 크롤링 완료 - 실행 시간 : {} ===", formatted);
        }
    }
}


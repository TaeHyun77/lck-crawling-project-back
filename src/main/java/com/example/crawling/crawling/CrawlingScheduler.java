package com.example.crawling.crawling;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Around;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

@RequiredArgsConstructor
@Slf4j
@Component
public class CrawlingScheduler {

    private final CrawlingService crawlingService;

    @Scheduled(fixedDelay = 60000) // 1분마다 크롤링 진행
    public void scheduleCrawling() {

        try {
            // 모든 크롤링이 완료될 때까지 대기
            crawlingService.asyncCrawling().join();

        } catch (CustomException e) {
            log.info("Lck Data 크롤링 작업을 실패하였습니다.");

            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_CRAWLING_SCHEDULING);
        }
    }
}


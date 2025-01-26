package com.example.crawling.config;

import com.example.crawling.crawling.Crawling;
import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CrawlingScheduler {

    private final Crawling crawling;

    @Scheduled(fixedDelay = 60000)
    public void scheduleCrawling() {

        try {
            System.out.println("Lck Data 크롤링 작업이 시작되었습니다.");
            crawling.process();
            System.out.println("Lck Data 크롤링 작업이 완료되었습니다.");
        } catch (CustomException e) {
            System.out.println("Lck Data 크롤링 작업을 실패하였습니다.");
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_CRAWLING_DATA);
        }
    }
}

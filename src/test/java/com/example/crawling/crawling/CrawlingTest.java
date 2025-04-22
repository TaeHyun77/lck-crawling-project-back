/*
package com.example.crawling.crawling;

import com.example.crawling.crawling.Crawling;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;

@Slf4j
@SpringBootTest
class CrawlingTest {

    @Autowired
    private Crawling crawling;

    @Test
    public void testSynchronousCrawling() {
        log.info("=== 동기 크롤링 테스트 시작 ===");
        Instant start = Instant.now();

        //crawling.process(); // 동기 실행

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        log.info("동기 크롤링 수행 시간: " + duration.toMillis() + "ms");

        assertTrue(duration.toMillis() > 0);
    }

    @BeforeEach
    public void resetCrawlingState() {
        crawling.resetCrawlingState(); // isCrawling 초기화하는 메서드 호출
    }

    @Test
    public void testAsynchronousCrawling() {
        Instant start = Instant.now();

        log.info("=== 비동기 크롤링 테스트 시작 ===");

        crawling.startCrawlingAsync().join(); // 비동기 실행 후 완료될 때까지 대기

        log.info("=== 비동기 크롤링 테스트 끝 ===");

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        log.info("비동기 크롤링 수행 시간: " + duration.toMillis() + "ms");

        assertTrue(duration.toMillis() > 0);
    }
}
*/

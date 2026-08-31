package com.example.crawling;

import com.example.crawling.crawling.CrawlingScheduler;
import com.example.crawling.crawling.CrawlingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class CrawlingTimeMeasure {

    // @Scheduled가 테스트 중 스레드 풀을 선점하지 못하도록 모킹
    @MockBean
    private CrawlingScheduler crawlingScheduler;

    @Autowired
    private CrawlingService crawlingService;

    private static final int REPEAT = 5;

    @Test
    @DisplayName("비동기 크롤링 vs 동기 크롤링 속도 비교")
    public void crawlingTimeDiffAsyncAndSync() throws Exception {

        // WebDriver 첫 초기화 비용이 측정에 영향을 주지 않도록 워밍업 실행
        log.info("=== 워밍업 시작 (측정 제외) ===");
        crawlingService.asyncCrawling().join();
        log.info("=== 워밍업 완료 ===");

        long asyncTotal = 0;
        long syncTotal = 0;

        log.info("=== 비동기 크롤링 측정 시작 ({} 회) ===", REPEAT);
        for (int i = 0; i < REPEAT; i++) {
            long start = System.nanoTime();
            crawlingService.asyncCrawling().join();
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            asyncTotal += elapsed;
            log.info("[Async {}회] {}ms", i + 1, elapsed);
        }

        log.info("=== 동기 크롤링 측정 시작 ({} 회) ===", REPEAT);
        for (int i = 0; i < REPEAT; i++) {
            long start = System.nanoTime();
            crawlingService.syncCrawling();
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            syncTotal += elapsed;
            log.info("[Sync  {}회] {}ms", i + 1, elapsed);
        }

        long asyncAvg = asyncTotal / REPEAT;
        long syncAvg  = syncTotal / REPEAT;
        long diff     = syncAvg - asyncAvg;
        double ratio  = (double) syncAvg / asyncAvg;

        log.info("====================================================");
        log.info("비동기 평균 소요 시간 : {}ms", asyncAvg);
        log.info("동기    평균 소요 시간 : {}ms", syncAvg);
        log.info("속도 차이             : {}ms (동기가 {}배 더 느림)", diff, String.format("%.2f", ratio));
        log.info("====================================================");
    }
}

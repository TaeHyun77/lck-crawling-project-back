package com.example.crawling;

import com.example.crawling.crawling.CrawlingScheduler;
import com.example.crawling.crawling.CrawlingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * API 호출 → 서비스 로직 → DB 저장까지 전체 흐름 시간 측정.
 */
@Slf4j
@SpringBootTest
public class ApiTimeMeasure {

    @MockBean
    private CrawlingScheduler crawlingScheduler;

    @Autowired
    private CrawlingService crawlingService;

    private static final int REPEAT = 5;

    @Test
    @DisplayName("API 기반 비동기 데이터 갱신 5회 평균 시간 측정 (순위 + 일정 병렬, DB 포함)")
    void measureAsyncApiCallWithDb() throws Exception {
        // 워밍업 (JIT + DB 커넥션 풀 초기화 비용 제외)
        log.info("=== 워밍업 시작 (측정 제외) ===");
        crawlingService.asyncCrawling().join();
        log.info("=== 워밍업 완료 ===\n");

        long asyncTotal = 0;
        long syncTotal = 0;

        log.info("=== 비동기 측정 시작 ({} 회) ===", REPEAT);
        for (int i = 1; i <= REPEAT; i++) {
            long start = System.nanoTime();
            crawlingService.asyncCrawling().join();
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            asyncTotal += elapsed;
            log.info("[Async {}회] {}ms", i, elapsed);
        }

        log.info("=== 동기 측정 시작 ({} 회) ===", REPEAT);
        for (int i = 1; i <= REPEAT; i++) {
            long start = System.nanoTime();
            crawlingService.syncCrawling();
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            syncTotal += elapsed;
            log.info("[Sync  {}회] {}ms", i, elapsed);
        }

        long asyncAvg = asyncTotal / REPEAT;
        long syncAvg = syncTotal / REPEAT;

        log.info("====================================================");
        log.info("비동기 평균 소요 시간 : {}ms", asyncAvg);
        log.info("동기    평균 소요 시간 : {}ms", syncAvg);
        log.info("속도 차이             : {}ms (동기가 {배 더 느림)", syncAvg - asyncAvg,
                String.format("%.2f", (double) syncAvg / asyncAvg));
        log.info("====================================================");
    }
}

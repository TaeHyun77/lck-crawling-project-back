package com.example.crawling;

import com.example.crawling.crawling.CrawlingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableAsync
@SpringBootTest
public class CrawlingTimeMeasure {

    @Autowired
    private CrawlingService crawlingService;

    @Test
    @DisplayName("비동기적 크롤링과 동기적 크롤링 시간 차이 체크 - 10회 반복")
    public void crawlingTimeDiffAsyncAndSync() throws Exception {

        int repeat = 10;

        long asyncTotal = 0;
        long syncTotal = 0;

        // 비동기 테스트
        for (int i = 0; i < repeat; i++) {
            long start = System.nanoTime();
            crawlingService.asyncCrawling().join();
            long elapsed = System.nanoTime() - start;

            asyncTotal += elapsed;
            System.out.println("[Async " + (i + 1) + "회] " + (elapsed / 1_000_000) + "ms");
        }

        // 동기 테스트
        for (int i = 0; i < repeat; i++) {
            long start = System.nanoTime();
            crawlingService.syncCrawling();
            long elapsed = System.nanoTime() - start;

            syncTotal += elapsed;
            System.out.println("[Sync " + (i + 1) + "회] " + (elapsed / 1_000_000) + "ms");
        }

        System.out.println("==================================");
        System.out.println("비동기 평균 소요 시간: " + (asyncTotal / repeat / 1_000_000) + "ms");
        System.out.println("동기    평균 소요 시간: " + (syncTotal / repeat / 1_000_000) + "ms");
        System.out.println("==================================");
    }
}

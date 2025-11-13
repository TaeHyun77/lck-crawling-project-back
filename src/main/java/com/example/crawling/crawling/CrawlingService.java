package com.example.crawling.crawling;

import com.example.crawling.config.WebDriverFactory;
import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.ranking.RankingService;
import com.example.crawling.schedule.MatchScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@EnableAsync
@Configuration
public class CrawlingService {

    private final MatchScheduleService matchScheduleService;
    private final RankingService rankingService;
    private final WebDriver driver; // 동기적 크롤링 용도

    @Async
    public CompletableFuture<Void> asyncCrawling() {

        return CompletableFuture.runAsync(() -> {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            futures.add(CompletableFuture.runAsync(() -> {
                WebDriver localDriver = WebDriverFactory.createWebDriver();

                try {
                    matchScheduleService.crawlingSchedules(localDriver);
                } catch (Exception e) {
                    log.error("getDataList 크롤링 실패: ");

                    throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_CRAWLING_LCK_DATA);
                } finally {
                    localDriver.quit();
                }
            }));

            futures.add(CompletableFuture.runAsync(() -> {
                WebDriver localDriver = WebDriverFactory.createWebDriver();

                try {
                    rankingService.crawlingRanking(localDriver);
                } catch (Exception e) {
                    log.error("getRanking 크롤링 실패: ");

                    throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_CRAWLING_RANKING_DATA);
                } finally {
                    localDriver.quit();
                }
            }));

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        });
    }

    // 동기적으로 실행한 크롤링
    public void syncCrawling() {
        try {
            matchScheduleService.crawlingSchedules(driver);
            rankingService.crawlingRanking(driver);
        } catch (CustomException e) {
            log.info("동기적 크롤링 실패");

            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_CRAWLING);
        }
    }
}
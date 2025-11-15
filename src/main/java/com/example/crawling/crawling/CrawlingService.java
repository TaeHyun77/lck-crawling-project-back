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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor

@Service
public class CrawlingService {

    private final MatchScheduleService matchScheduleService;
    private final RankingService rankingService;


    public CompletableFuture<Void> crawlSchedulesAsync() {
        return CompletableFuture.runAsync(() -> {
            log.info("일정 실행 스레드 = {}", Thread.currentThread().getName());
            WebDriver localDriver = WebDriverFactory.createWebDriver();

            try {
                matchScheduleService.crawlingSchedules(localDriver);
                log.info("LCK 일정 크롤링 완료");
            } catch (Exception e) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_CRAWLING_LCK_DATA);
            } finally {
                localDriver.quit();
            }
        });
    }

    public CompletableFuture<Void> crawlRankingAsync() {
        return CompletableFuture.runAsync(() -> {
            log.info("순위 실행 스레드 = {}", Thread.currentThread().getName());
            WebDriver localDriver = WebDriverFactory.createWebDriver();

            try {
                rankingService.crawlingRanking(localDriver);
                log.info("LCK 순위 크롤링 완료");
            } catch (Exception e) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_CRAWLING_RANKING_DATA);
            } finally {
                localDriver.quit();
            }
        });
    }

    // 비동기적으로 실행한 크롤링
    public CompletableFuture<Void> asyncCrawling() {
        return CompletableFuture.allOf(
                crawlSchedulesAsync(),
                crawlRankingAsync()
        ).handle((res, ex) -> {
            if(ex != null) {
                log.error("크롤링 실패", ex);
            }
            return null;
        });
    }

    // 동기적으로 실행한 크롤링
    public void syncCrawling() {
        WebDriver localDriver = WebDriverFactory.createWebDriver();

        try {
            matchScheduleService.crawlingSchedules(localDriver);
            rankingService.crawlingRanking(localDriver);
        } finally {
            localDriver.quit();
        }
    }
}
package com.example.crawling.crawling;

import com.example.crawling.config.WebDriverFactory;
import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.parameters.P;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@EnableAsync
@Configuration
public class Crawling {

    private final CrawlingService crawlingService;
    private final WebDriver driver; // 동기적 크롤링 용도

    @Async
    public CompletableFuture<Void> startCrawlingAsync() {

        return CompletableFuture.runAsync(() -> {
            try {
                List<CompletableFuture<Void>> futures = new ArrayList<>();

                futures.add(CompletableFuture.runAsync(() -> {
                    WebDriver localDriver = WebDriverFactory.createWebDriver();

                    try {
                        crawlingService.getDataList(localDriver);
                    } catch (Exception e) {
                        log.error("getDataList 크롤링 실패: ", e);
                    } finally {
                        localDriver.quit();
                    }
                }));

                futures.add(CompletableFuture.runAsync(() -> {
                    WebDriver localDriver = WebDriverFactory.createWebDriver();

                    try {
                        crawlingService.getRanking(localDriver);
                    } catch (Exception e) {
                        log.error("getRanking 크롤링 실패: ", e);
                    } finally {
                        localDriver.quit();
                    }
                }));

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            } catch (CustomException e) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_LOAD_DRIVER);
            }
        });
    }

    // 동기적으로 실행한 크롤링
    public void process() {
        try {
            crawlingService.getDataList(driver);
            crawlingService.getRanking(driver);
        } catch (CustomException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_LOAD_DRIVER);
        }
    }
}
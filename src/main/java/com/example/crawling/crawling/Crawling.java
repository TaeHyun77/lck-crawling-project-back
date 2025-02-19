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

    //private final WebDriver driver;

    private final CrawlingService crawlingService;
    private final Object crawlingLock = new Object(); // 스레드 동기화를 위한 lock 객체
    private boolean isCrawling = false;

    @Async
    public CompletableFuture<Void> startCrawlingAsync() {

        // startCrawlingAsync()가 동시에 여러 번 실행되지 않도록 보장하는 것
        // crawlingLock 쓰지 않고 매개변수에 this로 변경해도 됨
        synchronized (crawlingLock) {
            if (isCrawling) {
                log.info("이미 크롤링이 진행 중입니다. 요청을 스킵합니다.");
                return CompletableFuture.completedFuture(null);
            }
            isCrawling = true;
        }

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

            } finally {
                isCrawling = false;
            }
        });
    }

    // 테스트 코드에서 isCrawling을 초기화 하기 위함
    public void resetCrawlingState() {
        synchronized (crawlingLock) {
            isCrawling = false;
            log.info("isCrawling 상태 초기화 완료!");
        }
    }


//    // 동기적으로 실행한 크롤링
//    public void process() {
//        try {
//            crawlingService.getDataList(driver);
//            crawlingService.getRanking(driver);
//        } catch (CustomException e) {
//            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_LOAD_DRIVER);
//        }
//    }
}
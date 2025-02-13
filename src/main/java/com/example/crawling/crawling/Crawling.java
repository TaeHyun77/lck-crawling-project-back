package com.example.crawling.crawling;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class Crawling {

    private final WebDriver driver;
    private final CrawlingService crawlingService;

    public void process() {
        try {
            crawlingService.getDataList(driver);
            crawlingService.getRanking(driver);
        } catch (CustomException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_LOAD_DRIVER);
        } finally {
            if (driver != null) { // driver가 null인지 확인 후 종료
                try {
                    driver.quit();
                    log.info("WebDriver 종료");
                } catch (Exception ex) {
                    log.error("WebDriver 종료 중 오류 발생", ex);
                }
            }
        }
    }
}
package com.example.crawling.crawling;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Configuration
public class Crawling {

    private final WebDriver driver;
    private final CrawlingService crawlingService;

    public void process() {
        try {
            crawlingService.getDataList(driver);
            crawlingService.getRankingData(driver);
        } catch (CustomException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_LOAD_DRIVER);
        }
    }
}
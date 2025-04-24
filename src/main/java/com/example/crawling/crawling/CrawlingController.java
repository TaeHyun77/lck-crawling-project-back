package com.example.crawling.crawling;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CrawlingController {

    private final Crawling crawling;

    private final CrawlingService crawlingService;
    private final WebDriver driver;

    // 비동기적 크롤링
    @GetMapping("/crawling/nonBlock")
    public void crawlNonBlock() {
        crawlingService.getDataList(driver);
    }

    // 동기적 크롤링
    @GetMapping("/crawling/block")
    public void crawlBlock() {
        crawling.process();
    }
}


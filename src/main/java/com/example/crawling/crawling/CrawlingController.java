package com.example.crawling.crawling;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/crawling")
@RestController
public class CrawlingController {

    private final CrawlingService crawlingService;

    @GetMapping("/async")
    public void doAsyncCrawling() {
        crawlingService.asyncCrawling();
    }

    @GetMapping("/sync")
    public void doSyncCrawling() {
        crawlingService.syncCrawling();
    }
}

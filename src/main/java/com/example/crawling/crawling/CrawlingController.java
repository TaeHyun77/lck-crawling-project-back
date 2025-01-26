package com.example.crawling.crawling;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CrawlingController {

    private final Crawling crawling;
    @PostMapping("/crawling")
    public void crawl() {
        crawling.process();
    }
}

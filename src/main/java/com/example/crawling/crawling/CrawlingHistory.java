package com.example.crawling.crawling;

import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Set;

@Component
public class CrawlingHistory {
    private final Set<String> crawledMonths = new HashSet<>();

    // 특정 월이 저장되어 있는지 bool 형으로 return
    public boolean isCrawled(String month) {
        return crawledMonths.contains(month);
    }

    public void markAsCrawled(String month) {
        crawledMonths.add(month);
    }
}
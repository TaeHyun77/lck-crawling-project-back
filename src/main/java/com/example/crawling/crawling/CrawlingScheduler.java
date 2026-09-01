package com.example.crawling.crawling;

import com.example.crawling.global.exception.CustomException;
import com.example.crawling.global.exception.ErrorCode;
import com.example.crawling.schedule.domain.MatchScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletionException;

@RequiredArgsConstructor
@Slf4j
@Component
public class CrawlingScheduler {

    private final CrawlingService crawlingService;
    private final MatchScheduleRepository matchScheduleRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // 매 시 정각 — 일정 + 순위 전체 동기화
    @Scheduled(cron = "0 0 * * * *")
    public void regularCrawl() {
        try {
            crawlingService.asyncCrawling().join();
        } catch (CompletionException e) {
            log.error("LCK 전체 크롤링 작업을 실패하였습니다: {}", e.getCause().getMessage());
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_CRAWLING_SCHEDULING);
        }
    }

    // 3분마다 — 진행 중인 경기가 있을 때만 일정 빠른 갱신
    @Scheduled(fixedDelay = 3 * 60 * 1000)
    public void activeCrawl() {
        LocalDate today = LocalDate.now(KST);
        String currentTime = LocalTime.now(KST).format(TIME_FORMATTER);

        if (!matchScheduleRepository.hasMatchInProgress(today, currentTime)) {
            return;
        }

        log.info("진행 중인 경기가 감지되었습니다. 빠른 크롤링을 시작합니다.");
        try {
            crawlingService.crawlSchedulesAsync().join();
        } catch (CompletionException e) {
            // 활성 크롤링 실패는 3분 뒤 자동 재시도되므로 예외를 전파하지 않음
            log.error("활성 크롤링 실패: {}", e.getCause().getMessage());
        }
    }
}

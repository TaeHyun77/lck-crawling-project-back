package com.example.crawling.notification.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmScheduler {

    private final FcmService fcmService;

    @Scheduled(cron = "0 10 * * * *") // 매 시간 10분에 실행 → 경기 시작 3시간 전부터 1시간마다 알림
    public void sendPushNotification3Before() {
        log.info("경기 시작 3시간 전 푸시 알림 전송 시작");
        String result = fcmService.pushMatchSchedule(3);
        log.info("경기 시작 3시간 전 푸시 알림 완료 : {}", result);
    }

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행 → 오늘 있는 경기 목록 푸시 알림
    public void sendPushNotification24Before() {
        log.info("당일 경기 푸시 알림 전송 시작");
        String result = fcmService.pushMatchSchedule(24);
        log.info("당일 경기 푸시 알림 완료 : {}", result);
    }
}

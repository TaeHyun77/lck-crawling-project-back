package com.example.crawling.fcm;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class FcmScheduler {

    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(cron = "0 10 * * * *") // 매 시간 10분에 실행 -> 경기 시작 3시간 전부터 1시간 마다 알림
    public void sendPushNotification3Before() {

        log.info("알림 전송");

        String url = "http://localhost:8080/push/notification?param=48";

        try {
            String response = restTemplate.postForObject(url, null, String.class);
            log.info("경기 시작 3시간 전 푸시 알림 성공 : " + response);
        } catch (CustomException e) {
            log.info("경기 시작 3시간 전 정각 푸시 알림 실패 : " + e.getMessage());
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAILED_TO_SEND_NOTIFICATION_3_HOURS_BEFORE);
        }
    }

    @Scheduled(cron = "0 0 0 * * *")  // 매일 00:00 실행 -> 오늘 있는 경기 목록 푸시 알람
    public void sendPushNotification24Before() {

        String url = "http://localhost:8080/push/notification?param=48";

        try {
            String response = restTemplate.postForObject(url, null, String.class);
            log.info("정각 푸시 알림 성공 : " + response);
        } catch (CustomException e) {
            log.info("정각 푸시 알림 실패 : " + e.getMessage());
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAILED_TO_SEND_NOTIFICATION_24_HOURS_BEFORE);
        }
    }
}

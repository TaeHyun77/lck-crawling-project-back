package com.example.crawling.fcm;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FcmScheduled {

    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(cron = "0 10 * * * *") // 매 시간 10분에 실행
    public void sendPushNotificationRequest() {

        String url = "http://localhost:8080/push/notification";

        try {
            String response = restTemplate.postForObject(url, null, String.class);
            System.out.println("정각 푸시 알림 성공 : " + response);
        } catch (Exception e) {
            System.err.println("정각 푸시 알림 실패 : " + e.getMessage());
        }
    }
}

package com.example.crawling.notification.infra;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

// Firebase SDK를 초기화해서 푸시 알림 발송이나 Firebase 관련 기능을 사용할 수 있도록 한다.
@Slf4j
@Configuration
public class FirebaseInitialization {

    @PostConstruct
    public void initialize() {
        try (InputStream serviceAccount =
                     new ClassPathResource("firebase/lck-crawling-project.json").getInputStream()) {

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // 중복 초기화 방지
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("FCM 초기화가 완료되었습니다.");
            }

        } catch (IOException e) {
            log.info("FCM 초기화에 실패하였습니다.");
            throw new RuntimeException(e);
        }
    }
}

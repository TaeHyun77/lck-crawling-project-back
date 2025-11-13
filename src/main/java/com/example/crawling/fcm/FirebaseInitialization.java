package com.example.crawling.fcm;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

// Firebase SDK를 초기화해서, 푸시 알림 발송이나 Firebase 관련 기능을 사용할 수 있도록 하는 코드
@Slf4j
@Configuration
public class FirebaseInitialization {

    // fcm 초기화 코드
    @PostConstruct
    public void initialize() {
        try (InputStream serviceAccount =
                     new ClassPathResource("firebase/lck-crawling-project.json").getInputStream()) {

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // 이미 초기화된 FirebaseApp이 있는지 확인 (중복 초기화 방지)
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

// FileInputStream을 사용하면 Spring Boot가 JAR 파일로 패키징 된 후에는 해당 파일을 찾을 수 없음
// ./src/main/resources/는 개발 환경에서만 존재하는 경로이고, JAR 배포 시 리소스 폴더는 classpath에 포함됨
// 따라서 getClass().getClassLoader().getResourceAsStream()을 사용하여 classpath에서 로드해야 함
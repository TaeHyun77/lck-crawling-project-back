package com.example.crawling.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FirebaseInitialization {

    // fcm 초기화 코드
    @PostConstruct
    public void initialize() {
        try {
            //FileInputStream serviceAccount = new FileInputStream("./src/main/resources/firebase/lck-crawling-project.json");

            InputStream serviceAccount = new ClassPathResource("firebase/lck-crawling-project.json").getInputStream();


            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// FileInputStream을 사용하면 Spring Boot가 JAR 파일로 패키징된 후에는 해당 파일을 찾을 수 없음
// ./src/main/resources/는 개발 환경에서만 존재하는 경로이고, JAR 배포 시 리소스 폴더는 classpath에 포함됨
// 따라서 getClass().getClassLoader().getResourceAsStream()을 사용하여 classpath에서 로드해야 함
package com.example.crawling.config;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class WebDriverConfig {

    @Bean
    public WebDriver webDriver() {

        try {

            // 크롬 버전에 맞는 driver 자동 설치
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*"); // CORS 우회
            options.addArguments("--disable-popup-blocking"); // 팝업 차단 해제
            options.addArguments("--start-maximized"); // 창 최대화
            options.addArguments("--disable-gpu"); // GPU 가속 비활성화
            options.addArguments("--disable-cache", "--disable-application-cache", "--disk-cache-size=0"); // 캐시 비활성화

            // user-agent를 사용하여 탐지를 피함
            // user-agent : 웹 브라우저나 클라이언트가 서버와 통신할 때 자신의 정보를 포함하여 보내는 HTTP 요청 헤더 값
            // headless 하면 크롤링을 방지하는 사이트에서는 Headless Chrome을 감지하여 차단하므로 크롤링을 할 때 일반적인 브라우저의 User-Agent를 설정하면 탐지를 피할 수 있음
            options.addArguments("--headless");
            options.addArguments("--window-size=1920x1080");
            options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.20 Safari/537.36");

            return new ChromeDriver(options);

        } catch (CustomException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_SETTING_DRIVER);
        }
    }
}
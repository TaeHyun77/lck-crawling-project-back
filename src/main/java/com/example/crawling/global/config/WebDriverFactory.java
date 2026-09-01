package com.example.crawling.global.config;

import com.example.crawling.global.exception.CustomException;
import com.example.crawling.global.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.http.HttpStatus;

public class WebDriverFactory {

    public static WebDriver createWebDriver() {
        try {
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*"); // CORS 우회
            options.addArguments("--disable-popup-blocking"); // 팝업 차단 해제
            options.addArguments("--start-maximized"); // 창 최대화
            options.addArguments("--disable-gpu"); // GPU 가속 비활성화
            options.addArguments("--disable-cache", "--disable-application-cache", "--disk-cache-size=0"); // 캐시 비활성화
            options.addArguments("--no-sandbox");
            // headless 모드에서 user-agent로 봇 탐지 우회
            options.addArguments("--headless");
            options.addArguments("--window-size=1920x1080");
            options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.20 Safari/537.36");

            return new ChromeDriver(options);

        } catch (CustomException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_SETTING_DRIVER);
        }
    }
}

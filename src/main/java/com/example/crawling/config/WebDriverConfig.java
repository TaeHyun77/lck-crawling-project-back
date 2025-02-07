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

import java.util.Currency;

@Configuration
public class WebDriverConfig {

    @Bean
    public WebDriver webDriver() {

        try {

            // 크롬 버전에 맞는 driver 자동 설치
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-popup-blocking");
            options.addArguments("--start-maximized");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-cache", "--disable-application-cache", "--disk-cache-size=0");

            // headless 하면 크롤링 하는 것을 사이트에서 알기에 ip를 차단당함 -> user-agent 사용
//            options.addArguments("--headless");
//            options.addArguments("--window-size=1920x1080");
//            options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.20 Safari/537.36");

            return new ChromeDriver(options);

        } catch (CustomException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_SETTING_DRIVER);
        }
    }
}
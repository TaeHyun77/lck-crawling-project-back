package com.example.crawling.crawling;

import com.example.crawling.global.config.WebDriverFactory;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * CSS 클래스 변경 여부를 확인하기 위한 진단 테스트.
 * @SpringBootTest 없이 WebDriver만 직접 사용한다.
 */
public class SchedulePageDiagnosticTest {

    private static final String SCHEDULE_URL = "https://game.naver.com/esports/League_of_Legends/schedule/lck";
    private static final String RANKING_URL  = "https://game.naver.com/esports/League_of_Legends/record/lck/team/lck_2026";

    @Test
    public void dumpSchedulePageHtml() {
        WebDriver driver = WebDriverFactory.createWebDriver();
        try {
            driver.get(SCHEDULE_URL);
            Thread.sleep(8000); // JS 렌더링 대기
            String src = driver.getPageSource();
            System.out.println("=== 전체 길이: " + src.length() + " ===");
            // 경기 카드 콘텐츠 찾기
            System.out.println("=== 25000~30000 구간 ===");
            System.out.println(src.substring(25000, Math.min(30000, src.length())));
        } catch (Exception e) {
            System.out.println("오류: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    @Test
    public void diagnoseSchedulePage() {
        WebDriver driver = WebDriverFactory.createWebDriver();
        try {
            driver.get(SCHEDULE_URL);

            // JS 렌더링 대기 (최대 25초)
            new WebDriverWait(driver, Duration.ofSeconds(25))
                    .until(d -> !d.findElements(By.cssSelector("body *[class]")).isEmpty());

            System.out.println("=== 일정 페이지 DOM 로드 완료 ===");

            // "schedule" 키워드가 포함된 클래스 찾기
            List<WebElement> scheduleEls = driver.findElements(By.cssSelector("[class*='schedule']"));
            System.out.println("\n--- 'schedule' 클래스 포함 엘리먼트 ---");
            scheduleEls.stream().limit(15).forEach(el ->
                System.out.println("  <" + el.getTagName() + "> " + el.getAttribute("class")));

            // "calendar" 키워드
            List<WebElement> calendarEls = driver.findElements(By.cssSelector("[class*='calendar']"));
            System.out.println("\n--- 'calendar' 클래스 포함 엘리먼트 ---");
            calendarEls.stream().limit(10).forEach(el ->
                System.out.println("  <" + el.getTagName() + "> " + el.getAttribute("class")));

            // "card" 키워드
            List<WebElement> cardEls = driver.findElements(By.cssSelector("[class*='card']"));
            System.out.println("\n--- 'card' 클래스 포함 엘리먼트 ---");
            cardEls.stream().limit(10).forEach(el ->
                System.out.println("  <" + el.getTagName() + "> " + el.getAttribute("class")));

            // "row" 키워드
            List<WebElement> rowEls = driver.findElements(By.cssSelector("[class*='row_']"));
            System.out.println("\n--- 'row_' 클래스 포함 엘리먼트 ---");
            rowEls.stream().limit(10).forEach(el ->
                System.out.println("  <" + el.getTagName() + "> " + el.getAttribute("class")));

        } catch (Exception e) {
            System.out.println("진단 중 오류: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    @Test
    public void dumpRankingPageHtml() {
        WebDriver driver = WebDriverFactory.createWebDriver();
        try {
            driver.get(RANKING_URL);
            Thread.sleep(8000);
            String src = driver.getPageSource();
            System.out.println("=== 순위 페이지 전체 길이: " + src.length() + " ===");
            // 팀 순위 데이터가 있을 중간 구간
            // 10000~20000 구간 (그룹 구조 파악)
            System.out.println("=== 순위 페이지 16000~22000 ===");
            System.out.println(src.substring(16000, Math.min(22000, src.length())));
        } catch (Exception e) {
            System.out.println("오류: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    @Test
    public void diagnoseRankingPage() {
        WebDriver driver = WebDriverFactory.createWebDriver();
        try {
            driver.get(RANKING_URL);
            Thread.sleep(5000);
            System.out.println("=== 순위 페이지 접속 ===");
            List<WebElement> rankEls = driver.findElements(By.cssSelector("[data-rank], [data-team], [class*='_rank_'], [class*='_team_']"));
            System.out.println("data-rank/team 엘리먼트: " + rankEls.size() + "개");
            rankEls.stream().limit(10).forEach(el ->
                System.out.println("  <" + el.getTagName() + " class='" + el.getAttribute("class") + "'>"));
        } catch (Exception e) {
            System.out.println("오류: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    private void checkSelector(WebDriver driver, String selector, String description, int waitSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(waitSeconds))
                    .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
            List<WebElement> elements = driver.findElements(By.cssSelector(selector));
            System.out.printf("[OK] %s — %d개 발견 (%s)%n", description, elements.size(), selector);
        } catch (Exception e) {
            System.out.printf("[FAIL] %s — 셀렉터 없음 (%s)%n", description, selector);
        }
    }
}

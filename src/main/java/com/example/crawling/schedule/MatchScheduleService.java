package com.example.crawling.schedule;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.schedule.dto.MatchScheduleResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class MatchScheduleService {
    private final MatchScheduleUpdateService matchScheduleUpdateService;
    private final MatchScheduleRepository matchScheduleRepository;
    private static final String SCHEDULE_URL = "https://game.naver.com/esports/League_of_Legends/schedule/lck";

    // LCK 일정 정보 크롤링
    public void crawlingSchedules(WebDriver driver) {
        driver.get(SCHEDULE_URL);

        // 경기가 있는 달 정보를 담을 리스트
        Map<Integer, String> monthInfos = extractActiveMonth(driver);

        // 현재 달 구하기
        LocalDate today = LocalDate.now();
        int currentMonth = 9; // today.getMonthValue(); 현재는 LCK 안해서 9월로 임의 지정

        for (int month: monthInfos.keySet()) {
            if (month != currentMonth) {
                continue;
            }

            try {
                driver.get(monthInfos.get(month));

                log.info("{}월의 일정 정보를 크롤링합니다.", month);
                scrapeMonthlySchedule(driver, month);
                log.info("{}월의 일정 정보를 크롤링이 완료되었습니다.", month);
            } catch (Exception e) {
                log.error("LCK 일정 크롤링 실패: {}", e.getMessage(), e);
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_CRAWLING_LCK_DATA);
            }
        }

    }

    private void scrapeMonthlySchedule(WebDriver driver, int currentMonth) {

        log.info("{}월 일정 파싱 중", currentMonth);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-time-stamp]")));

        List<WebElement> matchesOnMonth = driver.findElements(By.cssSelector("[data-time-stamp]"));

        // 연도 말에 다음 연도 일정이 표시될 경우를 위해 KST 기준으로 현재 연도를 사용
        int year = LocalDate.now(ZoneId.of("Asia/Seoul")).getYear();
        DateTimeFormatter monthDayFormatter = DateTimeFormatter.ofPattern("MM월 dd일");
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MM월 dd일 (E)", Locale.KOREAN);

        for (WebElement webElement : matchesOnMonth) {

            WebElement dateInfo = webElement.findElement(By.cssSelector("[class*='_date_']"));
            // "오늘09월 15일 (화)" 또는 "09월 15일 (화)" → "09월 15일"
            String dateCleaned = dateInfo.getText()
                    .replace("오늘", "")
                    .replaceAll("\\s*\\(.*?\\)", "")
                    .trim();
            LocalDate matchDate = MonthDay.parse(dateCleaned, monthDayFormatter).atYear(year);

            List<WebElement> matches = webElement.findElements(By.cssSelector("li[data-tournament]"));

            for (WebElement match : matches) {

                String startTime = match.findElement(By.cssSelector("[class*='_time_']")).getText();

                String matchStatus = match.findElement(By.cssSelector("[data-type]")).getText();

                String stageType = match.findElement(By.cssSelector("[class*='_title_']")).getText();

                String team1 = match.findElement(By.cssSelector("[class*='_home_'] [class*='_name_']")).getText();
                String team2 = match.findElement(By.cssSelector("[class*='_away_'] [class*='_name_']")).getText();

                String teamScore1 = "none";
                String teamScore2 = "none";

                if (!matchStatus.equals("예정")) {
                    List<WebElement> scoreElements = match.findElements(By.cssSelector("[data-lose-score]"));
                    if (scoreElements.size() >= 2) {
                        teamScore1 = scoreElements.get(0).getText();
                        teamScore2 = scoreElements.get(1).getText();
                    }
                }

                String teamImg1 = null;
                String teamImg2 = null;
                try {
                    teamImg1 = match.findElement(By.cssSelector("[class*='_home_'] img")).getAttribute("src");
                } catch (NoSuchElementException ignored) {}
                try {
                    teamImg2 = match.findElement(By.cssSelector("[class*='_away_'] img")).getAttribute("src");
                } catch (NoSuchElementException ignored) {}

                MatchSchedule scheduleData = new MatchSchedule(
                        currentMonth,
                        matchDate,
                        startTime,
                        team1,
                        team2,
                        matchStatus,
                        stageType,
                        teamScore1,
                        teamScore2,
                        teamImg1,
                        teamImg2
                );

                matchScheduleUpdateService.updateMatchScheduleIfChanged(scheduleData);
            }
        }
    }

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("MM월 dd일 (E)", Locale.KOREAN);

    public List<MatchScheduleResponseDto> getAllSchedule() {
        return matchScheduleRepository.findAll().stream()
                .map(schedule -> MatchScheduleResponseDto.builder()
                        .month(schedule.getMonth())
                        .matchDate(schedule.getMatchDate().format(DISPLAY_FORMATTER))
                        .startTime(schedule.getStartTime())
                        .team1(schedule.getTeam1())
                        .team2(schedule.getTeam2())
                        .matchStatus(schedule.getMatchStatus())
                        .stageType(schedule.getStageType())
                        .teamScore1(schedule.getTeamScore1())
                        .teamScore2(schedule.getTeamScore2())
                        .teamImg1(schedule.getTeamImg1())
                        .teamImg2(schedule.getTeamImg2())
                        .build())
                .collect(Collectors.toList());
    }

    // 경기가 존재하는 달을 구하는 로직
    private Map<Integer, String> extractActiveMonth(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        // 비활성 달은 <div>, 활성 달은 <a> 태그로 렌더링됨
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("a[href*='schedule/lck?date=']"))
        );

        List<WebElement> activeMonthElements = driver.findElements(
                By.cssSelector("a[href*='schedule/lck?date=']")
        );

        Map<Integer, String> monthLinkInfos = new HashMap<>();

        for (WebElement monthElement : activeMonthElements) {
            try {
                String href = monthElement.getAttribute("href");
                String monthText = monthElement.getText().trim();

                Integer month = Integer.parseInt(monthText.replace("월", "").trim());

                monthLinkInfos.put(month, href);
            } catch (Exception e) {
                log.error("월 정보 파싱 중 오류 발생: {}", e.getMessage(), e);
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.ERROR_TO_PARSING_MONTH);
            }
        }

        return monthLinkInfos;
    }
}


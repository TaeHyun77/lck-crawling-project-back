package com.example.crawling.schedule;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class MatchScheduleService {

    private final MatchScheduleRepository matchScheduleRepository;

    // LCK 일정 정보 크롤링
    public void crawlingSchedules(WebDriver driver) {

        // LCK 일정 정보 페이지
        driver.get("https://game.naver.com/esports/League_of_Legends/schedule/lck");

        // 경기가 있는 달 정보를 담을 리스트
        Map<Integer, String> monthInfos = extractActiveMonth(driver);

        // 현재 달 구하기
        LocalDate today = LocalDate.now();
        int currentMonth = 9; // today.getMonthValue(); 현재는 LCK 안해서 9월로 임의 지정

        for (int month: monthInfos.keySet()) {
            if (month != currentMonth) {
                // log.info("{}월 - 이번 달이 아니므로 크롤링을 건너뜁니다.", month);
                continue;
            }

            try {
                driver.get(monthInfos.get(month));

                log.info("{}월의 일정 정보를 크롤링합니다.", month);
                scrapeMonthlySchedule(driver, month);
                log.info("{}월의 일정 정보를 크롤링이 완료되었습니다.", month);
            } catch (Exception e) {
                log.info("LCK 일정 크롤링 실패");
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_CRAWLING_LCK_DATA);
            }
        }

    }

    private void scrapeMonthlySchedule(WebDriver driver, int currentMonth)  {

        log.info(String.valueOf(currentMonth));

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));

        // 사이트 전체 스캔
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".schedule_container__2rbMY")));

        // 현재 달 특정 날의 경기 목록 일정
        List<WebElement> matchesOnMonth = driver.findElements(By.cssSelector(".card_item__3Covz"));

        for (WebElement webElement : matchesOnMonth) {

            // 경기 날짜
            WebElement dateInfo = webElement.findElement(By.cssSelector(".card_date__1kdC3"));
            String date = dateInfo.getText().replace("오늘", "");

            // 특정 날의 각각 경기 목록
            List<WebElement> matches = webElement.findElements(By.cssSelector(".row_item__dbJjy"));

            // 특정 날의 각각 경기 목록 루프
            for (WebElement match : matches) {

                // 시작 시간
                WebElement timeElements = match.findElement(By.cssSelector(".row_time__28bwr"));
                String startTime = timeElements.getText();

                // 경기 결과 상태
                WebElement matchStatusElements = match.findElement(By.cssSelector(".row_state__2RKDU"));
                String matchStatus = matchStatusElements.getText();

                // 스테이지 정보
                WebElement stageTypeElements = match.findElement(By.cssSelector(".row_title__1sdwN"));
                String stageType = stageTypeElements.getText();

                // 팀 이름
                WebElement teamElement = match.findElement(By.cssSelector(".row_box_score__1WQuz"));
                List<WebElement> teamNameElements = teamElement.findElements(By.cssSelector(".row_name__IDFHz"));
                String team1 = teamNameElements.get(0).getText();
                String team2 = teamNameElements.get(1).getText();

                // 점수
                String teamScore1 = "none";
                String teamScore2 = "none";

                if (!matchStatus.equals("예정")) {
                    WebElement scoreElement = match.findElement(By.cssSelector(".row_box_score__1WQuz"));
                    List<WebElement> numberElements = scoreElement.findElements(By.cssSelector(".row_score__2RmGQ"));
                    teamScore1 = numberElements.get(0).getText();
                    teamScore2 = numberElements.get(1).getText();
                }

                // 팀 이미지
                List<WebElement> imageElements = match.findElements(By.cssSelector(".row_box_score__1WQuz img"));
                String teamImg1 = null;
                String teamImg2 = null;

                if (imageElements.size() >= 2) {
                    teamImg1 = imageElements.get(0).getAttribute("src");
                    teamImg2 = imageElements.get(1).getAttribute("src");
                } else {
                    teamImg1 = imageElements.get(0).getAttribute("src");
                }

                MatchSchedule scheduleData = new MatchSchedule(
                        currentMonth,
                        date,
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

                checkUpdate(scheduleData);
            }
        }
    }

    // 일정 정보 갱신
    @Transactional
    public void checkUpdate(MatchSchedule crawlingMatchSchedule) {

        MatchSchedule dbMatchSchedule = matchScheduleRepository.findByMatchDateAndStartTime(
                crawlingMatchSchedule.getMatchDate(), crawlingMatchSchedule.getStartTime()
        ).orElseGet(() -> matchScheduleRepository.save(crawlingMatchSchedule));

        // 갱신 되었다면 변경
        if (isMatchScheduleChanged(dbMatchSchedule, crawlingMatchSchedule)) {
            dbMatchSchedule.updateMatchSchedule(
                    crawlingMatchSchedule.getMonth(), crawlingMatchSchedule.getTeam1(), crawlingMatchSchedule.getTeam2(),
                    crawlingMatchSchedule.getMatchStatus(), crawlingMatchSchedule.getStageType(),
                    crawlingMatchSchedule.getTeamScore1(), crawlingMatchSchedule.getTeamScore2(),
                    crawlingMatchSchedule.getTeamImg1(), crawlingMatchSchedule.getTeamImg2()
            );

            log.info("일정 정보가 갱신되었습니다.");
            matchScheduleRepository.save(dbMatchSchedule);
        }
    }

    // 일정 정보가 수정되었는지 여부
    private boolean isMatchScheduleChanged(MatchSchedule existing, MatchSchedule newSchedule) {
        return  existing.getMonth() != newSchedule.getMonth() ||
                !existing.getStartTime().equals(newSchedule.getStartTime()) ||
                !existing.getMatchStatus().equals(newSchedule.getMatchStatus()) ||
                !existing.getStageType().equals(newSchedule.getStageType()) ||
                !existing.getTeamScore1().equals(newSchedule.getTeamScore1()) ||
                !existing.getTeamScore2().equals(newSchedule.getTeamScore2()) ||
                !Objects.equals(existing.getTeamImg1(), newSchedule.getTeamImg1()) ||
                !Objects.equals(existing.getTeamImg2(), newSchedule.getTeamImg2());
    }

    public List<MatchScheduleResponseDto> getAllSchedule() {
        return matchScheduleRepository.findAll().stream()
                .map(schedule -> MatchScheduleResponseDto.builder()
                        .month(schedule.getMonth())
                        .matchDate(schedule.getMatchDate())
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
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".schedule_calendar_list_month__1VIHT"))
        );

        // 경기가 있는 달 정보를 크롤링
        List<WebElement> activeMonthElements = driver.findElements(
                By.cssSelector("a.schedule_calendar_month__2mWJA:not([data-disabled='true'])")
        );

        // 경기가 있는 달 정보를 담을 리스트
        Map<Integer, String> monthLinkInfos = new HashMap<>();

        // 경기가 있는 달 정보를 리스트에 담는 로직
        for (WebElement monthElement : activeMonthElements) {
            try {
                String href = monthElement.getAttribute("href");
                String monthText = monthElement.findElement(By.cssSelector("span")).getText();

                Integer month = Integer.parseInt(monthText.replace("월", "").trim());

                monthLinkInfos.put(month, href);
            } catch (Exception e) {
                log.warn("월 정보 파싱 중 오류 발생 - 건너뜀", e);

                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.ERROR_TO_PARSING_MONTH);
            }
        }

        return monthLinkInfos;
    }
}


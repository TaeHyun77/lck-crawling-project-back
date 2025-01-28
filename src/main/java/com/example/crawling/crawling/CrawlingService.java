package com.example.crawling.crawling;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.schedule.MatchScheduleDto;
import com.example.crawling.schedule.MatchScheduleRepository;
import com.example.crawling.rank.RankDto;
import com.example.crawling.rank.RankRepository;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CrawlingService {

    private final MatchScheduleRepository matchScheduleRepository;
    private final RankRepository rankRepository;
    private final CrawlingHistory crawlingHistory;

    public void getDataList(WebDriver driver) {

        LocalDate currentDate = LocalDate.now();
        String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        int month = Integer.parseInt(currentDate.format(DateTimeFormatter.ofPattern("MM")));

        log.info("현재 크롤링 월 : " + formattedDate);
        deleteMatchScheduleByMonth(month);

        // 현재 월 데이터 크롤링
        driver.get("https://game.naver.com/esports/League_of_Legends/schedule/lck");
        crawlScheduleData(driver, month);

        // 다음 월 데이터 크롤링 (한 번만 실행)
        List<WebElement> unselectedMonths = driver.findElements(By.cssSelector("a[data-selected='false']"));

        for (WebElement monthElement : unselectedMonths) {
            String nextMonthText = monthElement.findElement(By.cssSelector("span")).getText();
            log.info("다음 월 데이터: " + nextMonthText); // "2월"과 같은 형식

            // 다른 월의 일정 url
            String nextMonthHref = monthElement.getAttribute("href");

            String nextMonth = nextMonthHref.split("date=")[1]; // YYYY-MM 형식

            if (!crawlingHistory.isCrawled(nextMonth)) {
                log.info(nextMonth + " 크롤링 시작!");
                driver.manage().deleteAllCookies();
                driver.get(nextMonthHref);
                driver.navigate().refresh();
                crawlScheduleData(driver, Integer.parseInt(nextMonth.split("-")[1]));
                crawlingHistory.markAsCrawled(nextMonth);
            } else {
                log.info(nextMonth + " 이미 크롤링 완료된 데이터입니다.");
            }
        }
    }

    private void crawlScheduleData(WebDriver driver, int month)  {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".schedule_container__2rbMY")));

        // 특정 날짜의 경기 목록 일정
        List<WebElement> elements = driver.findElements(By.cssSelector(".card_item__3Covz"));

        for (int i = 0; i < elements.size(); i++) {

            // 경기 날짜
            WebElement dateInfo = elements.get(i).findElement(By.cssSelector(".card_date__1kdC3"));
            String date = dateInfo.getText();

            // 각각의 경기 목록
            List<WebElement> scheduleElements = elements.get(i).findElements(By.cssSelector(".row_item__dbJjy"));

            for (int j = 0; j < scheduleElements.size(); j++) {

                // 시작 시간
                WebElement timeElements = scheduleElements.get(j).findElement(By.cssSelector(".row_time__28bwr"));
                String startTime = timeElements.getText();

                // 경기 결과 상태
                WebElement matchStatusElements = scheduleElements.get(j).findElement(By.cssSelector(".row_state__2RKDU"));
                String matchStatus = matchStatusElements.getText();

                WebElement stageTypeElements = scheduleElements.get(j).findElement(By.cssSelector(".row_title__1sdwN"));
                String stageType = stageTypeElements.getText();

                // 팀 이름
                WebElement teamElement = scheduleElements.get(j).findElement(By.cssSelector(".row_box_score__1WQuz"));
                List<WebElement> teamNameElements = teamElement.findElements(By.cssSelector(".row_name__IDFHz"));
                String team1 = teamNameElements.get(0).getText();
                String team2 = teamNameElements.get(1).getText();

                // 점수
                String teamScore1 = "none";
                String teamScore2 = "none";

                if (!matchStatus.equals("예정")) {
                    WebElement scoreElement = scheduleElements.get(j).findElement(By.cssSelector(".row_box_score__1WQuz"));
                    List<WebElement> numberElements = scoreElement.findElements(By.cssSelector(".row_score__2RmGQ"));
                    teamScore1 = numberElements.get(0).getText();
                    teamScore2 = numberElements.get(1).getText();
                }

                // 이미지
                List<WebElement> imageElements = scheduleElements.get(j).findElements(By.cssSelector(".row_box_score__1WQuz img"));
                String teamImg1 = null;
                String teamImg2 = null;

                if (imageElements != null && imageElements.size() >= 2) {
                    teamImg1 = imageElements.get(0).getAttribute("src");
                    teamImg2 = imageElements.get(1).getAttribute("src");
                }

                ScheduleData scheduleData = new ScheduleData(
                        date,
                        startTime,
                        matchStatus,
                        stageType,
                        team1,
                        team2,
                        teamImg1,
                        teamImg2
                );

                log.info("scheduleData : " + scheduleData);

                try {
                    MatchScheduleDto scheduleDto = MatchScheduleDto.builder()
                            .month(month)
                            .matchDate(scheduleData.date())
                            .startTime(scheduleData.startTime())
                            .team1(scheduleData.team1())
                            .team2(scheduleData.team2())
                            .matchStatus(scheduleData.matchStatus())
                            .stageType(stageType)
                            .teamScore1(teamScore1)
                            .teamScore2(teamScore2)
                            .teamImg1(scheduleData.teamImg1())
                            .teamImg2(scheduleData.teamImg2())
                            .build();

                    matchScheduleRepository.save(scheduleDto.toEntity());
                    log.info("경기 일정 DB 저장 완료 !");
                } catch (CustomException e) {
                    log.info("경기 일정 DB 저장 실패 !");
                    throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_STORE_SCHEDULE_DATA);
                }
            }
        }
    }

    public void getRankingData(WebDriver driver) {

        deleteRanking();

        driver.get("https://game.naver.com/esports/League_of_Legends/record/lck/team/lck_2024_summer");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".record_list_wrap__A8cnT")));
        List<WebElement> dataList = driver.findElements(By.cssSelector(".record_list_item__2fFsp"));

        for (int i = 0; i < dataList.size()/2; i++) {

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".record_list_wrap__A8cnT")));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".record_list_thumb_logo__1s1BT")));

            // 팀 이름
            WebElement teamNameElement = dataList.get(i).findElement(By.cssSelector(".record_list_name__27huQ"));
            String teamName = teamNameElement.getText();

            // 팀 이미지
            WebElement teamImgElement = dataList.get(i).findElement(By.cssSelector(".record_list_thumb_logo__1s1BT"));
            String backgroundImage = teamImgElement.getCssValue("background-image");
            log.info("Background Image : " + backgroundImage);

            String imageUrl = backgroundImage.split("\\?")[0].substring(4).replaceAll("\"", "");
            log.info("imageUrl : " + imageUrl);

            List<WebElement> teamElements = dataList.get(i+10).findElements(By.cssSelector(".record_list_data__3wyY7"));

            RankingData rankingData = new RankingData(
                    teamName,
                    teamElements.get(0).getText(), // winCnt
                    teamElements.get(1).getText(), // loseCnt
                    teamElements.get(2).getText(), // pointDiff
                    teamElements.get(3).getText(), // winRate
                    teamElements.get(4).getText(), // kda
                    teamElements.get(5).getText(), // killCnt
                    teamElements.get(6).getText(), // deathCnt
                    teamElements.get(7).getText(),  // assistCnt
                    imageUrl
            );

            log.info("rankingData : " + rankingData);

            try {
                RankDto rankData = RankDto.builder()
                        .teamName(rankingData.teamName())
                        .teamImgUrl(rankingData.imageUrl())
                        .winCnt(rankingData.winCnt())
                        .loseCnt(rankingData.loseCnt())
                        .pointDiff(rankingData.pointDiff())
                        .winRate(rankingData.winRate())
                        .kda(rankingData.kda())
                        .killCnt(rankingData.killCnt())
                        .deathCnt(rankingData.deathCnt())
                        .assistCnt(rankingData.assistCnt())
                        .build();

                rankRepository.save(rankData.toRankEntity());
                log.info("순위 DB 저장 성공 !");
            } catch (CustomException e) {
                log.info("순위 일정 DB 저장 실패 !");
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_STORE_RANKING_DATA);
            }
        }
    }

    @Transactional
    public void deleteMatchScheduleByMonth(int month) {
        try {
            matchScheduleRepository.deleteByMonth(month);
            matchScheduleRepository.deleteByMonth(2);
            log.info(month + "월 일정 데이터 삭제 성공 !");

            matchScheduleRepository.resetAutoIncrement(); // id 값 1부터 시작 하도록
        } catch (Exception e) {
            log.info(month + "월 일정 데이터 삭제 실패 !");
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_DELETE_SCHEDULE_DATA);
        }
    }

    @Transactional
    public void deleteRanking() {
        try {
            rankRepository.deleteAll();
            log.info("순위 데이터 삭제 성공 !");

            rankRepository.resetAutoIncrement();
        } catch (CustomException e) {
            log.info("순위 데이터 삭제 실패 !");
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_DELETE_RANKING_DATA);
        }
    }
}
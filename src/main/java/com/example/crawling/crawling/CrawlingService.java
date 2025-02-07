package com.example.crawling.crawling;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.schedule.MatchSchedule;
import com.example.crawling.schedule.MatchScheduleDto;
import com.example.crawling.schedule.MatchScheduleRepository;
import com.example.crawling.rank.RankDto;
import com.example.crawling.rank.RankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CrawlingService {

    private final MatchScheduleRepository matchScheduleRepository;
    private final RankRepository rankRepository;

    // ********** 2월 기준 **********
    public void getDataList(WebDriver driver) {

        LocalDate currentDate = LocalDate.now();
        int month = currentDate.getMonthValue();
        log.info("현재 크롤링 월 : " + month);

        // 현재 월 데이터 크롤링 ( 2월꺼 )
        driver.get("https://game.naver.com/esports/League_of_Legends/schedule/lck");
        crawlScheduleData(driver, month);

        // 다음 월 데이터 크롤링 ( 1월꺼 , 한 번만 실행 )
        WebElement unselectedMonths = driver.findElement(By.cssSelector("a[data-selected='false']"));

        // 다른 월의 일정 url ( 1월꺼 )
        String nextMonthHref = unselectedMonths.getAttribute("href");

        String nextMonthText = unselectedMonths.findElement(By.cssSelector("span")).getText();
        log.info("다음 월 데이터: " + nextMonthText); // "1월"과 같은 형식

        int other_month = Integer.parseInt(nextMonthText.replace("월", ""));

        if (!matchScheduleRepository.existsByMonth(other_month)) {
            log.info(nextMonthText + " 크롤링 시작!");

            driver.manage().deleteAllCookies();
            driver.get(nextMonthHref);

            // 강제 새로 고침
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            jsExecutor.executeScript("location.reload();");

            crawlScheduleData(driver, other_month);
        } else {
            log.info(nextMonthText + " 이미 크롤링 완료된 데이터입니다.");
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
                } else if(imageElements != null && imageElements.size() >= 1) {
                    teamImg1 = imageElements.get(0).getAttribute("src");
                    teamImg2 = null;
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

                    saveOrUpdateMatchSchedule(scheduleDto.toEntity());
                } catch (CustomException e) {
                    log.info("경기 일정 DB 저장 실패 !");
                    throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_STORE_SCHEDULE_DATA);
                }
            }
        }
    }

    @Transactional
    public void saveOrUpdateMatchSchedule(MatchSchedule matchSchedule) {

        Optional<MatchSchedule> existingSchedule;

        try {
            existingSchedule = matchScheduleRepository.findByMatchDateAndTeam1AndTeam2(
                    matchSchedule.getMatchDate(), matchSchedule.getTeam1(), matchSchedule.getTeam2());
        } catch (CustomException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUNT_MATCHSCHEDULE);
        }

        if (existingSchedule.isPresent()) {
            MatchSchedule schedule = existingSchedule.get();

            if (isMatchScheduleChanged(schedule, matchSchedule)) {
                schedule.updateMatchSchedule(
                        matchSchedule.getMonth(), matchSchedule.getMatchDate(), matchSchedule.getStartTime(),
                        matchSchedule.getTeam1(), matchSchedule.getTeam2(), matchSchedule.getMatchStatus(),
                        matchSchedule.getStageType(), matchSchedule.getTeamScore1(), matchSchedule.getTeamScore2(),
                        matchSchedule.getTeamImg1(), matchSchedule.getTeamImg2()
                );

                matchScheduleRepository.save(schedule);
                log.info(matchSchedule.getMatchDate() + " " + matchSchedule.getTeam1() + "-" + matchSchedule.getTeam2() + "의 경기 정보가 업데이트 되었습니다.");
            } else {
                log.info(matchSchedule.getMatchDate() + " " + matchSchedule.getTeam1() + "-" + matchSchedule.getTeam2() + "의 경기 정보는 변경 되지 않았습니다.");
            }
        } else {
            matchScheduleRepository.save(matchSchedule);
            log.info("새로운 경기 일정 저장");
        }
    }

    private boolean isMatchScheduleChanged(MatchSchedule existing, MatchSchedule newSchedule) {
        return !existing.getStartTime().equals(newSchedule.getStartTime()) ||
                !existing.getMatchStatus().equals(newSchedule.getMatchStatus()) ||
                !existing.getStageType().equals(newSchedule.getStageType()) ||
                !existing.getTeamScore1().equals(newSchedule.getTeamScore1()) ||
                !existing.getTeamScore2().equals(newSchedule.getTeamScore2()) ||
                !Objects.equals(existing.getTeamImg1(), newSchedule.getTeamImg1()) ||
                !Objects.equals(existing.getTeamImg2(), newSchedule.getTeamImg2());
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
            String imageUrl = backgroundImage.split("\\?")[0].substring(4).replaceAll("\"", "");

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
            } catch (CustomException e) {
                log.info("순위 일정 DB 저장 실패 !");
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_STORE_RANKING_DATA);
            }
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
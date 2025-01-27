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
import java.time.Duration;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CrawlingService {

    private final MatchScheduleRepository matchScheduleRepository;
    private final RankRepository rankRepository;

    public void getDataList(WebDriver driver) {

        try {
            matchScheduleRepository.deleteAll();
            log.info("일정 데이터 삭제 성공");
        } catch (CustomException e) {
            log.info("데이터 삭제 실패");
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_DELETE_SCHEDULE_DATA);
        }

        driver.get("https://game.naver.com/esports/League_of_Legends/schedule/lck");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".list_wrap__3zIxG")));

        List<WebElement> teamElements = driver.findElements(By.cssSelector(".row_item__dbJjy"));

        for (int i = 0; i < teamElements.size(); i++) {

            // 경기 날짜
            List<WebElement> dateInfo = driver.findElements(By.cssSelector(".card_date__1kdC3"));
            String date = dateInfo.get(i/2).getText().replace("오늘", "");

            // 각 경기 별 일정 크롤링
            teamElements = driver.findElements(By.cssSelector(".row_item__dbJjy"));
            WebElement team = teamElements.get(i);

            // 시작 시간
            WebElement timeElements = team.findElement(By.cssSelector(".row_time__28bwr"));
            String startTime = timeElements.getText();

            // 경기 결과 상태
            WebElement matchStatusElements = team.findElement(By.cssSelector(".row_state__2RKDU"));
            String matchStatus = matchStatusElements.getText();

            // 팀 이름
            WebElement teamElement = team.findElement(By.cssSelector(".row_box_score__1WQuz"));
            List<WebElement> teamNameElements = teamElement.findElements(By.cssSelector(".row_name__IDFHz"));
            String team1 = teamNameElements.get(0).getText();
            String team2 = teamNameElements.get(1).getText();

            // 점수
            String teamScore1 = "none";
            String teamScore2 = "none";

            if (!matchStatus.equals("예정")) {
                WebElement scoreElement = team.findElement(By.cssSelector(".row_box_score__1WQuz"));
                List<WebElement> numberElements = scoreElement.findElements(By.cssSelector(".row_score__2RmGQ"));
                teamScore1 = numberElements.get(0).getText();
                teamScore2 = numberElements.get(1).getText();
            }

            // 이미지
            List<WebElement> imageElements = team.findElements(By.cssSelector(".row_box_score__1WQuz img"));
            String teamImg1 = imageElements.get(0).getAttribute("src");
            String teamImg2 = imageElements.get(1).getAttribute("src");

            ScheduleData scheduleData = new ScheduleData(
                    date,
                    startTime,
                    matchStatus,
                    team1,
                    team2,
                    teamImg1,
                    teamImg2
            );

            log.info("scheduleData : " + scheduleData);

            try {
                MatchScheduleDto scheduleDto = MatchScheduleDto.builder()
                        .month(1)
                        .matchDate(scheduleData.date())
                        .startTime(scheduleData.startTime())
                        .team1(scheduleData.team1())
                        .team2(scheduleData.team2())
                        .matchStatus(scheduleData.matchStatus())
                        .teamScore1(teamScore1)
                        .teamScore2(teamScore2)
                        .teamImg1(scheduleData.teamImg1())
                        .teamImg2(scheduleData.teamImg2())
                        .build();

                matchScheduleRepository.save(scheduleDto.toEntity());
                log.info("경기 일정 DB 저장 완료");
            } catch (CustomException e) {
                log.info("경기 일정 DB 저장 실패");
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_STORE_SCHEDULE_DATA);
            }

        }
    }

    public void getRankingData(WebDriver driver) {

        try {
            rankRepository.deleteAll();
            log.info("순위 데이터 삭제 성공");
        } catch (CustomException e) {
            log.info("순위 데이터 삭제 실패");
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_DELETE_RANKING_DATA);
        }

        driver.get("https://game.naver.com/esports/League_of_Legends/record/lck/team/lck_2024_summer");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".record_list_wrap__A8cnT")));
        List<WebElement> dataList = driver.findElements(By.cssSelector(".record_list_item__2fFsp"));

        for (int i = 0; i < dataList.size()/2; i++) {

            // 팀 이름
            WebElement teamNameElement = dataList.get(i).findElement(By.cssSelector(".record_list_name__27huQ"));
            String teamName = teamNameElement.getText();

            // 팀 이미지
            WebElement teamImgElement = dataList.get(i).findElement(By.cssSelector(".record_list_thumb_logo__1s1BT"));
            String styleAttr = teamImgElement.getAttribute("style");
            String imageUrl = styleAttr.substring(styleAttr.indexOf("url(") + 4, styleAttr.indexOf(")"));
            imageUrl = imageUrl.replace("\"", "");

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
                log.info("순위 DB 저장 성공");
            } catch (CustomException e) {
                log.info("순위 일정 DB 저장 실패");
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_STORE_RANKING_DATA);
            }
        }
    }
}
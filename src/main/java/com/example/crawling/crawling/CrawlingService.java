package com.example.crawling.crawling;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.schedule.MatchScheduleDto;
import com.example.crawling.schedule.MatchScheduleRepository;
import com.example.crawling.rank.RankDto;
import com.example.crawling.rank.RankRepository;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CrawlingService {

    private final MatchScheduleRepository matchScheduleRepository;
    private final RankRepository rankRepository;

    public void getDataList(WebDriver driver) {

        try {
            matchScheduleRepository.deleteAll();
            System.out.println("일정 데이터 삭제 성공");
        } catch (CustomException e) {
            System.out.println("데이터 삭제 실패");
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_DELETE_SCHEDULE_DATA);
        }

        driver.get("https://game.naver.com/esports/League_of_Legends/schedule/lck");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".list_wrap__3zIxG")));

        List<WebElement> teamElements = driver.findElements(By.cssSelector(".row_item__dbJjy"));

        for (int i = 0; i < teamElements.size(); i++) {

            String date = null;
            String startTime = null;
            String matchStatus = null;
            String team1 = null;
            String team2 = null;
            String teamScore1 = "vs";
            String teamScore2 = "vs";
            String teamImg1 = null;
            String teamImg2 = null;

            // 경기 날짜
            List<WebElement> dateInfo = driver.findElements(By.cssSelector(".card_date__1kdC3"));

            date = dateInfo.get(i/2).getText().replace("오늘", "");

            System.out.println("경기 날짜: " + date);

            teamElements = driver.findElements(By.cssSelector(".row_item__dbJjy"));

            WebElement team = teamElements.get(i);

            // 시작 시간
            WebElement timeElements = team.findElement(By.cssSelector(".row_time__28bwr"));
            startTime = timeElements.getText();
            System.out.println("시작 시간 : " + startTime);

            // 경기 결과 상태
            WebElement matchStatusElements = team.findElement(By.cssSelector(".row_state__2RKDU"));
            matchStatus = matchStatusElements.getText();
            System.out.println("경기 진행 상태: " + matchStatus);

            // 팀 이름
            WebElement teamElement = team.findElement(By.cssSelector(".row_box_score__1WQuz"));

            List<WebElement> teamNameElements = teamElement.findElements(By.cssSelector(".row_name__IDFHz"));

            team1 = teamNameElements.get(0).getText();
            team2 = teamNameElements.get(1).getText();

            System.out.println("team 1: " + team1);
            System.out.println("team 2: " + team2);

            // 점수
            if (!matchStatus.equals("예정")) {
                WebElement scoreElement = team.findElement(By.cssSelector(".row_box_score__1WQuz"));

                List<WebElement> numberElements = scoreElement.findElements(By.cssSelector(".row_score__2RmGQ"));

                teamScore1 = numberElements.get(0).getText();
                teamScore2 = numberElements.get(1).getText();
            }

            System.out.println("team1 score: " + teamScore1);
            System.out.println("team2 score: " + teamScore2);

            // 이미지
            List<WebElement> imageElements = team.findElements(By.cssSelector(".row_box_score__1WQuz img"));

            teamImg1 = imageElements.get(0).getAttribute("src");
            teamImg2 = imageElements.get(1).getAttribute("src");

            System.out.println("teamImg1 : " + teamImg1);
            System.out.println("teamImg2 : " + teamImg2);

            try {
                MatchScheduleDto scheduleData = MatchScheduleDto.builder()
                        .month(1)
                        .matchDate(date)
                        .startTime(startTime)
                        .team1(team1)
                        .team2(team2)
                        .matchStatus(matchStatus)
                        .teamScore1(teamScore1)
                        .teamScore2(teamScore2)
                        .teamImg1(teamImg1)
                        .teamImg2(teamImg2)
                        .build();

                matchScheduleRepository.save(scheduleData.toEntity());
                System.out.println("경기 일정 DB 저장 완료");
            } catch (CustomException e) {
                System.out.println("경기 일정 DB 저장 실패");
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_STORE_SCHEDULE_DATA);
            }
        }
    }

    public void getRankingData(WebDriver driver) {

        String teamName;

        String winCnt;

        String loseCnt;

        String pointDiff;

        String winRate;

        String kda;

        String killCnt;

        String deathCnt;

        String assistCnt;

        try {
            rankRepository.deleteAll();
            System.out.println("순위 데이터 삭제 성공");
        } catch (CustomException e) {
            System.out.println("순위 데이터 삭제 실패");
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_DELETE_RANKING_DATA);
        }

        driver.get("https://game.naver.com/esports/League_of_Legends/record/lck/team/lck_2024_summer");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".record_list_wrap__A8cnT")));

        List<WebElement> dataList = driver.findElements(By.cssSelector(".record_list_item__2fFsp"));

        for (int i = 0; i < dataList.size()/2; i++) {

            // 팀 이름
            WebElement teamNameElement = dataList.get(i).findElement(By.cssSelector(".record_list_name__27huQ"));
            teamName = teamNameElement.getText();
            System.out.println(teamName);

            // 팀 이미지
            WebElement teamImgElement = dataList.get(i).findElement(By.cssSelector(".record_list_thumb_logo__1s1BT"));
            String styleAttr = teamImgElement.getAttribute("style");

            String imageUrl = styleAttr.substring(styleAttr.indexOf("url(") + 4, styleAttr.indexOf(")"));
            imageUrl = imageUrl.replace("\"", "");

            System.out.println("팀 로고 URL: " + imageUrl);


            List<WebElement> teamElements = dataList.get(i+10).findElements(By.cssSelector(".record_list_data__3wyY7"));

            winCnt = teamElements.get(0).getText();
            System.out.println("Win Count: " + winCnt);

            loseCnt = teamElements.get(1).getText();
            System.out.println("Lose Count: " + loseCnt);

            pointDiff = teamElements.get(2).getText();
            System.out.println("Point Difference: " + pointDiff);

            winRate = teamElements.get(3).getText();
            System.out.println("Win Rate: " + winRate);

            kda = teamElements.get(4).getText();
            System.out.println("KDA: " + kda);

            killCnt = teamElements.get(5).getText();
            System.out.println("Kill Count: " + killCnt);

            deathCnt = teamElements.get(6).getText();
            System.out.println("Death Count: " + deathCnt);

            assistCnt = teamElements.get(7).getText();
            System.out.println("Assist Count: " + assistCnt);

            try {
                RankDto rankData = RankDto.builder()
                        .teamName(teamName)
                        .teamImgUrl(imageUrl)
                        .winCnt(winCnt)
                        .loseCnt(loseCnt)
                        .pointDiff(pointDiff)
                        .winRate(winRate)
                        .kda(kda)
                        .killCnt(killCnt)
                        .deathCnt(deathCnt)
                        .assistCnt(assistCnt)
                        .build();

                rankRepository.save(rankData.toRankEntity());
                System.out.println("순위 DB 저장 성공");
            } catch (CustomException e) {
                System.out.println("순위 일정 DB 저장 실패");
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_STORE_RANKING_DATA);
            }
        }
    }
}
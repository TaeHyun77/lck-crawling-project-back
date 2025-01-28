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

            }
        }
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
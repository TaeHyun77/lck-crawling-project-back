package com.example.crawling.crawling;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.ranking.Ranking;
import com.example.crawling.ranking.RankingDto;
import com.example.crawling.ranking.RankingRedisService;
import com.example.crawling.ranking.RankingRepository;
import com.example.crawling.schedule.MatchSchedule;
import com.example.crawling.schedule.MatchScheduleDto;
import com.example.crawling.schedule.MatchScheduleRedisService;
import com.example.crawling.schedule.MatchScheduleRepository;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@Service
public class CrawlingService {

    private final MatchScheduleRepository matchScheduleRepository;
    private final RankingRepository rankingRepository;
    private final MatchScheduleRedisService matchScheduleRedisService;
    private final RankingRedisService rankingRedisService;


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
            String date = dateInfo.getText().replace("오늘", "");

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
    public void saveOrUpdateMatchSchedule(MatchSchedule matchSchedule) { // matchSchedule : 새로운 데이터

        // redis에서 값을 찾음
        Optional<MatchSchedule> redisSchedule = matchScheduleRedisService.getMatchSchedule(
                matchSchedule.getMatchDate(), matchSchedule.getStartTime());

        // 변경 되지 않은 경우
        if (redisSchedule.isPresent() && !isMatchScheduleChanged(redisSchedule.get(), matchSchedule)) {
            log.info("변경 없음: " + redisSchedule.get().getMatchDate() + " " + redisSchedule.get().getTeam1() + "-" + redisSchedule.get().getTeam2());
            return;
        }

        // 변경된 경우 DB 업데이트
        Optional<MatchSchedule> existingSchedule = matchScheduleRepository.findByMatchDateAndStartTime( // existingSchedule : db에 존재 하는 데이터
                matchSchedule.getMatchDate(), matchSchedule.getStartTime());

        if (existingSchedule.isPresent()) {
            MatchSchedule schedule = existingSchedule.get();

            schedule.updateMatchSchedule(
                    matchSchedule.getTeam1(), matchSchedule.getTeam2(), matchSchedule.getMatchStatus(),
                    matchSchedule.getStageType(), matchSchedule.getTeamScore1(), matchSchedule.getTeamScore2(),
                    matchSchedule.getTeamImg1(), matchSchedule.getTeamImg2());

            matchScheduleRepository.save(schedule);
            log.info("DB 업데이트: " + matchSchedule.getMatchDate() + " " + matchSchedule.getTeam1() + "-" + matchSchedule.getTeam2());
        } else {
            matchScheduleRepository.save(matchSchedule);
            log.info("새로운 경기 일정 DB 저장");
        }

        // Redis 최신화
        matchScheduleRedisService.getOrUpdateMatchSchedule(matchSchedule.getMatchDate(), matchSchedule.getStartTime());
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


    public void getRanking(WebDriver driver) {

        driver.get("https://esports.op.gg/standings/lck");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.content.w-full.flex-1.transition-all")));

        List<WebElement> tabs = driver.findElements(By.cssSelector(".flex.lg\\:p-1.lg\\:mx-0.mx-3.rounded.bg-gray-800.mb-2 > div"));

        for (WebElement tab : tabs) {

            tab.click();

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".dark\\:border-gray-900.border-t.border-gray-200.cursor-pointer.lg\\:h-12.h-10.hover\\:bg-gray-200.dark\\:bg-gray-800.dark\\:hover\\:bg-gray-850.border-t-gray-900")));

            List<WebElement> dataList = driver.findElements(By.cssSelector(".dark\\:border-gray-900.border-t.border-gray-200.cursor-pointer.lg\\:h-12.h-10.hover\\:bg-gray-200.dark\\:bg-gray-800.dark\\:hover\\:bg-gray-850.border-t-gray-900"));

            for (WebElement webElement : dataList) {

                String stage = tab.getText();

                String all = webElement.getText();
                String[] parts = all.split("\n");

                // 순위
                int teamRank = Integer.parseInt(parts[0]);

                // 이미지
                WebElement imgElement = webElement.findElement(By.cssSelector(".flex.items-center.text-t2 img"));
                String img = imgElement.getAttribute("src");

                // 팀 명
                WebElement teamNameElement = webElement.findElement(By.cssSelector(".flex.items-center.text-t2 span"));
                String teamName = teamNameElement.getText();

                // 승패
                WebElement recordElement = webElement.findElement(By.cssSelector(".whitespace-nowrap"));
                String record = recordElement.getText().split("\\d+%")[0].trim();

                // 세트당 승패
                String recordSet = parts[3].replaceAll("(\\d+W \\d+L).*", "$1");

                RankingData rankingData = new RankingData(
                        stage,
                        teamRank,
                        img,
                        teamName,
                        record,
                        recordSet
                );

                try {
                    RankingDto rankingDto = RankingDto.builder()
                            .stage(rankingData.stage())
                            .teamRank(rankingData.teamRank())
                            .img(rankingData.img())
                            .teamName(rankingData.teamName())
                            .record(rankingData.record())
                            .recordSet(rankingData.recordSet())
                            .build();

                    saveOrUpdateRanking(rankingDto.toRanking(), stage);
                } catch (CustomException e) {
                    log.info("랭킹 데이터 DB 저장 실패 !");
                    throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_STORE_RANKING_DATA);
                }
            }
        }
    }

    @Transactional
    public void saveOrUpdateRanking(Ranking ranking, String stage) {

        // redis ranking 데이터
        Optional<Ranking> redisRanking = rankingRedisService.getRanking(stage, ranking.getTeamName());

        // redis 데이터 존재하면
        if (redisRanking.isPresent() && !isRankingChanged(redisRanking.get(), ranking)) {
            log.info("변경 없음: " + redisRanking.get().getStage() + " " + redisRanking.get().getTeamName());
            return;
        }

        Optional<Ranking> existRanking; // DB에 저장된 값

        try {
            existRanking = rankingRepository.findByTeamNameAndStage(ranking.getTeamName(), stage);
        } catch (CustomException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_RANKING_DATA);
        }

        if (existRanking.isPresent()) {
            Ranking rankingData = existRanking.get();

            if (isRankingChanged(rankingData, ranking)) {
                rankingData.updateRanking(ranking.getTeamRank(), ranking.getRecord(), ranking.getRecordSet());

                rankingRepository.save(rankingData);
                log.info(stage + " " + rankingData.getTeamName() + " 의 순위 정보가 업데이트 되었습니다.");
            } else {
                log.info(stage + " " + rankingData.getTeamName() + " 의 순위 정보가 업데이트 되지 않았습니다.");
            }
        } else {
            rankingRepository.save(ranking);
            log.info("새로운 순위 저장");
        }

        rankingRedisService.getOrUpdateRanking(ranking.getStage(), ranking.getTeamName());
    }

    private boolean isRankingChanged(Ranking existing, Ranking newRanking) {
        return existing.getTeamRank() != newRanking.getTeamRank() ||
                !existing.getRecord().equals(newRanking.getRecord()) ||
                !existing.getRecordSet().equals(newRanking.getRecordSet());
    }
}
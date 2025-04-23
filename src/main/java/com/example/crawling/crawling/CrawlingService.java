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
import java.util.*;


@Slf4j
@RequiredArgsConstructor
@Service
public class CrawlingService {

    private final MatchScheduleRepository matchScheduleRepository;
    private final RankingRepository rankingRepository;
    private final MatchScheduleRedisService matchScheduleRedisService;
    private final RankingRedisService rankingRedisService;

    public void getDataList(WebDriver driver) {

        driver.get("https://game.naver.com/esports/League_of_Legends/schedule/lck");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".schedule_calendar_list_month__1VIHT")));

        List<WebElement> allMonthElements = driver.findElements(By.cssSelector("a.schedule_calendar_month__2mWJA"));
        Set<Integer> targetMonths = Set.of(1, 2, 4, 5, 6, 7, 8);

        // WebElement 대신 정보를 저장할 DTO 리스트
        List<MonthLinkInfo> monthLinkInfos = new ArrayList<>();

        for (WebElement monthElement : allMonthElements) {
            try {
                String href = monthElement.getAttribute("href");
                String monthText = monthElement.findElement(By.cssSelector("span")).getText();
                int month = Integer.parseInt(monthText.replace("월", ""));

                monthLinkInfos.add(new MonthLinkInfo(href, month));
            } catch (Exception e) {
                log.warn("월 정보 파싱 중 오류 발생 - 건너뜀", e);
            }
        }

        for (MonthLinkInfo info : monthLinkInfos) {
            int month = info.getMonth();
            String href = info.getHref();

            if (!targetMonths.contains(month)) {
                log.info(month + "월은 크롤링 대상이 아님 - 건너뜀");
                continue;
            }

            boolean shouldCrawl = month == 4 || !matchScheduleRepository.existsByMonth(month);

            if (shouldCrawl) {
                log.info(month + "월 크롤링 시작!");

                driver.manage().deleteAllCookies();
                driver.get(href);

                JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
                jsExecutor.executeScript("location.reload();");

                crawlScheduleData(driver, month);
            } else {
                log.info(month + "월은 이미 크롤링됨 - 건너뜀");
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
        driver.get("https://game.naver.com/esports/League_of_Legends/home");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".ranking_group_table__3ktgw")));

        List<WebElement> rankingGroups = driver.findElements(By.cssSelector(".ranking_group__1q_o1"));

        for (WebElement group : rankingGroups) {
            // 그룹 내의 팀 랭킹 행들
            List<WebElement> rows = group.findElements(By.cssSelector(".ranking_item_row__2P7f8"));

            for (WebElement row : rows) {
                try {
                    // 순위
                    int teamRank = Integer.parseInt(row.findElement(By.cssSelector(".ranking_rank__1YjIT")).getText().trim());

                    // 이미지 URL
                    String img = row.findElement(By.cssSelector("img.ranking_image__1fTV-")).getAttribute("src");

                    // 팀명
                    String teamName = row.findElement(By.cssSelector(".ranking_text__3Q3qr")).getText().trim();

                    // 승/패/승률/득실차 값들
                    List<WebElement> stats = row.findElements(By.cssSelector("td.ranking_item__rMCiZ > span.ranking_num___owTL"));

                    int winCnt = Integer.parseInt(stats.get(0).getText().trim());
                    int loseCnt = Integer.parseInt(stats.get(1).getText().trim());
                    double winRate = Double.parseDouble(stats.get(2).getText().trim());
                    int pointDiff = Integer.parseInt(stats.get(3).getText().trim());

                    Ranking ranking = Ranking.builder()
                            .teamRank(teamRank)
                            .img(img)
                            .teamName(teamName)
                            .winCnt(winCnt)
                            .loseCnt(loseCnt)
                            .winRate(winRate)
                            .pointDiff(pointDiff)
                            .build();

                    saveOrUpdateRanking(ranking);

                } catch (Exception e) {
                    log.warn("랭킹 정보 파싱 중 오류 발생: " + e.getMessage(), e);
                    throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_STORE_RANKING_DATA);
                }
            }
        }
    }

    @Transactional
    public void saveOrUpdateRanking(Ranking ranking) {

        // redis ranking 데이터
        Optional<Ranking> redisRanking = rankingRedisService.getRanking(ranking.getTeamName());

        // redis 데이터 존재하면
        if (redisRanking.isPresent() && !isRankingChanged(redisRanking.get(), ranking)) {
            log.info(redisRanking.get().getTeamName() + "의 순위 데이터 변경 없음");
            return;
        }

        Optional<Ranking> existRanking; // DB에 저장된 값

        try {
            existRanking = rankingRepository.findByTeamName(ranking.getTeamName());
        } catch (CustomException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_RANKING_DATA);
        }

        // DB에 랭킹 데이터가 존재하면
        if (existRanking.isPresent()) {
            Ranking rankingData = existRanking.get();

                if (isRankingChanged(rankingData, ranking)) { // ( 기존 데이터, 새 데이터 )
                rankingData.updateRanking(ranking.getTeamRank(), ranking.getWinCnt(), ranking.getLoseCnt(), ranking.getWinRate(), ranking.getPointDiff());

                rankingRepository.save(rankingData);
                log.info(rankingData.getTeamName() + " 의 순위 정보가 업데이트 되었습니다.");
            } else {
                log.info(rankingData.getTeamName() + " 의 순위 정보가 업데이트 되지 않았습니다.");
            }
        } else {
            rankingRepository.save(ranking);
            log.info("새로운 순위 저장");
        }

        rankingRedisService.getOrUpdateRanking(ranking.getTeamName());
    }

    private boolean isRankingChanged(Ranking existing, Ranking newRanking) {
        return existing.getTeamRank() != newRanking.getTeamRank() ||
                existing.getWinCnt() != newRanking.getWinCnt() ||
                existing.getLoseCnt() != newRanking.getLoseCnt() ||
                existing.getWinRate() != newRanking.getWinRate() ||
                existing.getPointDiff() != newRanking.getPointDiff();
    }
}
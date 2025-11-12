package com.example.crawling.crawling;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.ranking.Ranking;
import com.example.crawling.ranking.RankingRepository;
import com.example.crawling.schedule.MatchSchedule;
import com.example.crawling.schedule.MatchScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
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

    // LCK 일정 정보 크롤링
    public void getDataList(WebDriver driver) {

        // Lck 일정 정보 페이지
        driver.get("https://game.naver.com/esports/League_of_Legends/schedule/lck");

        // 경기가 있는 달 정보를 담을 리스트
        Map<Integer, String> monthLinkInfos = activeMonthList(driver);

        // 현재 달 구하기
        LocalDate today = LocalDate.now();

        int currentMonth = 9; // today.getMonthValue();

        for (int month: monthLinkInfos.keySet()) {
            if (month != currentMonth) {
                log.info("{}월 데이터는 이번 달 데이터가 아닙니다.", month);
            } else {
                driver.get(monthLinkInfos.get(currentMonth));

                try {
                    log.info("{}월의 일정 정보를 크롤링합니다.", currentMonth);
                    crawlingLckSchedule(driver, currentMonth);
                    log.info("{}월의 일정 정보를 크롤링이 완료되었습니다.", currentMonth);
                } catch (Exception e) {
                    log.info("LCK 일정 크롤링 실패");
                    throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_CRAWLING_LCK_DATA);
                }
            }
        }
    }

    private void crawlingLckSchedule(WebDriver driver, int currentMonth)  {

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
                    crawlingMatchSchedule.getTeam1(), crawlingMatchSchedule.getTeam2(),
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
        return !existing.getStartTime().equals(newSchedule.getStartTime()) ||
                !existing.getMatchStatus().equals(newSchedule.getMatchStatus()) ||
                !existing.getStageType().equals(newSchedule.getStageType()) ||
                !existing.getTeamScore1().equals(newSchedule.getTeamScore1()) ||
                !existing.getTeamScore2().equals(newSchedule.getTeamScore2()) ||
                !Objects.equals(existing.getTeamImg1(), newSchedule.getTeamImg1()) ||
                !Objects.equals(existing.getTeamImg2(), newSchedule.getTeamImg2());
    }

    // LCK 팀 순위 크롤링
    public void getRanking(WebDriver driver) {

        driver.get("https://game.naver.com/esports/League_of_Legends/record/lck/team/lck_2025");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div._season__container__lXQAy")));

        // 그룹 이름 목록 (LEGEND 그룹, RISE 그룹 등)
        List<WebElement> matchGroupTitles = driver.findElements(By.cssSelector(".record_list_group__x2V7Q"));

        // 각 그룹의 실제 랭킹 정보 DIV
        List<WebElement> rankingmatchGroups = driver.findElements(By.cssSelector("div.record_list_wrap__A8cnT"));

        for (int g = 0; g < rankingmatchGroups.size(); g++) {
            WebElement matchGroup = rankingmatchGroups.get(g);
            String matchGroupName = (g < matchGroupTitles.size())
                    ? matchGroupTitles.get(g).getText().trim()
                    : "Unknown Group";

            try {
                // 팀명/순위/로고 정보
                List<WebElement> teamRows = matchGroup.findElements(By.cssSelector("ul.record_list_team__2NtZO > li.record_list_item__2fFsp"));

                // 승/패/득실차/승률 정보
                List<WebElement> scoreRows = matchGroup.findElements(By.cssSelector("div.record_list_wrap_list__lkd3u ul > li.record_list_item__2fFsp"));

                // 팀 개수
                int teamCount = Math.min(teamRows.size(), scoreRows.size());

                for (int i = 0; i < teamCount; i++) {
                    WebElement team = teamRows.get(i);
                    WebElement score = scoreRows.get(i);

                    // 팀 순위
                    int teamRank = Integer.parseInt(team.findElement(By.cssSelector(".record_list_rank__3mn_o")).getText().trim());

                    // 팀명
                    String teamName = team.findElement(By.cssSelector(".record_list_name__27huQ")).getText().trim();

                    // 팀 이미지
                    String imageUrl = "";
                    WebElement logo = team.findElement(By.cssSelector("span.record_list_thumb_logo__1s1BT"));

                    // lazy-load 대비: 화면 중앙으로 이동
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", logo);

                    // 대기
                    WebDriverWait waitImg = new WebDriverWait(driver, Duration.ofSeconds(8));
                    try {
                        waitImg.until(d -> {
                            String bg = (String) ((JavascriptExecutor) d).executeScript(
                                    "return window.getComputedStyle(arguments[0]).backgroundImage;", logo);
                            // none이 아니거나, img 태그가 존재하면 통과
                            boolean hasBg = bg != null && bg.contains("url(");
                            boolean hasImg = !logo.findElements(By.tagName("img")).isEmpty();
                            return hasBg || hasImg;
                        });

                        String bg = (String) ((JavascriptExecutor) driver).executeScript(
                                "return window.getComputedStyle(arguments[0]).backgroundImage;", logo);
                        if (bg != null && bg.contains("url(")) {
                            imageUrl = bg.replaceAll(".*url\\((?:\"|')?(.*?)(?:\"|')?\\).*", "$1");
                        } else {
                            WebElement img = logo.findElement(By.tagName("img"));
                            imageUrl = img.getAttribute("src");
                        }

                    } catch (TimeoutException e) {
                        imageUrl = ""; // 그래도 못 찾으면 빈 문자열
                    }

                    List<WebElement> stats = score.findElements(By.cssSelector("span.record_list_data__3wyY7"));
                    int winCnt = Integer.parseInt(stats.get(0).getText().trim()); // 승리 수
                    int loseCnt = Integer.parseInt(stats.get(1).getText().trim()); // 패배 수
                    int pointDiff = Integer.parseInt(stats.get(2).getText().trim()); // 득점차
                    double winRate = Double.parseDouble(stats.get(3).getText().trim()); // 승률

                    Ranking crawlingRanking = Ranking.builder()
                            .matchGroup(matchGroupName)
                            .teamRank(teamRank)
                            .img(imageUrl)
                            .teamName(teamName)
                            .winCnt(winCnt)
                            .loseCnt(loseCnt)
                            .winRate(winRate)
                            .pointDiff(pointDiff)
                            .build();

                    checkUpdate(crawlingRanking);

                    // System.out.printf("[%s] %d위 %s | W:%d L:%d Diff:%d WinRate:%.2f%n", matchGroupName, teamRank, teamName, winCnt, loseCnt, pointDiff, winRate);
                }

            } catch (Exception e) {
                log.warn("랭킹 정보 파싱 중 오류 발생: {}", e.getMessage(), e);
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_STORE_RANKING_DATA);
            }
        }
    }

    @Transactional
    public void checkUpdate(Ranking crawlingRanking) {

        Ranking dbRanking = rankingRepository.findByTeamName(crawlingRanking.getTeamName())
                .orElseGet(() -> rankingRepository.save(crawlingRanking));

        if (isRankingChanged(dbRanking, crawlingRanking)) {
            dbRanking.updateRanking(
                    crawlingRanking.getMatchGroup(), crawlingRanking.getTeamRank(),
                    crawlingRanking.getWinCnt(), crawlingRanking.getLoseCnt(),
                    crawlingRanking.getWinRate(), crawlingRanking.getPointDiff()
                    ,crawlingRanking.getImg()
            );

            log.info("순위 정보가 갱신되었습니다.");
            rankingRepository.save(dbRanking);
        }

    }

    // 순위 정보가 변경되었는지 여부
    private boolean isRankingChanged(Ranking existing, Ranking newRanking) {
        return  !existing.getMatchGroup().equals(newRanking.getMatchGroup()) ||
                existing.getTeamRank() != newRanking.getTeamRank() ||
                existing.getWinCnt() != newRanking.getWinCnt() ||
                existing.getLoseCnt() != newRanking.getLoseCnt() ||
                existing.getWinRate() != newRanking.getWinRate() ||
                existing.getPointDiff() != newRanking.getPointDiff() ||
                !existing.getImg().equals(newRanking.getImg());
    }

    // 경기가 존재하는 달을 구하는 로직
    private Map<Integer, String> activeMonthList(WebDriver driver) {
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
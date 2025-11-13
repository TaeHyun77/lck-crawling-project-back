package com.example.crawling.ranking;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class RankingService {

    private final RankingRepository rankingRepository;

    // LCK 팀 순위 크롤링
    public void crawlingRanking(WebDriver driver) {

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
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_CRAWLING_RANKING_DATA);
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

    public List<RankingResponseDto> getAllRanking() {
        return rankingRepository.findAll().stream()
                .map(ranking -> RankingResponseDto.builder()
                        .teamRank(ranking.getTeamRank())
                        .img(ranking.getImg())
                        .teamName(ranking.getTeamName())
                        .winCnt(ranking.getWinCnt())
                        .loseCnt(ranking.getLoseCnt())
                        .winRate(ranking.getWinRate())
                        .pointDiff(ranking.getPointDiff())
                        .matchGroup(ranking.getMatchGroup())
                        .build())
                .collect(Collectors.toList());
    }
}

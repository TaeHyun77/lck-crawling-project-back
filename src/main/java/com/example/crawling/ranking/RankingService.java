package com.example.crawling.ranking;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.ranking.dto.RankingResponseDto;
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
    private final RankingUpdateService rankingUpdateService;
    private final RankingRepository rankingRepository;
    private static final String RANKING_URL = "https://game.naver.com/esports/League_of_Legends/record/lck/team/lck_2026";

    // LCK 팀 순위 크롤링
    public void crawlingRanking(WebDriver driver) {
        driver.get(RANKING_URL);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("strong[class*='_group_']")));

        // 그룹 이름 목록 (LEGEND 그룹, RISE 그룹 등)
        List<WebElement> matchGroupTitles = driver.findElements(By.cssSelector("strong[class*='_group_']"));

        // 그룹별 팀명/순위/로고 UL 목록
        List<WebElement> teamLists = driver.findElements(By.cssSelector("[class*='_wrap_team_'] ul"));

        // 그룹별 승/패/득실차/승률 UL 목록
        List<WebElement> statsLists = driver.findElements(By.cssSelector("[class*='_wrap_filter_'] ul"));

        for (int g = 0; g < matchGroupTitles.size(); g++) {
            String matchGroupName = matchGroupTitles.get(g).getText().trim();

            try {
                List<WebElement> teamRows = teamLists.get(g).findElements(By.cssSelector("li"));
                List<WebElement> statsRows = statsLists.get(g).findElements(By.cssSelector("li"));

                int teamCount = Math.min(teamRows.size(), statsRows.size());

                for (int i = 0; i < teamCount; i++) {
                    WebElement team = teamRows.get(i);
                    WebElement statsRow = statsRows.get(i);

                    int teamRank = Integer.parseInt(team.findElement(By.cssSelector("[class*='_rank_']")).getText().trim());

                    // data-long-name 속성은 팀명 span에만 존재하므로 안정적인 셀렉터로 사용
                    String teamName = team.findElement(By.cssSelector("span[data-long-name]")).getText().trim();

                    String imageUrl = "";
                    WebElement logo = team.findElement(By.cssSelector("[class*='_thumb_logo_']"));

                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", logo);

                    WebDriverWait waitImg = new WebDriverWait(driver, Duration.ofSeconds(8));
                    try {
                        waitImg.until(d -> {
                            String bg = (String) ((JavascriptExecutor) d).executeScript(
                                    "return window.getComputedStyle(arguments[0]).backgroundImage;", logo);
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
                        imageUrl = "";
                    }

                    List<WebElement> stats = statsRow.findElements(By.cssSelector("[class*='_data_']"));
                    int winCnt = Integer.parseInt(stats.get(0).getText().trim());
                    int loseCnt = Integer.parseInt(stats.get(1).getText().trim());
                    // 득실차는 음수 또는 양수 부호가 붙을 수 있음
                    int pointDiff = Integer.parseInt(stats.get(2).getText().trim().replace("+", ""));
                    double winRate = Double.parseDouble(stats.get(3).getText().trim());

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

                    rankingUpdateService.updateRankingIfChanged(crawlingRanking);
                }

            } catch (Exception e) {
                log.warn("랭킹 정보 파싱 중 오류 발생: {}", e.getMessage(), e);
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_CRAWLING_RANKING_DATA);
            }
        }
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

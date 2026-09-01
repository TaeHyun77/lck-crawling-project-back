# 크롤링 전략 패턴 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Selenium / Playwright / API 세 가지 크롤링 전략을 인터페이스로 추상화하고 성능을 비교 측정한다.

**Architecture:** `RankingFetchStrategy`, `ScheduleFetchStrategy` 인터페이스를 정의하고 각각 3가지 구현체를 만든다. `StrategyCrawlingService`가 두 전략을 주입받아 비동기 실행한다. 기존 Selenium 코드(`RankingService`, `MatchScheduleService`)는 수정하지 않는다.

**Tech Stack:** Java 17, Spring Boot 3.4.1, Playwright Java 1.44.0, Selenium 4.20.0, Gradle

---

## 파일 구조

```
신규 생성
├── src/main/java/com/example/crawling/
│   ├── crawling/strategy/
│   │   ├── RankingFetchStrategy.java
│   │   ├── ScheduleFetchStrategy.java
│   │   ├── ranking/
│   │   │   ├── SeleniumRankingFetcher.java
│   │   │   ├── PlaywrightRankingFetcher.java
│   │   │   └── ApiRankingFetcher.java
│   │   └── schedule/
│   │       ├── SeleniumScheduleFetcher.java
│   │       ├── PlaywrightScheduleFetcher.java
│   │       └── ApiScheduleFetcher.java
│   ├── crawling/StrategyCrawlingService.java
│   └── global/config/PlaywrightBrowserFactory.java
└── src/test/java/com/example/crawling/
    └── CrawlingStrategyTimeMeasure.java

수정
└── build.gradle  (Playwright 의존성 추가)
```

---

### Task 1: Playwright 의존성 추가 및 브라우저 설치

**Files:**
- Modify: `build.gradle`

- [ ] **Step 1: build.gradle에 Playwright 의존성 추가**

`dependencies` 블록 안 Selenium 의존성 바로 아래에 추가:

```groovy
implementation 'com.microsoft.playwright:playwright:1.44.0'
```

- [ ] **Step 2: 의존성 다운로드 확인**

```bash
./gradlew dependencies --configuration compileClasspath | grep playwright
```

Expected:
```
com.microsoft.playwright:playwright:1.44.0
```

- [ ] **Step 3: Playwright Chromium 브라우저 바이너리 설치**

`build.gradle` 하단에 태스크 추가:
```groovy
tasks.register('installPlaywright', JavaExec) {
    dependsOn compileJava
    classpath = sourceSets.main.runtimeClasspath
    mainClass = 'com.microsoft.playwright.CLI'
    args = ['install', 'chromium']
}
```

실행:
```bash
./gradlew installPlaywright
```

Expected: `Chromium ...% downloaded` 로그 후 완료

- [ ] **Step 4: 컴파일 확인**

```bash
./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 커밋**

```bash
git add build.gradle
git commit -m "chore Playwright 의존성 추가"
```

---

### Task 2: 인터페이스 생성

**Files:**
- Create: `src/main/java/com/example/crawling/crawling/strategy/RankingFetchStrategy.java`
- Create: `src/main/java/com/example/crawling/crawling/strategy/ScheduleFetchStrategy.java`

- [ ] **Step 1: RankingFetchStrategy 생성**

```java
package com.example.crawling.crawling.strategy;

public interface RankingFetchStrategy {
    void fetchAndSave();
}
```

- [ ] **Step 2: ScheduleFetchStrategy 생성**

```java
package com.example.crawling.crawling.strategy;

public interface ScheduleFetchStrategy {
    void fetchAndSave();
}
```

- [ ] **Step 3: 컴파일 확인**

```bash
./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/example/crawling/crawling/strategy/
git commit -m "feat 크롤링 전략 인터페이스 추가"
```

---

### Task 3: PlaywrightBrowserFactory 생성

**Files:**
- Create: `src/main/java/com/example/crawling/global/config/PlaywrightBrowserFactory.java`

- [ ] **Step 1: PlaywrightBrowserFactory 생성**

`WebDriverFactory`와 동일한 역할. headless Chromium을 반환한다.

```java
package com.example.crawling.global.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

import java.util.List;

public class PlaywrightBrowserFactory {

    public static Browser createBrowser(Playwright playwright) {
        return playwright.chromium().launch(
            new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(List.of(
                    "--no-sandbox",
                    "--disable-gpu",
                    "--disable-cache"
                ))
        );
    }
}
```

- [ ] **Step 2: 컴파일 확인**

```bash
./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/example/crawling/global/config/PlaywrightBrowserFactory.java
git commit -m "feat PlaywrightBrowserFactory 추가"
```

---

### Task 4: Selenium 어댑터 Fetcher 생성

기존 `RankingService`, `MatchScheduleService`를 위임하는 어댑터. WebDriver 생성/종료만 감싼다.

**Files:**
- Create: `src/main/java/com/example/crawling/crawling/strategy/ranking/SeleniumRankingFetcher.java`
- Create: `src/main/java/com/example/crawling/crawling/strategy/schedule/SeleniumScheduleFetcher.java`

- [ ] **Step 1: SeleniumRankingFetcher 생성**

```java
package com.example.crawling.crawling.strategy.ranking;

import com.example.crawling.crawling.strategy.RankingFetchStrategy;
import com.example.crawling.global.config.WebDriverFactory;
import com.example.crawling.ranking.application.RankingService;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SeleniumRankingFetcher implements RankingFetchStrategy {

    private final RankingService rankingService;

    @Override
    public void fetchAndSave() {
        WebDriver driver = WebDriverFactory.createWebDriver();
        try {
            rankingService.crawlingRanking(driver);
        } finally {
            driver.quit();
        }
    }
}
```

- [ ] **Step 2: SeleniumScheduleFetcher 생성**

```java
package com.example.crawling.crawling.strategy.schedule;

import com.example.crawling.crawling.strategy.ScheduleFetchStrategy;
import com.example.crawling.global.config.WebDriverFactory;
import com.example.crawling.schedule.application.MatchScheduleService;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SeleniumScheduleFetcher implements ScheduleFetchStrategy {

    private final MatchScheduleService matchScheduleService;

    @Override
    public void fetchAndSave() {
        WebDriver driver = WebDriverFactory.createWebDriver();
        try {
            matchScheduleService.crawlingSchedules(driver);
        } finally {
            driver.quit();
        }
    }
}
```

- [ ] **Step 3: 컴파일 확인**

```bash
./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/example/crawling/crawling/strategy/
git commit -m "feat Selenium 어댑터 Fetcher 추가"
```

---

### Task 5: API 어댑터 Fetcher 생성

기존 `RankingApiService`, `MatchScheduleApiService`를 단순 위임하는 어댑터.

**Files:**
- Create: `src/main/java/com/example/crawling/crawling/strategy/ranking/ApiRankingFetcher.java`
- Create: `src/main/java/com/example/crawling/crawling/strategy/schedule/ApiScheduleFetcher.java`

- [ ] **Step 1: ApiRankingFetcher 생성**

```java
package com.example.crawling.crawling.strategy.ranking;

import com.example.crawling.crawling.strategy.RankingFetchStrategy;
import com.example.crawling.ranking.application.RankingApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ApiRankingFetcher implements RankingFetchStrategy {

    private final RankingApiService rankingApiService;

    @Override
    public void fetchAndSave() {
        rankingApiService.fetchAndSaveRanking();
    }
}
```

- [ ] **Step 2: ApiScheduleFetcher 생성**

```java
package com.example.crawling.crawling.strategy.schedule;

import com.example.crawling.crawling.strategy.ScheduleFetchStrategy;
import com.example.crawling.schedule.application.MatchScheduleApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ApiScheduleFetcher implements ScheduleFetchStrategy {

    private final MatchScheduleApiService matchScheduleApiService;

    @Override
    public void fetchAndSave() {
        matchScheduleApiService.fetchAndSaveSchedule();
    }
}
```

- [ ] **Step 3: 컴파일 확인**

```bash
./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/example/crawling/crawling/strategy/
git commit -m "feat API 어댑터 Fetcher 추가"
```

---

### Task 6: StrategyCrawlingService 생성

두 전략을 주입받아 비동기로 실행하는 오케스트레이터.
같은 인터페이스 구현체가 3개이므로 `@Qualifier`로 주입 대상을 명시한다.
기본값은 Playwright로 설정한다.

**Files:**
- Create: `src/main/java/com/example/crawling/crawling/StrategyCrawlingService.java`

- [ ] **Step 1: StrategyCrawlingService 생성**

```java
package com.example.crawling.crawling;

import com.example.crawling.crawling.strategy.RankingFetchStrategy;
import com.example.crawling.crawling.strategy.ScheduleFetchStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class StrategyCrawlingService {

    private final RankingFetchStrategy rankingStrategy;
    private final ScheduleFetchStrategy scheduleStrategy;
    private final Executor crawlingExecutor;

    public StrategyCrawlingService(
            @Qualifier("playwrightRankingFetcher") RankingFetchStrategy rankingStrategy,
            @Qualifier("playwrightScheduleFetcher") ScheduleFetchStrategy scheduleStrategy,
            @Qualifier("crawlingExecutor") Executor crawlingExecutor
    ) {
        this.rankingStrategy = rankingStrategy;
        this.scheduleStrategy = scheduleStrategy;
        this.crawlingExecutor = crawlingExecutor;
    }

    public CompletableFuture<Void> crawlAsync() {
        return CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> {
                    log.info("순위 전략 실행 스레드 = {}", Thread.currentThread().getName());
                    rankingStrategy.fetchAndSave();
                }, crawlingExecutor),
                CompletableFuture.runAsync(() -> {
                    log.info("일정 전략 실행 스레드 = {}", Thread.currentThread().getName());
                    scheduleStrategy.fetchAndSave();
                }, crawlingExecutor)
        );
    }
}
```

- [ ] **Step 2: 컴파일 확인**

`PlaywrightRankingFetcher`, `PlaywrightScheduleFetcher`가 아직 없어서 빈 충돌이 날 수 있다.
컴파일만 확인하고 `@Qualifier` 대상 빈은 Task 7, 8에서 생성한다.

```bash
./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL` (컴파일 오류 없음. 런타임 빈 오류는 Task 7, 8 완료 후 해소)

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/example/crawling/crawling/StrategyCrawlingService.java
git commit -m "feat StrategyCrawlingService 추가"
```

---

### Task 7: PlaywrightRankingFetcher 구현

`RankingService.crawlingRanking()`의 Playwright 버전. 동일한 셀렉터를 사용하되 Playwright API로 재작성한다.

**Files:**
- Create: `src/main/java/com/example/crawling/crawling/strategy/ranking/PlaywrightRankingFetcher.java`

- [ ] **Step 1: PlaywrightRankingFetcher 생성**

```java
package com.example.crawling.crawling.strategy.ranking;

import com.example.crawling.crawling.strategy.RankingFetchStrategy;
import com.example.crawling.global.config.PlaywrightBrowserFactory;
import com.example.crawling.ranking.domain.Ranking;
import com.example.crawling.ranking.application.RankingUpdateService;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.TimeoutError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class PlaywrightRankingFetcher implements RankingFetchStrategy {

    private final RankingUpdateService rankingUpdateService;
    private static final String RANKING_URL =
            "https://game.naver.com/esports/League_of_Legends/record/lck/team/lck_2026";

    @Override
    public void fetchAndSave() {
        try (Playwright pw = Playwright.create();
             Browser browser = PlaywrightBrowserFactory.createBrowser(pw)) {

            Page page = browser.newPage();
            page.navigate(RANKING_URL);
            page.waitForSelector("strong[class*='_group_']");

            List<Locator> groupTitles = page.locator("strong[class*='_group_']").all();
            List<Locator> teamLists   = page.locator("[class*='_wrap_team_'] ul").all();
            List<Locator> statsLists  = page.locator("[class*='_wrap_filter_'] ul").all();

            for (int g = 0; g < groupTitles.size(); g++) {
                String matchGroupName = groupTitles.get(g).textContent().trim();

                List<Locator> teamRows  = teamLists.get(g).locator("li").all();
                List<Locator> statsRows = statsLists.get(g).locator("li").all();
                int teamCount = Math.min(teamRows.size(), statsRows.size());

                for (int i = 0; i < teamCount; i++) {
                    Locator team     = teamRows.get(i);
                    Locator statsRow = statsRows.get(i);

                    int teamRank = Integer.parseInt(
                            team.locator("[class*='_rank_']").textContent().trim());
                    String teamName = team.locator("span[data-long-name]").textContent().trim();

                    Locator logo = team.locator("[class*='_thumb_logo_']");
                    logo.scrollIntoViewIfNeeded();

                    String imageUrl = extractImageUrl(page, logo);

                    List<Locator> stats = statsRow.locator("[class*='_data_']").all();
                    int winCnt      = Integer.parseInt(stats.get(0).textContent().trim());
                    int loseCnt     = Integer.parseInt(stats.get(1).textContent().trim());
                    // 득실차는 음수 또는 양수 부호가 붙을 수 있음
                    int pointDiff   = Integer.parseInt(stats.get(2).textContent().trim().replace("+", ""));
                    double winRate  = Double.parseDouble(stats.get(3).textContent().trim());

                    Ranking ranking = Ranking.builder()
                            .matchGroup(matchGroupName)
                            .teamRank(teamRank)
                            .img(imageUrl)
                            .teamName(teamName)
                            .winCnt(winCnt)
                            .loseCnt(loseCnt)
                            .winRate(winRate)
                            .pointDiff(pointDiff)
                            .build();

                    rankingUpdateService.updateRankingIfChanged(ranking);
                }
            }
        }
    }

    // background-image CSS 또는 img 태그에서 이미지 URL 추출 (최대 8초 대기)
    private String extractImageUrl(Page page, Locator logo) {
        try {
            page.waitForFunction(
                    "([el]) => { const bg = window.getComputedStyle(el).backgroundImage;" +
                    " return (bg && bg.includes('url(')) || !!el.querySelector('img'); }",
                    List.of(logo.elementHandle()),
                    new Page.WaitForFunctionOptions().setTimeout(8_000)
            );

            String bg = (String) logo.evaluate(
                    "el => window.getComputedStyle(el).backgroundImage");
            if (bg != null && bg.contains("url(")) {
                return bg.replaceAll(".*url\\((?:\"|')?(.*?)(?:\"|')?\\).*", "$1");
            }
            if (logo.locator("img").count() > 0) {
                return logo.locator("img").getAttribute("src");
            }
        } catch (TimeoutError e) {
            log.warn("팀 로고 이미지 로딩 타임아웃");
        }
        return "";
    }
}
```

- [ ] **Step 2: 컴파일 확인**

```bash
./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/example/crawling/crawling/strategy/ranking/PlaywrightRankingFetcher.java
git commit -m "feat PlaywrightRankingFetcher 구현"
```

---

### Task 8: PlaywrightScheduleFetcher 구현

`MatchScheduleService.crawlingSchedules()`의 Playwright 버전.

**Files:**
- Create: `src/main/java/com/example/crawling/crawling/strategy/schedule/PlaywrightScheduleFetcher.java`

- [ ] **Step 1: PlaywrightScheduleFetcher 생성**

```java
package com.example.crawling.crawling.strategy.schedule;

import com.example.crawling.crawling.strategy.ScheduleFetchStrategy;
import com.example.crawling.global.config.PlaywrightBrowserFactory;
import com.example.crawling.schedule.application.MatchScheduleUpdateService;
import com.example.crawling.schedule.domain.MatchSchedule;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class PlaywrightScheduleFetcher implements ScheduleFetchStrategy {

    private final MatchScheduleUpdateService matchScheduleUpdateService;
    private static final String SCHEDULE_URL =
            "https://game.naver.com/esports/League_of_Legends/schedule/lck";
    // RankingService와 동일하게 LCK 비시즌이므로 임의 지정
    private static final int CURRENT_MONTH = 9;

    @Override
    public void fetchAndSave() {
        try (Playwright pw = Playwright.create();
             Browser browser = PlaywrightBrowserFactory.createBrowser(pw)) {

            Page page = browser.newPage();
            page.navigate(SCHEDULE_URL);
            page.waitForSelector("a[href*='schedule/lck?date=']");

            Map<Integer, String> monthInfos = extractActiveMonths(page);

            for (Map.Entry<Integer, String> entry : monthInfos.entrySet()) {
                if (entry.getKey() != CURRENT_MONTH) continue;

                log.info("{}월의 일정 정보를 크롤링합니다.", entry.getKey());
                page.navigate(entry.getValue());
                page.waitForSelector("[data-time-stamp]");
                scrapeMonthlySchedule(page, entry.getKey());
                log.info("{}월의 일정 크롤링이 완료되었습니다.", entry.getKey());
            }
        }
    }

    private Map<Integer, String> extractActiveMonths(Page page) {
        Map<Integer, String> result = new HashMap<>();
        List<Locator> monthLinks = page.locator("a[href*='schedule/lck?date=']").all();
        for (Locator link : monthLinks) {
            String href      = link.getAttribute("href");
            String monthText = link.textContent().trim();
            int month        = Integer.parseInt(monthText.replace("월", "").trim());
            result.put(month, href);
        }
        return result;
    }

    private void scrapeMonthlySchedule(Page page, int currentMonth) {
        List<Locator> matchGroups = page.locator("[data-time-stamp]").all();
        int year = LocalDate.now(ZoneId.of("Asia/Seoul")).getYear();
        DateTimeFormatter monthDayFormatter = DateTimeFormatter.ofPattern("MM월 dd일");

        for (Locator group : matchGroups) {
            // "오늘09월 15일 (화)" → "09월 15일"
            String dateCleaned = group.locator("[class*='_date_']").textContent()
                    .replace("오늘", "")
                    .replaceAll("\\s*\\(.*?\\)", "")
                    .trim();
            LocalDate matchDate = MonthDay.parse(dateCleaned, monthDayFormatter).atYear(year);

            List<Locator> matches = group.locator("li[data-tournament]").all();
            for (Locator match : matches) {
                String startTime   = match.locator("[class*='_time_']").textContent();
                String matchStatus = match.locator("[data-type]").textContent();
                String stageType   = match.locator("[class*='_title_']").textContent();
                String team1       = match.locator("[class*='_home_'] [class*='_name_']").textContent();
                String team2       = match.locator("[class*='_away_'] [class*='_name_']").textContent();

                String teamScore1 = "none";
                String teamScore2 = "none";
                if (!matchStatus.equals("예정")) {
                    List<Locator> scoreEls = match.locator("[data-lose-score]").all();
                    if (scoreEls.size() >= 2) {
                        teamScore1 = scoreEls.get(0).textContent();
                        teamScore2 = scoreEls.get(1).textContent();
                    }
                }

                String teamImg1 = null;
                String teamImg2 = null;
                try { teamImg1 = match.locator("[class*='_home_'] img").getAttribute("src"); }
                catch (Exception ignored) {}
                try { teamImg2 = match.locator("[class*='_away_'] img").getAttribute("src"); }
                catch (Exception ignored) {}

                MatchSchedule schedule = new MatchSchedule(
                        currentMonth, matchDate, startTime, team1, team2,
                        matchStatus, stageType, teamScore1, teamScore2, teamImg1, teamImg2
                );
                matchScheduleUpdateService.updateMatchScheduleIfChanged(schedule);
            }
        }
    }
}
```

- [ ] **Step 2: 전체 컴파일 + 기존 테스트 통과 확인**

`StrategyCrawlingService`가 `playwrightRankingFetcher`, `playwrightScheduleFetcher` 빈을 찾을 수 있게 됐으므로 애플리케이션 컨텍스트가 정상 로드되는지 확인한다.

```bash
./gradlew compileJava compileTestJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/example/crawling/crawling/strategy/schedule/PlaywrightScheduleFetcher.java
git commit -m "feat PlaywrightScheduleFetcher 구현"
```

---

### Task 9: 3-way 비교 테스트 작성

Selenium / Playwright / API 세 전략을 5회씩 측정해 평균 시간을 출력한다.
각 회차마다 ranking + schedule을 `CompletableFuture.allOf`로 비동기 실행해 실제 운영 조건과 동일하게 측정한다.

**Files:**
- Create: `src/test/java/com/example/crawling/CrawlingStrategyTimeMeasure.java`

- [ ] **Step 1: CrawlingStrategyTimeMeasure 생성**

```java
package com.example.crawling;

import com.example.crawling.crawling.CrawlingScheduler;
import com.example.crawling.crawling.strategy.RankingFetchStrategy;
import com.example.crawling.crawling.strategy.ScheduleFetchStrategy;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@SpringBootTest
public class CrawlingStrategyTimeMeasure {

    // @Scheduled가 테스트 중 스레드 풀을 선점하지 못하도록 모킹
    @MockBean
    private CrawlingScheduler crawlingScheduler;

    @Autowired @Qualifier("seleniumRankingFetcher")
    private RankingFetchStrategy seleniumRanking;

    @Autowired @Qualifier("playwrightRankingFetcher")
    private RankingFetchStrategy playwrightRanking;

    @Autowired @Qualifier("apiRankingFetcher")
    private RankingFetchStrategy apiRanking;

    @Autowired @Qualifier("seleniumScheduleFetcher")
    private ScheduleFetchStrategy seleniumSchedule;

    @Autowired @Qualifier("playwrightScheduleFetcher")
    private ScheduleFetchStrategy playwrightSchedule;

    @Autowired @Qualifier("apiScheduleFetcher")
    private ScheduleFetchStrategy apiSchedule;

    @Autowired @Qualifier("crawlingExecutor")
    private Executor crawlingExecutor;

    private static final int REPEAT = 5;

    @Test
    @DisplayName("Selenium vs Playwright vs API 비동기 크롤링 속도 비교 (5회 평균)")
    void compareStrategies() {
        log.info("=== 워밍업 시작 (Selenium, 측정 제외) ===");
        measureAsync(seleniumRanking, seleniumSchedule);
        log.info("=== 워밍업 완료 ===\n");

        long[] seleniumMs   = new long[REPEAT];
        long[] playwrightMs = new long[REPEAT];
        long[] apiMs        = new long[REPEAT];

        log.info("=== Selenium 측정 ({} 회) ===", REPEAT);
        for (int i = 0; i < REPEAT; i++) {
            seleniumMs[i] = measureAsync(seleniumRanking, seleniumSchedule);
            log.info("[Selenium   {}회] {}ms", i + 1, seleniumMs[i]);
        }

        log.info("=== Playwright 측정 ({} 회) ===", REPEAT);
        for (int i = 0; i < REPEAT; i++) {
            playwrightMs[i] = measureAsync(playwrightRanking, playwrightSchedule);
            log.info("[Playwright {}회] {}ms", i + 1, playwrightMs[i]);
        }

        log.info("=== API 측정 ({} 회) ===", REPEAT);
        for (int i = 0; i < REPEAT; i++) {
            apiMs[i] = measureAsync(apiRanking, apiSchedule);
            log.info("[API        {}회] {}ms", i + 1, apiMs[i]);
        }

        long seleniumAvg   = Arrays.stream(seleniumMs).sum() / REPEAT;
        long playwrightAvg = Arrays.stream(playwrightMs).sum() / REPEAT;
        long apiAvg        = Arrays.stream(apiMs).sum() / REPEAT;

        log.info("=================================================");
        log.info("Selenium   평균: {}ms", seleniumAvg);
        log.info("Playwright 평균: {}ms", playwrightAvg);
        log.info("API        평균: {}ms", apiAvg);
        log.info("=================================================");
    }

    private long measureAsync(RankingFetchStrategy ranking, ScheduleFetchStrategy schedule) {
        long start = System.nanoTime();
        CompletableFuture.allOf(
                CompletableFuture.runAsync(ranking::fetchAndSave, crawlingExecutor),
                CompletableFuture.runAsync(schedule::fetchAndSave, crawlingExecutor)
        ).join();
        return (System.nanoTime() - start) / 1_000_000;
    }
}
```

- [ ] **Step 2: 테스트 실행**

```bash
./gradlew test --tests "com.example.crawling.CrawlingStrategyTimeMeasure" 2>&1 | \
  grep -E "(Selenium|Playwright|API|===|평균|PASSED|FAILED)"
```

Expected: 세 전략 모두 정상 완료 후 평균 시간 출력

- [ ] **Step 3: 기존 테스트 무영향 확인**

```bash
./gradlew test --tests "com.example.crawling.CrawlingTimeMeasure" 2>&1 | \
  grep -E "(비동기|동기|평균|PASSED|FAILED)"
```

Expected: `PASSED` 및 기존과 유사한 수치 출력

- [ ] **Step 4: 커밋**

```bash
git add src/test/java/com/example/crawling/CrawlingStrategyTimeMeasure.java
git commit -m "test Selenium vs Playwright vs API 3-way 성능 비교 테스트 추가"
```

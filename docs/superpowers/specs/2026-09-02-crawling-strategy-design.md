# 크롤링 전략 패턴 도입 설계

## 배경

기존 Selenium 기반 크롤링 코드(`RankingService`, `MatchScheduleService`)가 동작 중인 상태에서
Playwright와 API 방식을 나란히 비교하고, 향후 전략을 교체 가능하게 만들기 위해 전략 패턴을 도입한다.
기존 Selenium 코드는 수정하지 않는다.

## 목표

1. Selenium / Playwright / API 세 가지 구현을 인터페이스로 추상화한다.
2. `StrategyCrawlingService`로 전략을 주입받아 비동기 실행한다.
3. 세 전략의 성능을 `CrawlingStrategyTimeMeasure` 테스트로 측정·비교한다.

---

## 아키텍처

### 신규 파일 (기존 코드 무수정)

```
crawling/strategy/
├── RankingFetchStrategy.java
├── ScheduleFetchStrategy.java
├── ranking/
│   ├── SeleniumRankingFetcher.java    ← RankingService 위임
│   ├── PlaywrightRankingFetcher.java  ← 신규 구현
│   └── ApiRankingFetcher.java         ← RankingApiService 위임
└── schedule/
    ├── SeleniumScheduleFetcher.java   ← MatchScheduleService 위임
    ├── PlaywrightScheduleFetcher.java ← 신규 구현
    └── ApiScheduleFetcher.java        ← MatchScheduleApiService 위임

crawling/
└── StrategyCrawlingService.java

global/config/
└── PlaywrightBrowserFactory.java

test/
└── CrawlingStrategyTimeMeasure.java
```

### 실행 흐름

```
StrategyCrawlingService.crawlAsync()
    ├── CompletableFuture.runAsync(rankingStrategy::fetchAndSave, crawlingExecutor)
    └── CompletableFuture.runAsync(scheduleStrategy::fetchAndSave, crawlingExecutor)
                ↓ CompletableFuture.allOf().join()
```

---

## 인터페이스

```java
public interface RankingFetchStrategy {
    void fetchAndSave();
}

public interface ScheduleFetchStrategy {
    void fetchAndSave();
}
```

---

## Fetcher 구현

### Selenium (어댑터)

기존 서비스를 위임하며 WebDriver 생성/종료를 감싼다.

```java
@Component
public class SeleniumRankingFetcher implements RankingFetchStrategy {
    private final RankingService rankingService;

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

`SeleniumScheduleFetcher`도 동일한 패턴으로 `MatchScheduleService` 위임.

### Playwright (신규)

`PlaywrightBrowserFactory`로 브라우저를 생성하고 try-with-resources로 자원을 관리한다.
크롤링 로직은 기존 Selenium 코드와 동일한 셀렉터를 사용하되 Playwright API로 재작성한다.
Auto-wait를 활용해 `WebDriverWait` 없이 구현한다.

```java
@Component
public class PlaywrightRankingFetcher implements RankingFetchStrategy {
    public void fetchAndSave() {
        try (Playwright pw = Playwright.create();
             Browser browser = PlaywrightBrowserFactory.createBrowser(pw);
             Page page = browser.newPage()) {
            // 크롤링 로직
        }
    }
}
```

### API (어댑터)

기존 서비스를 단순 위임한다.

```java
@Component
public class ApiRankingFetcher implements RankingFetchStrategy {
    private final RankingApiService rankingApiService;

    public void fetchAndSave() {
        rankingApiService.fetchAndSaveRanking();
    }
}
```

---

## StrategyCrawlingService

같은 인터페이스 구현체가 3개이므로 `@Qualifier`로 주입 대상을 명시한다.
`StrategyCrawlingService` 자체는 특정 전략에 종속되지 않으며, 주입 시 `@Qualifier`로 원하는 구현체를 선택한다.

```java
@Service
public class StrategyCrawlingService {
    private final RankingFetchStrategy rankingStrategy;
    private final ScheduleFetchStrategy scheduleStrategy;
    private final Executor crawlingExecutor;

    public CompletableFuture<Void> crawlAsync() {
        return CompletableFuture.allOf(
            CompletableFuture.runAsync(rankingStrategy::fetchAndSave, crawlingExecutor),
            CompletableFuture.runAsync(scheduleStrategy::fetchAndSave, crawlingExecutor)
        );
    }
}
```

---

## PlaywrightBrowserFactory

`WebDriverFactory`와 동일한 역할. headless 모드, viewport, user-agent를 설정한다.

```java
public class PlaywrightBrowserFactory {
    public static Browser createBrowser(Playwright playwright) {
        return playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(true)
        );
    }
}
```

---

## 비교 테스트

`CrawlingStrategyTimeMeasure` 테스트에서 세 전략을 순서대로 5회씩 측정한다.
각 회차마다 ranking + schedule을 `allOf`로 비동기 실행해 실제 운영 조건과 동일하게 측정한다.

출력 예시:
```
[Selenium  1회] 2874ms
...
[Playwright 1회] xxxx ms
...
[API        1회] xxxx ms
...
==============================================
Selenium   평균: 2767ms
Playwright 평균: ????ms
API        평균: ????ms
==============================================
```

---

## 의존성 추가

`build.gradle`에 Playwright Java 라이브러리를 추가한다.

```groovy
implementation 'com.microsoft.playwright:playwright:1.44.0'
```

---

## 검증 방법

1. `CrawlingStrategyTimeMeasure` 테스트 실행 — 세 전략 모두 정상 완료 및 시간 출력 확인
2. DB에 랭킹·일정 데이터가 정상 저장되는지 확인
3. 기존 `CrawlingTimeMeasure` 테스트가 여전히 통과하는지 확인 (기존 코드 무영향 검증)

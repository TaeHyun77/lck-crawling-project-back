package com.example.crawling.schedule.application;

import com.example.crawling.global.exception.CustomException;
import com.example.crawling.global.exception.ErrorCode;
import com.example.crawling.global.infra.naver.NaverEsportsClient;
import com.example.crawling.schedule.domain.MatchSchedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 네이버 e스포츠 API를 통해 LCK 경기 일정을 가져와 DB에 저장한다.
 * Selenium 기반의 MatchScheduleService와 동일한 역할을 수행한다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MatchScheduleApiService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final MatchScheduleUpdateService matchScheduleUpdateService;
    private final NaverEsportsClient naverEsportsClient;

    public void fetchAndSaveSchedule() {
        LocalDate today = LocalDate.now(KST);
        String yearMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        log.info("LCK 일정 API 호출: topLeagueId=lck, month={}", yearMonth);

        try {
            NaverEsportsClient.ScheduleContent content = naverEsportsClient.getMonthlySchedule("lck", yearMonth);
            List<NaverEsportsClient.MatchItem> matches = content.matches();

            if (matches == null || matches.isEmpty()) {
                log.info("{}월 일정 데이터 없음", today.getMonthValue());
                return;
            }

            for (NaverEsportsClient.MatchItem match : matches) {
                LocalDateTime matchDateTime = Instant.ofEpochMilli(match.startDate())
                        .atZone(KST)
                        .toLocalDateTime();

                LocalDate matchDate = matchDateTime.toLocalDate();
                String startTime = matchDateTime.format(TIME_FORMATTER);
                int month = matchDate.getMonthValue();

                String matchStatus = toKoreanStatus(match.matchStatus());
                String stageType = match.title() != null ? match.title() : "";

                NaverEsportsClient.TeamInfo home = match.homeTeam();
                NaverEsportsClient.TeamInfo away = match.awayTeam();

                String team1 = home != null ? home.name() : "";
                String team2 = away != null ? away.name() : "";
                String teamImg1 = home != null ? home.imageUrl() : null;
                String teamImg2 = away != null ? away.imageUrl() : null;

                String teamScore1 = toScoreString(match.homeScore(), matchStatus);
                String teamScore2 = toScoreString(match.awayScore(), matchStatus);

                MatchSchedule schedule = MatchSchedule.builder()
                        .month(month)
                        .matchDate(matchDate)
                        .startTime(startTime)
                        .team1(team1)
                        .team2(team2)
                        .matchStatus(matchStatus)
                        .stageType(stageType)
                        .teamScore1(teamScore1)
                        .teamScore2(teamScore2)
                        .teamImg1(teamImg1)
                        .teamImg2(teamImg2)
                        .build();

                matchScheduleUpdateService.updateMatchScheduleIfChanged(schedule);
            }
        } catch (Exception e) {
            log.error("LCK 일정 API 호출 실패: {}", e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_CRAWLING_LCK_DATA);
        }
    }

    private String toKoreanStatus(String apiStatus) {
        if (apiStatus == null) return "예정";
        return switch (apiStatus) {
            case "SCHEDULED", "BEFORE" -> "예정";
            case "PLAYING" -> "진행중";
            case "FINISHED" -> "종료";
            default -> apiStatus;
        };
    }

    // 예정 경기에는 스코어가 없으므로 "none" 처리
    private String toScoreString(Integer score, String koreanStatus) {
        if ("예정".equals(koreanStatus) || score == null) return "none";
        return String.valueOf(score);
    }
}

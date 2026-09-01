package com.example.crawling.global.infra.naver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * 네이버 e스포츠 내부 API 클라이언트.
 * game.naver.com(React SPA)이 브라우저에서 호출하는 JSON API를 직접 호출한다.
 */
@Component
public class NaverEsportsClient {

    private static final String ORIGIN = "https://game.naver.com";
    private final RestClient rankingClient;
    private final RestClient scheduleClient;

    public NaverEsportsClient() {
        this.rankingClient = RestClient.builder()
                .baseUrl("https://esports-api.game.naver.com/service/v1")
                .defaultHeader("Origin", ORIGIN)
                .defaultHeader("Referer", ORIGIN)
                .build();
        this.scheduleClient = RestClient.builder()
                .baseUrl("https://esports-api.game.naver.com/service/v2")
                .defaultHeader("Origin", ORIGIN)
                .defaultHeader("Referer", ORIGIN)
                .build();
    }

    public List<RankingItem> getRankings(String leagueId) {
        RankingApiResponse response = rankingClient.get()
                .uri("/ranking/{leagueId}/team", leagueId)
                .retrieve()
                .body(RankingApiResponse.class);
        return response.content();
    }

    public ScheduleContent getMonthlySchedule(String topLeagueId, String yearMonth) {
        ScheduleApiResponse response = scheduleClient.get()
                .uri(b -> b.path("/schedule/month")
                        .queryParam("topLeagueId", topLeagueId)
                        .queryParam("month", yearMonth)
                        .queryParam("relay", false)
                        .build())
                .retrieve()
                .body(ScheduleApiResponse.class);
        return response.content();
    }

    // --- 응답 DTO ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RankingApiResponse(int code, String message, List<RankingItem> content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ScheduleApiResponse(int code, String message, ScheduleContent content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RankingItem(
            String groupName,
            int rank,
            int wins,
            int loses,
            int score,
            double winRate,
            TeamInfo team
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamInfo(
            String name,
            String nameEng,
            String imageUrl
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ScheduleContent(
            List<TeamInfo> teams,
            List<MatchItem> matches
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MatchItem(
            long startDate,
            String title,
            Integer homeScore,
            Integer awayScore,
            String matchStatus,
            TeamInfo homeTeam,
            TeamInfo awayTeam
    ) {}
}

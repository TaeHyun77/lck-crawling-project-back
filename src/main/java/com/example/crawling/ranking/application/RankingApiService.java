package com.example.crawling.ranking.application;

import com.example.crawling.global.exception.CustomException;
import com.example.crawling.global.exception.ErrorCode;
import com.example.crawling.global.infra.naver.NaverEsportsClient;
import com.example.crawling.ranking.domain.Ranking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 네이버 e스포츠 API를 통해 LCK 팀 순위를 가져와 DB에 저장한다.
 * Selenium 기반의 RankingService와 동일한 역할을 수행한다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RankingApiService {

    private final RankingUpdateService rankingUpdateService;
    private final NaverEsportsClient naverEsportsClient;

    public void fetchAndSaveRanking() {
        String leagueId = "lck_" + LocalDate.now().getYear();
        log.info("LCK 순위 API 호출: leagueId={}", leagueId);

        try {
            List<NaverEsportsClient.RankingItem> items = naverEsportsClient.getRankings(leagueId);

            for (NaverEsportsClient.RankingItem item : items) {
                NaverEsportsClient.TeamInfo team = item.team();
                if (team == null) continue;

                Ranking ranking = Ranking.builder()
                        .matchGroup(item.groupName())
                        .teamRank(item.rank())
                        .img(team.imageUrl())
                        .teamName(team.name())
                        .winCnt(item.wins())
                        .loseCnt(item.loses())
                        .winRate(item.winRate())
                        .pointDiff(item.score())
                        .build();

                rankingUpdateService.updateRankingIfChanged(ranking);
            }
        } catch (Exception e) {
            log.error("LCK 순위 API 호출 실패: {}", e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_CRAWLING_RANKING_DATA);
        }
    }
}

package com.example.crawling.ranking.application;

import com.example.crawling.ranking.domain.Ranking;
import com.example.crawling.ranking.domain.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankingUpdateService {
    private final RankingRepository rankingRepository;

    @Transactional
    public void updateRankingIfChanged(Ranking crawledRanking) {
        Ranking existingRanking = rankingRepository.findByTeamName(crawledRanking.getTeamName())
                .orElseGet(() -> rankingRepository.save(crawledRanking));

        if (isRankingChanged(existingRanking, crawledRanking)) {
            existingRanking.updateRanking(
                    crawledRanking.getMatchGroup(), crawledRanking.getTeamRank(),
                    crawledRanking.getWinCnt(), crawledRanking.getLoseCnt(),
                    crawledRanking.getWinRate(), crawledRanking.getPointDiff(),
                    crawledRanking.getImg()
            );
        }
    }

    private boolean isRankingChanged(Ranking existing, Ranking newRanking) {
        return !existing.getMatchGroup().equals(newRanking.getMatchGroup()) ||
                existing.getTeamRank() != newRanking.getTeamRank() ||
                existing.getWinCnt() != newRanking.getWinCnt() ||
                existing.getLoseCnt() != newRanking.getLoseCnt() ||
                existing.getWinRate() != newRanking.getWinRate() ||
                existing.getPointDiff() != newRanking.getPointDiff() ||
                !existing.getImg().equals(newRanking.getImg());
    }
}

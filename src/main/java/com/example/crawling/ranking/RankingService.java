package com.example.crawling.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RankingService {

    private final RankingRepository rankingRepository;

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
                        .build())
                .collect(Collectors.toList());
    }
}

package com.example.crawling.ranking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class RankingResponseDto {

    private Long id;

    private int teamRank;

    private String img;

    private String teamName;

    private int winCnt;

    private int loseCnt;

    private double winRate;

    private int pointDiff;

    private String matchGroup;
}

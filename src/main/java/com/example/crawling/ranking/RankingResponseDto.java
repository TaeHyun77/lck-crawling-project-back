package com.example.crawling.ranking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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

    @Builder
    public RankingResponseDto(int teamRank, String img, String teamName, int winCnt, int loseCnt, double winRate, int pointDiff) {
        this.teamRank = teamRank;
        this.img = img;
        this.teamName = teamName;
        this.winCnt = winCnt;
        this.loseCnt = loseCnt;
        this.winRate = winRate;
        this.pointDiff = pointDiff;
    }
}

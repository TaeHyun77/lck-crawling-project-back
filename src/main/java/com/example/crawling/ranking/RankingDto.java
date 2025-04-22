package com.example.crawling.ranking;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

@NoArgsConstructor
@Getter
public class RankingDto {

    private int teamRank;

    private String img;

    private String teamName;

    private int winCnt;

    private int loseCnt;

    private double winRate;

    private int pointDiff;

    @Builder
    public RankingDto(int teamRank, String img, String teamName, int winCnt, int loseCnt, double winRate, int pointDiff) {
        this.teamRank = teamRank;
        this.img = img;
        this.teamName = teamName;
        this.winCnt = winCnt;
        this.loseCnt = loseCnt;
        this.winRate = winRate;
        this.pointDiff = pointDiff;
    }

    public Ranking toRanking() {
        return Ranking.builder()
                .teamRank(teamRank)
                .img(img)
                .teamName(teamName)
                .winCnt(winCnt)
                .loseCnt(loseCnt)
                .winRate(winRate)
                .pointDiff(pointDiff)
                .build();
    }
}

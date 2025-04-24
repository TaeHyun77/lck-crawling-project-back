package com.example.crawling.ranking;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class RankingRequestDto {

    private int teamRank;

    private String img;

    private String teamName;

    private int winCnt;

    private int loseCnt;

    private double winRate;

    private int pointDiff;

    @Builder
    public RankingRequestDto(int teamRank, String img, String teamName, int winCnt, int loseCnt, double winRate, int pointDiff) {
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

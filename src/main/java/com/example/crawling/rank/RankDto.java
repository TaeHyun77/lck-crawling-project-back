package com.example.crawling.rank;

import lombok.Builder;

public class RankDto {

    private String teamName;

    private String teamImgUrl;

    private String winCnt;

    private String loseCnt;

    private String pointDiff;

    private String winRate;

    private String kda;

    private String killCnt;

    private String deathCnt;

    private String assistCnt;

    @Builder
    public RankDto(String teamName, String teamImgUrl, String winCnt, String loseCnt, String pointDiff, String winRate, String kda, String killCnt, String deathCnt, String assistCnt) {
        this.teamName = teamName;
        this.teamImgUrl = teamImgUrl;
        this.winCnt = winCnt;
        this.loseCnt = loseCnt;
        this.pointDiff = pointDiff;
        this.winRate = winRate;
        this.kda = kda;
        this.killCnt = killCnt;
        this.deathCnt = deathCnt;
        this.assistCnt = assistCnt;
    }

    public Rank toRankEntity() {
        return Rank.builder()
                .teamName(teamName)
                .teamImgUrl(teamImgUrl)
                .winCnt(winCnt)
                .loseCnt(loseCnt)
                .pointDiff(pointDiff)
                .winRate(winRate)
                .kda(kda)
                .killCnt(killCnt)
                .deathCnt(deathCnt)
                .assistCnt(assistCnt)
                .build();
    }

}

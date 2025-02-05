package com.example.crawling.crawling;

public record RankingData(
        String teamName,
        String winCnt,
        String loseCnt,
        String pointDiff,
        String winRate,
        String kda,
        String killCnt,
        String deathCnt,
        String assistCnt,
        String imageUrl
) {}

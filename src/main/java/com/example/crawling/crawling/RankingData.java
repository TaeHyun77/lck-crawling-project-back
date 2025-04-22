package com.example.crawling.crawling;

public record RankingData(
        int teamRank,
        String img,
        String teamName,
        int winCnt,
        int loseCnt,
        double winRate,
        int pointDiff
){}

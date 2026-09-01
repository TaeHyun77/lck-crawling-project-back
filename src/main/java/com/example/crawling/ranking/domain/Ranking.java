package com.example.crawling.ranking.domain;

import com.example.crawling.global.BaseTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ranking")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Ranking extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String matchGroup; // 그룹

    private int teamRank; // 순위

    private String img; // 팀 로고 이미지

    private String teamName; // 팀명

    private int winCnt; // 승리 수

    private int loseCnt; // 패배 수

    private double winRate; // 승률

    private int pointDiff; // 득실 차

    @Builder
    public Ranking(String matchGroup, int teamRank, String img, String teamName, int winCnt, int loseCnt, double winRate, int pointDiff) {
        this.matchGroup = matchGroup;
        this.teamRank = teamRank;
        this.img = img;
        this.teamName = teamName;
        this.winCnt = winCnt;
        this.loseCnt = loseCnt;
        this.winRate = winRate;
        this.pointDiff = pointDiff;
    }

    public void updateRanking(String matchGroup, int teamRank, int winCnt, int loseCnt, double winRate, int pointDiff, String img) {
        this.matchGroup = matchGroup;
        this.teamRank = teamRank;
        this.winCnt = winCnt;
        this.loseCnt = loseCnt;
        this.winRate = winRate;
        this.pointDiff = pointDiff;
        this.img = img;
    }
}

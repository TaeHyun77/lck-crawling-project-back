package com.example.crawling.ranking;

import com.example.crawling.BaseTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "ranking")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Ranking extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String matchGroup;

    private int teamRank;

    private String img;

    private String teamName;

    private int winCnt;

    private int loseCnt;

    private double winRate;

    private int pointDiff;

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

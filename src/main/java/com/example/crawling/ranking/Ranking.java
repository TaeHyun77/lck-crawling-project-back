package com.example.crawling.ranking;

import com.example.crawling.config.BaseTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Ranking extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int teamRank;

    private String img;

    private String teamName;

    private int winCnt;

    private int loseCnt;

    private double winRate;

    private int pointDiff;

    @Builder
    public Ranking(int teamRank, String img, String teamName, int winCnt, int loseCnt, double winRate, int pointDiff) {
        this.teamRank = teamRank;
        this.img = img;
        this.teamName = teamName;
        this.winCnt = winCnt;
        this.loseCnt = loseCnt;
        this.winRate = winRate;
        this.pointDiff = pointDiff;
    }

    public void updateRanking(int teamRank, int winCnt, int loseCnt, double winRate, int pointDiff) {
        this.teamRank = teamRank;
        this.winCnt = winCnt;
        this.loseCnt = loseCnt;
        this.winRate = winRate;
        this.pointDiff = pointDiff;
    }
}

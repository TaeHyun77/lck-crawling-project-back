package com.example.crawling.rank;

import com.example.crawling.config.BaseTime;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Table(name="ranking")
@Entity
public class Rank extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    public Rank(String teamName, String teamImgUrl, String winCnt, String loseCnt, String pointDiff, String winRate, String kda, String killCnt, String deathCnt, String assistCnt) {
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
}

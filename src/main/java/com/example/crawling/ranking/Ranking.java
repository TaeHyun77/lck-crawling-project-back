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

    private String stage;

    private int teamRank;

    private String img;

    private String teamName;

    private String record;

    private String recordSet;

    @Builder
    public Ranking(String stage, int teamRank, String img, String teamName, String record, String recordSet) {
        this.stage = stage;
        this.teamRank = teamRank;
        this.img = img;
        this.teamName = teamName;
        this.record = record;
        this.recordSet = recordSet;
    }

    public void updateRanking(int teamRank, String record, String recordSet) {
        this.teamRank = teamRank;
        this.record = record;
        this.recordSet = recordSet;
    }
}

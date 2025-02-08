package com.example.crawling.ranking;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

@NoArgsConstructor
@Getter
public class RankingDto {

    private String stage;

    private int teamRank;

    private String img;

    private String teamName;

    private String record;

    private String recordSet;

    @Builder
    public RankingDto(String stage, int teamRank, String img, String teamName, String record, String recordSet) {
        this.stage = stage;
        this.teamRank = teamRank;
        this.img = img;
        this.teamName = teamName;
        this.record = record;
        this.recordSet = recordSet;
    }

    public Ranking toRanking() {
        return Ranking.builder()
                .stage(stage)
                .teamRank(teamRank)
                .img(img)
                .teamName(teamName)
                .record(record)
                .recordSet(recordSet)
                .build();
    }
}

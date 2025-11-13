package com.example.crawling.schedule;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MatchScheduleRequestDto {

    private int month;

    private String matchDate;

    private String startTime;

    private String team1;

    private String team2;

    private String matchStatus;

    private String stageType;

    private String teamScore1;

    private String teamScore2;

    private String teamImg1;

    private String teamImg2;
}

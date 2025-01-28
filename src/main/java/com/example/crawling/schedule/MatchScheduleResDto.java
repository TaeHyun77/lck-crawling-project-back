package com.example.crawling.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchScheduleResDto {

    private Long id;

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

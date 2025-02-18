package com.example.crawling.schedule;

import com.example.crawling.config.BaseTime;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Table(name="match_schedule")
@Entity
public class MatchSchedule extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Builder
    public MatchSchedule(int month, String matchDate, String startTime, String team1, String team2, String matchStatus, String stageType, String teamScore1, String teamScore2, String teamImg1, String teamImg2) {
        this.month = month;
        this.matchDate = matchDate;
        this.startTime = startTime;
        this.team1 = team1;
        this.team2 = team2;
        this.matchStatus = matchStatus;
        this.stageType = stageType;
        this.teamScore1 = teamScore1;
        this.teamScore2 = teamScore2;
        this.teamImg1 = teamImg1;
        this.teamImg2 = teamImg2;
    }

    public void updateMatchSchedule(String team1, String team2, String matchStatus, String stageType, String teamScore1, String teamScore2, String teamImg1, String teamImg2) {
        this.team1 = team1;
        this.team2 = team2;
        this.matchStatus = matchStatus;
        this.stageType = stageType;
        this.teamScore1 = teamScore1;
        this.teamScore2 = teamScore2;
        this.teamImg1 = teamImg1;
        this.teamImg2 = teamImg2;
    }
}

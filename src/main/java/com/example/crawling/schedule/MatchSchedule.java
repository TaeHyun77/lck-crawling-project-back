package com.example.crawling.schedule;

import com.example.crawling.BaseTime;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name="match_schedule")
@Getter
@NoArgsConstructor
public class MatchSchedule extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int month; // 월

    private LocalDate matchDate; // 일정 날짜

    private String startTime; // 시작 시간

    private String team1;

    private String team2;

    private String matchStatus; // 경기 상태 ( 종료, 예정 .. )

    private String stageType; // 경기 종류

    private String teamScore1;

    private String teamScore2;

    private String teamImg1;

    private String teamImg2;

    @Builder
    public MatchSchedule(int month, LocalDate matchDate, String startTime, String team1, String team2, String matchStatus, String stageType, String teamScore1, String teamScore2, String teamImg1, String teamImg2) {
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

    public void updateMatchSchedule(int month, String team1, String team2, String matchStatus, String stageType, String teamScore1, String teamScore2, String teamImg1, String teamImg2) {
        this.month = month;
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

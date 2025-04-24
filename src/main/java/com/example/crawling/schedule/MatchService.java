package com.example.crawling.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MatchService {

    private final MatchScheduleRepository matchScheduleRepository;

    public List<MatchScheduleResponseDto> getAllSchedule() {
        return matchScheduleRepository.findAll().stream()
                .map(schedule -> MatchScheduleResponseDto.builder()
                        .month(schedule.getMonth())
                        .matchDate(schedule.getMatchDate())
                        .startTime(schedule.getStartTime())
                        .team1(schedule.getTeam1())
                        .team2(schedule.getTeam2())
                        .matchStatus(schedule.getMatchStatus())
                        .stageType(schedule.getStageType())
                        .teamScore1(schedule.getTeamScore1())
                        .teamScore2(schedule.getTeamScore2())
                        .teamImg1(schedule.getTeamImg1())
                        .teamImg2(schedule.getTeamImg2())
                        .build())
                .collect(Collectors.toList());
    }
}


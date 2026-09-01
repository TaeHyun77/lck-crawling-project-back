package com.example.crawling.schedule.application;

import com.example.crawling.schedule.domain.MatchSchedule;
import com.example.crawling.schedule.domain.MatchScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class MatchScheduleUpdateService {
    private final MatchScheduleRepository matchScheduleRepository;

    @Transactional
    public void updateMatchScheduleIfChanged(MatchSchedule crawlingMatchSchedule) {
        MatchSchedule dbMatchSchedule = matchScheduleRepository.findByMatchDateAndStartTime(
                crawlingMatchSchedule.getMatchDate(), crawlingMatchSchedule.getStartTime()
        ).orElseGet(() -> matchScheduleRepository.save(crawlingMatchSchedule));

        if (isMatchScheduleChanged(dbMatchSchedule, crawlingMatchSchedule)) {
            dbMatchSchedule.updateMatchSchedule(
                    crawlingMatchSchedule.getMonth(), crawlingMatchSchedule.getTeam1(), crawlingMatchSchedule.getTeam2(),
                    crawlingMatchSchedule.getMatchStatus(), crawlingMatchSchedule.getStageType(),
                    crawlingMatchSchedule.getTeamScore1(), crawlingMatchSchedule.getTeamScore2(),
                    crawlingMatchSchedule.getTeamImg1(), crawlingMatchSchedule.getTeamImg2()
            );

            matchScheduleRepository.save(dbMatchSchedule);
        }
    }

    private boolean isMatchScheduleChanged(MatchSchedule existing, MatchSchedule newSchedule) {
        return existing.getMonth() != newSchedule.getMonth() ||
                !existing.getStartTime().equals(newSchedule.getStartTime()) ||
                !existing.getMatchStatus().equals(newSchedule.getMatchStatus()) ||
                !existing.getStageType().equals(newSchedule.getStageType()) ||
                !existing.getTeamScore1().equals(newSchedule.getTeamScore1()) ||
                !existing.getTeamScore2().equals(newSchedule.getTeamScore2()) ||
                !Objects.equals(existing.getTeamImg1(), newSchedule.getTeamImg1()) ||
                !Objects.equals(existing.getTeamImg2(), newSchedule.getTeamImg2());
    }
}

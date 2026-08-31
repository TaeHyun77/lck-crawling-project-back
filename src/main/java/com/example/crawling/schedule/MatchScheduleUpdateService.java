package com.example.crawling.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class MatchScheduleUpdateService {
    private final MatchScheduleRepository matchScheduleRepository;

    // 일정 정보 갱신
    @Transactional
    public void updateMatchScheduleIfChanged(MatchSchedule crawlingMatchSchedule) {
        MatchSchedule dbMatchSchedule = matchScheduleRepository.findByMatchDateAndStartTime(
                crawlingMatchSchedule.getMatchDate(), crawlingMatchSchedule.getStartTime()
        ).orElseGet(() -> matchScheduleRepository.save(crawlingMatchSchedule));

        // 갱신 되었다면 변경
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

    // 일정 정보가 수정되었는지 여부
    private boolean isMatchScheduleChanged(MatchSchedule existing, MatchSchedule newSchedule) {
        return  existing.getMonth() != newSchedule.getMonth() ||
                !existing.getStartTime().equals(newSchedule.getStartTime()) ||
                !existing.getMatchStatus().equals(newSchedule.getMatchStatus()) ||
                !existing.getStageType().equals(newSchedule.getStageType()) ||
                !existing.getTeamScore1().equals(newSchedule.getTeamScore1()) ||
                !existing.getTeamScore2().equals(newSchedule.getTeamScore2()) ||
                !Objects.equals(existing.getTeamImg1(), newSchedule.getTeamImg1()) ||
                !Objects.equals(existing.getTeamImg2(), newSchedule.getTeamImg2());
    }
}

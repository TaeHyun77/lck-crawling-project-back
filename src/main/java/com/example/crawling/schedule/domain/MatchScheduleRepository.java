package com.example.crawling.schedule.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MatchScheduleRepository extends JpaRepository<MatchSchedule, Long> {
    Optional<MatchSchedule> findByMatchDateAndStartTime(LocalDate matchDate, String startTime);

    List<MatchSchedule> findByTeam1InOrTeam2In(List<String> team1, List<String> team2);

    // 오늘 날짜 경기 중 시작 시간이 지났고 아직 종료되지 않은 경기가 있으면 true
    @Query("SELECT COUNT(m) > 0 FROM MatchSchedule m " +
           "WHERE m.matchDate = :today " +
           "AND m.startTime <= :currentTime " +
           "AND m.matchStatus <> '종료'")
    boolean hasMatchInProgress(@Param("today") LocalDate today, @Param("currentTime") String currentTime);
}

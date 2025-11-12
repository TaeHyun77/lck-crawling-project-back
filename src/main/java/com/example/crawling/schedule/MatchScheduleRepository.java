package com.example.crawling.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface MatchScheduleRepository extends JpaRepository<MatchSchedule, Long> {

    Optional<MatchSchedule> findByMatchDateAndStartTime(String matchDate, String startTime);

    List<MatchSchedule> findByTeam1InOrTeam2In(List<String> team1, List<String> team2);

}

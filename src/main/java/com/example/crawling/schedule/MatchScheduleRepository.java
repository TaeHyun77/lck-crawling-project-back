package com.example.crawling.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface MatchScheduleRepository extends JpaRepository<MatchSchedule, Long> {

    boolean existsByMonth(int month);

    Optional<MatchSchedule> findByMatchDateAndTeam1AndTeam2(String matchDate, String team1, String team2);
}

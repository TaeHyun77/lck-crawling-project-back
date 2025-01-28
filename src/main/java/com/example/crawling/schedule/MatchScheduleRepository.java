package com.example.crawling.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MatchScheduleRepository extends JpaRepository<MatchSchedule, Long> {

    @Transactional
    void deleteByMonth(int month);

    @Modifying
    @Query(value = "ALTER TABLE match_schedule AUTO_INCREMENT = 1", nativeQuery = true)
    @Transactional
    void resetAutoIncrement();

}

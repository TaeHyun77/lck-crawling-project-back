package com.example.crawling.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface MatchScheduleRepository extends JpaRepository<MatchSchedule, Long> {

    @Transactional
    void deleteByMonth(int month);

}

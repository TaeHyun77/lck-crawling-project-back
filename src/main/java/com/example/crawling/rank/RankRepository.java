package com.example.crawling.rank;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface RankRepository extends JpaRepository<Rank, Long> {

    @Modifying
    @Query(value = "ALTER TABLE ranking AUTO_INCREMENT = 1", nativeQuery = true)
    @Transactional
    void resetAutoIncrement();

}

package com.example.crawling.refresh_retoken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshRepository extends JpaRepository<Refresh, Long> {
    Boolean existsByRefresh(String refresh);

    @Transactional(isolation = Isolation.SERIALIZABLE)
    void deleteByRefresh(String refresh);
}

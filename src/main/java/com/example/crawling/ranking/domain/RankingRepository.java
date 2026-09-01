package com.example.crawling.ranking.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RankingRepository extends JpaRepository<Ranking, Long> {
    Optional<Ranking> findByTeamName(String teamName);

    // 선호팀 이미지 조회 시 사용
    List<Ranking> findByTeamNameIn(List<String> teamNames);
}

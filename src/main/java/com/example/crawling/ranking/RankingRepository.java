package com.example.crawling.ranking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RankingRepository extends JpaRepository<Ranking, Long> {

    Optional<Ranking> findByTeamNameAndStage(String teamName, String stage);

}

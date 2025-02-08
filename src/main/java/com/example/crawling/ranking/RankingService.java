package com.example.crawling.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RankingService {

    private final RankingRepository rankingRepository;

    public List<Ranking> getAllRanking() {
        return rankingRepository.findAll();
    }

}

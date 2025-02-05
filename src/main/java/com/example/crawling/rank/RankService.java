package com.example.crawling.rank;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RankService {

    private final RankRepository rankRepository;

    public List<Rank> getRankData1() {
        return rankRepository.findAll();
    }

}

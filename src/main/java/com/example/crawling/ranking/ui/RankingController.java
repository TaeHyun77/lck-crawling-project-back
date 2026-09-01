package com.example.crawling.ranking.ui;

import com.example.crawling.ranking.application.RankingService;
import com.example.crawling.ranking.ui.dto.RankingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class RankingController {
    private final RankingService rankingService;

    @GetMapping("/ranking")
    public List<RankingResponseDto> getRanking() {
        return rankingService.getAllRanking();
    }
}

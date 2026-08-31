package com.example.crawling.ranking;

import com.example.crawling.ranking.dto.RankingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class RankingController {
    private final RankingService rankingService;

    // 순위 데이터 조회
    @GetMapping("/ranking")
    public List<RankingResponseDto> getRanking() {
        return rankingService.getAllRanking();
    }
}

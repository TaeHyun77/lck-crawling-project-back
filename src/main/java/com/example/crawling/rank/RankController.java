package com.example.crawling.rank;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class RankController {

    private final RankService rankService;

    @GetMapping("/ranking")
    public List<Rank> getRankData() {
        return rankService.getRankData1();
    }

}

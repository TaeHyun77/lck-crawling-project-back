package com.example.crawling.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ScheduleController {

    private final MatchService matchService;

    @GetMapping("/schedules")
    public List<MatchScheduleResponseDto> getSchedule() {
        return matchService.getAllSchedule();
    }
}

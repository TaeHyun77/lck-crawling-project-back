package com.example.crawling.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class MatchScheduleController {

    private final MatchScheduleService matchScheduleService;

    @GetMapping("/schedules")
    public List<MatchScheduleResponseDto> getSchedule() {
        return matchScheduleService.getAllSchedule();
    }
}

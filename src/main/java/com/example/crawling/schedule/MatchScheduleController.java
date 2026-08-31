package com.example.crawling.schedule;

import com.example.crawling.schedule.dto.MatchScheduleResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class MatchScheduleController {
    private final MatchScheduleService matchScheduleService;

    // 일정 데이터 조회
    @GetMapping("/schedules")
    public List<MatchScheduleResponseDto> getSchedule() {
        return matchScheduleService.getAllSchedule();
    }
}

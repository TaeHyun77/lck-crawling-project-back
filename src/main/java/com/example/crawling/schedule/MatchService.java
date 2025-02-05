package com.example.crawling.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MatchService {

    private final MatchScheduleRepository matchScheduleRepository;

    public List<MatchSchedule> getAllSchedule() {
        return matchScheduleRepository.findAll();
    }
}

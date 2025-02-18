package com.example.crawling.schedule;

import com.example.crawling.schedule.MatchSchedule;
import com.example.crawling.schedule.MatchScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MatchScheduleRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REDIS_KEY_PREFIX = "match_schedule:";
    private final MatchScheduleRepository matchScheduleRepository;

    // Redis 경기 일정 조회
    public Optional<MatchSchedule> getMatchSchedule(String matchDate, String startTime) {
        String key = REDIS_KEY_PREFIX + matchDate + ":" + startTime;

        MatchSchedule schedule = (MatchSchedule) redisTemplate.opsForValue().get(key);

        return Optional.ofNullable(schedule);
    }

    // 경기 일정 Redis에 저장 (TTL: 1시간)
    public MatchSchedule getOrUpdateMatchSchedule(String matchDate, String startTime) {

        String key = REDIS_KEY_PREFIX + matchDate + ":" + startTime;

        // redis에서 조회
        MatchSchedule redisSchedule = (MatchSchedule) redisTemplate.opsForValue().get(key);

        // redis에 있다면 값 반환
        if (redisSchedule != null) {
            return redisSchedule;
        }

        // redis에 값 존재할 시

        Optional<MatchSchedule> dbSchedule = matchScheduleRepository.findByMatchDateAndStartTime(matchDate, startTime);

        if (dbSchedule.isPresent()) {
            MatchSchedule matchSchedule = dbSchedule.get();
            log.info("DB에서 경기 일정 가져옴: " + matchDate + " " + startTime);

            // redis에 값 저장
            redisTemplate.opsForValue().set(key, matchSchedule, Duration.ofHours(1));

            return matchSchedule;
        }

        return null;
    }

}

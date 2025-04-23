package com.example.crawling.ranking;

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
public class RankingRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REDIS_KEY_PREFIX = "ranking:";
    private final RankingRepository rankingRepository;

    // Redis 순위 조회
    public Optional<Ranking> getRanking(String teamName) {
        String key = REDIS_KEY_PREFIX + teamName;

        Ranking ranking = (Ranking) redisTemplate.opsForValue().get(key);

        return Optional.ofNullable(ranking);
    }

    // 경기 일정 Redis에 저장 (TTL: 1시간)
    public Ranking getOrUpdateRanking(String teamName) {

        String key = REDIS_KEY_PREFIX + teamName;

        // redis에서 조회
        Ranking redisRanking = (Ranking) redisTemplate.opsForValue().get(key);

        // redis에 있다면 값 반환
        if (redisRanking != null) {
            return redisRanking;
        }

        // redis에 값 존재할 시

        Optional<Ranking> dbRanking = rankingRepository.findByTeamName(teamName);

        if (dbRanking.isPresent()) {
            Ranking ranking = dbRanking.get();
            log.info("DB에서 순위 데이터 가져옴: " + teamName);

            // redis에 값 저장
            redisTemplate.opsForValue().set(key, ranking, Duration.ofHours(1));

            return ranking;
        }

        return null;
    }
}

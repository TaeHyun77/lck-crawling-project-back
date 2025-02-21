package com.example.crawling.db;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.schedule.MatchSchedule;
import com.example.crawling.schedule.MatchScheduleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;


// 실제 DB와 분리된 인 메모리 H2 데이터베이스를 사용하여 JPA 기능 테스트
// user와 month 가 h2에서는 예약어이기 때문에 변경 해줘야 함
@Slf4j
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource("classpath:application-test.properties")
@DataJpaTest
public class MatchSaveTest {

    @Autowired
    private MatchScheduleRepository matchScheduleRepository;

    @PersistenceContext
    private EntityManager em;

    @DisplayName("경기 일정 저장 테스트")
    @Test
    void SaveTest(){

        MatchSchedule testMatchSchedule = MatchSchedule.builder()
                .month(2)
                .matchDate("02월 19일 (수)")
                .startTime("17:30")
                .team1("T1")
                .team2("kt 롤스터")
                .matchStatus("예정")
                .stageType("no")
                .teamScore1("none")
                .teamScore2("none")
                .teamImg1("none")
                .teamImg2("none")
                .build();

        matchScheduleRepository.save(testMatchSchedule);
        em.flush();
        em.clear();

        MatchSchedule foundMatch = matchScheduleRepository.findById(testMatchSchedule.getId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUNT_MATCHSCHEDULE));

        Assertions.assertNotNull(foundMatch, "DB에서 데이터를 찾을 수 없습니다.");
        Assertions.assertEquals(foundMatch.getMatchDate(), "02월 19일 (수)","경기 날짜가 다릅니다.");
        Assertions.assertEquals(foundMatch.getStartTime(), "17:30", "경기 시간이 다릅니다.");
        Assertions.assertEquals(foundMatch.getTeam1(), "T1", "팀1이 다릅니다.");
        Assertions.assertEquals(foundMatch.getTeam2(),"kt 롤스터", "팀2가 다릅니다.");
    }
}

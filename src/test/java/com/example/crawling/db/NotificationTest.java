/*
package com.example.crawling.db;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.fcm.FcmService;
import com.example.crawling.schedule.MatchSchedule;
import com.example.crawling.schedule.MatchScheduleRepository;
import com.example.crawling.team.Team;
import com.example.crawling.team.TeamRepository;
import com.example.crawling.team.UserTeamMap;
import com.example.crawling.team.UserTeamMapRepository;
import com.example.crawling.user.User;
import com.example.crawling.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Transactional
@SpringBootTest
public class NotificationTest {

    @Autowired
    private MatchScheduleRepository matchScheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserTeamMapRepository userTeamMapRepository;

    @Autowired
    public FcmService fcmService;

    @PersistenceContext
    private EntityManager em;

    // mysql db에 저장된 값의 알림을 받기 위해선
    // user의 fcm 토큰이 만료 되어 있지 않아야 함
    // user의 notification_permission 값이 true 이어야 함
    @DisplayName("경기 알림 테스트")
    @Test
    void SaveTest() {

        User testUser = User.builder()
                .username("테스트")
                .name("테스트")
                .email("test7777@kakao.com")
                .role("ROLE_USER")
                .notificationPermission(true)
                .build();

        // 만료되지 않은 fcm 토큰 값 작성 해야 함
        testUser.setFcmToken("e_41W2F9y6aW3Rtnh_fmR6:APA91bFs8ksu7euY-Yn9jmqalZyXesL7LT5li0p1eHudLRtrdNXQ1Tvy5_txzCOBLrmgNSO_udllXmKecIjvH8sHk7-Awkz0Vog7PYxIfuATHNBPwKOIZtI");

        // User 저장
        userRepository.save(testUser);
        em.flush();
        em.clear();

        // Team 저장 및 UserTeamMap 설정
        String[] teamNames = {"T1", "젠지"};
        List<Team> teams = new ArrayList<>();

        for (String teamName : teamNames) {
            Team team = teamRepository.findByTeamName(teamName).orElseGet(() -> {
                Team newTeam = new Team();
                newTeam.setTeamName(teamName);
                return teamRepository.save(newTeam);
            });
            teams.add(team);
        }

        for (Team team : teams) {
            UserTeamMap userTeamMap = new UserTeamMap();
            userTeamMap.setUser(testUser);
            userTeamMap.setTeam(team);
            userTeamMapRepository.save(userTeamMap);
        }

        em.flush();
        em.clear();

        User user = userRepository.findById(testUser.getId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND_USER));

        // 적절하게 경기 일정 생성
        MatchSchedule testMatchSchedule = MatchSchedule.builder()
                .month(2)
                .matchDate("02월 19일 (수)")
                .startTime("23:45")
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

        MatchSchedule matchSchedule = matchScheduleRepository.findById(testMatchSchedule.getId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUNT_MATCHSCHEDULE));

        try {
            fcmService.pushUserMatch(24); // 현재로부터 n시간 이내의 경기 탐색
            log.info("알림 성공");
        } catch (Exception e) {
            log.info("알림 실패");
        }

        Assertions.assertEquals(user.getUsername(), "테스트", "username이 다릅니다.");
        Assertions.assertEquals(user.getName(), "테스트", "name이 다릅니다.");
        Assertions.assertEquals(user.getEmail(), "test7777@kakao.com", "email 다릅니다.");

        Assertions.assertEquals(matchSchedule.getMatchDate(), "02월 19일 (수)", "matchDate가 다릅니다.");
        Assertions.assertEquals(matchSchedule.getStartTime(), "23:45", "startTime이 다릅니다.");
    }
}


 */
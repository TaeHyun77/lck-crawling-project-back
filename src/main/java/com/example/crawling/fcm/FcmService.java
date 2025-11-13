package com.example.crawling.fcm;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.schedule.MatchSchedule;
import com.example.crawling.schedule.MatchScheduleRepository;
import com.example.crawling.user.User;
import com.example.crawling.user.UserRepository;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class FcmService {

    private final UserRepository userRepository;
    private final MatchScheduleRepository matchScheduleRepository;

    // 사용자의 FCM 토큰 저장
    public void registerFcmToken(FcmRequestDto dto) {

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_USER));

        user.setFcmToken(dto.getFcmToken());
        userRepository.save(user);
    }

    // 경기 알림 발송 ( 주어진 시간 범위 내에 사용자가 선호하는 팀의 경기가 있을 경우, 알림을 발송 - 모든 사용자 대상 )
    public String pushMatchSchedule(int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime plusHour = now.plusHours(hours);

        // FCM 토큰이 유효한 사용자만 필터링
        List<User> users = getUsersWithValidFcmToken();
        List<String> results = new ArrayList<>();

        for (User user : users) {
            // 사용자의 선호하는 팀 이름 목록 추출
            List<String> teamNames = getUserTeamNames(user);

            // 시간 범위 내에 있는 경기 일정 필터링
            List<MatchSchedule> upcomingMatches = getUpcomingMatches(teamNames, now, plusHour);

            if (upcomingMatches.isEmpty()) {
                log.info("{}님이 선호하는 예정된 경기 일정이 없습니다.", user.getName());
                continue;
            }

            // 경기 정보를 문자열로 변환
            String messageBody = buildMatchDetails(upcomingMatches);

            // Firebase 메시지 객체 생성
            Message message = buildFcmMessage(user.getFcmToken(), messageBody);

            // Firebase 메시지 전송 및 결과 처리
            sendFcmMessage(user, message, results);
        }

        return String.join("\n", results);
    }

    // 모든 사용자에게 알림 발송 ( 공지 사항 발송용 ? )
    public String pushAllUser(String notice) {

        // FCM 토큰이 유효한 사용자만 필터링
        List<User> users = getUsersWithValidFcmToken();
        List<String> results = new ArrayList<>();

        for (User user : users) {
            // Firebase 메시지 객체 생성
            Message message = buildFcmMessage(user.getFcmToken(), notice);

            // Firebase 메시지 전송 및 결과 처리
            sendFcmMessage(user, message, results);
        }

        return String.join("\n", results);
    }

    /**
     * FCM 토큰이 유효한 사용자만 필터링
     */
    private List<User> getUsersWithValidFcmToken() {
        return userRepository.findAll().stream()
                .filter(user -> user.getFcmToken() != null && !user.getFcmToken().isEmpty())
                .toList();
    }

    /**
     * 사용자의 선호하는 팀 이름 목록 추출
     */
    private List<String> getUserTeamNames(User user) {
        return user.getUserTeamMap().stream()
                .map(userTeamMap -> userTeamMap.getTeam().getTeamName())
                .toList();
    }

    /**
     * 시간 범위 내에 있는 경기 일정 필터링
     */
    private List<MatchSchedule> getUpcomingMatches(List<String> teamNames, LocalDateTime now, LocalDateTime plusHour) {
        return matchScheduleRepository.findByTeam1InOrTeam2In(teamNames, teamNames)
                .stream()
                .filter(match -> isMatchWithinTimeRange(match, now, plusHour))
                .toList();
    }

    /**
     * 경기 일정이 지정된 시간 범위에 포함되는지 확인
     */
    private boolean isMatchWithinTimeRange(MatchSchedule match, LocalDateTime now, LocalDateTime plusHour) {
        try {
            String dateStr = match.getMatchDate()
                    .replaceAll("[^0-9]", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("-$", "");

            LocalDate matchDate = LocalDate.parse(now.getYear() + "-" + dateStr,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalTime matchTime = LocalTime.parse(match.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime matchDateTime = LocalDateTime.of(matchDate, matchTime);

            return !matchDateTime.isBefore(now) && matchDateTime.isBefore(plusHour);
        } catch (Exception e) {
            log.error("날짜 변환 실패! 원본 데이터: {} {}", match.getMatchDate(), match.getStartTime(), e);
            return false;
        }
    }

    /**
     * 경기 정보를 문자열로 변환 : 02월 01일 (토) → 02-01
     */
    private String buildMatchDetails(List<MatchSchedule> matches) {
        return matches.stream()
                .map(match -> String.format("%s %s - %s vs %s",
                        match.getMatchDate(), match.getStartTime(), match.getTeam1(), match.getTeam2()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Firebase 메시지 객체 생성
     */
    private Message buildFcmMessage(String token, String body) {
        if (token == null || token.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_FCMTOKEN);
        }

        return Message.builder()
                .putData("title", "LCK 정보 사이트")
                .putData("body", body)
                .setToken(token)
                .build();
    }

    /**
     * Firebase 메시지 전송 및 결과 처리
     */
    @Transactional
    private void sendFcmMessage(User user, Message message, List<String> results) {
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("message : "  + message);
            log.info("response : " + response);

            results.add(user.getId() + "님에게 알림을 성공적으로 발송하였습니다. ");
        } catch (FirebaseMessagingException e) {

            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                log.warn("{}님의 유효하지 않은 토큰 발견 ( 삭제하였습니다. )", user.getEmail());

                user.setFcmToken("deleted");
                userRepository.save(user);
            } else {
                results.add(user.getId() + "님에게 알림 발송을 실패하였습니다. ");
            }
        }
    }
}


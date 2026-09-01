package com.example.crawling.notification.application;

import com.example.crawling.global.exception.CustomException;
import com.example.crawling.global.exception.ErrorCode;
import com.example.crawling.notification.ui.dto.FcmRequestDto;
import com.example.crawling.schedule.domain.MatchSchedule;
import com.example.crawling.schedule.domain.MatchScheduleRepository;
import com.example.crawling.user.domain.User;
import com.example.crawling.user.domain.UserRepository;
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
import java.util.Locale;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class FcmService {

    private final UserRepository userRepository;
    private final MatchScheduleRepository matchScheduleRepository;

    public void registerFcmToken(FcmRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_USER));

        user.updateFcmToken(dto.getFcmToken());
        userRepository.save(user);
    }

    public String pushMatchSchedule(int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime plusHour = now.plusHours(hours);

        List<User> users = getUsersWithValidFcmToken();
        List<String> results = new ArrayList<>();

        for (User user : users) {
            List<String> teamNames = getUserTeamNames(user);
            List<MatchSchedule> upcomingMatches = getUpcomingMatches(teamNames, now, plusHour);

            if (upcomingMatches.isEmpty()) {
                log.info("{}님이 선호하는 예정된 경기 일정이 없습니다.", user.getName());
                continue;
            }

            String messageBody = buildMatchDetails(upcomingMatches);
            Message message = buildFcmMessage(user.getFcmToken(), messageBody);
            sendFcmMessage(user, message, results);
        }

        return String.join("\n", results);
    }

    public String pushAllUser(String notice) {
        List<User> users = getUsersWithValidFcmToken();
        List<String> results = new ArrayList<>();

        for (User user : users) {
            Message message = buildFcmMessage(user.getFcmToken(), notice);
            sendFcmMessage(user, message, results);
        }

        return String.join("\n", results);
    }

    private List<User> getUsersWithValidFcmToken() {
        return userRepository.findAll().stream()
                .filter(user -> user.getFcmToken() != null && !user.getFcmToken().isEmpty())
                .toList();
    }

    private List<String> getUserTeamNames(User user) {
        return new ArrayList<>(user.getPreferredTeams());
    }

    private List<MatchSchedule> getUpcomingMatches(List<String> teamNames, LocalDateTime now, LocalDateTime plusHour) {
        return matchScheduleRepository.findByTeam1InOrTeam2In(teamNames, teamNames)
                .stream()
                .filter(match -> isMatchWithinTimeRange(match, now, plusHour))
                .toList();
    }

    private boolean isMatchWithinTimeRange(MatchSchedule match, LocalDateTime now, LocalDateTime plusHour) {
        try {
            LocalTime matchTime = LocalTime.parse(match.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime matchDateTime = LocalDateTime.of(match.getMatchDate(), matchTime);
            return !matchDateTime.isBefore(now) && matchDateTime.isBefore(plusHour);
        } catch (Exception e) {
            log.error("날짜 변환 실패! 원본 데이터: {} {}", match.getMatchDate(), match.getStartTime(), e);
            return false;
        }
    }

    private static final DateTimeFormatter MATCH_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM월 dd일 (E)", Locale.KOREAN);

    private String buildMatchDetails(List<MatchSchedule> matches) {
        return matches.stream()
                .map(match -> String.format("%s %s - %s vs %s",
                        match.getMatchDate().format(MATCH_DATE_FORMATTER),
                        match.getStartTime(), match.getTeam1(), match.getTeam2()))
                .collect(Collectors.joining("\n"));
    }

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

    @Transactional
    private void sendFcmMessage(User user, Message message, List<String> results) {
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("message : {}", message);
            log.info("response : {}", response);
            results.add(user.getId() + "님에게 알림을 성공적으로 발송하였습니다. ");
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                log.warn("{}님의 유효하지 않은 토큰 발견 ( 삭제하였습니다. )", user.getEmail());
                user.updateFcmToken(null);
                userRepository.save(user);
            } else {
                results.add(user.getId() + "님에게 알림 발송을 실패하였습니다. ");
            }
        }
    }
}

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

    public String pushUserMatch(int param) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime plusHour = now.plusHours(param);

        List<User> users = userRepository.findAll().stream()
                .filter(user -> user.getFcmToken() != null && !user.getFcmToken().isEmpty())
                .toList();

        log.info("#### users.size: " + users.size());

        List<String> results = new ArrayList<>();

        for (User user : users) {

            List<String> teamNames = user.getUserTeamMap().stream()
                    .map(userTeamMap -> userTeamMap.getTeam().getTeamName())
                    .toList();

            log.info("#### teamNames.size: " + teamNames.size());

            List<MatchSchedule> upcomingMatches = matchScheduleRepository.findByTeam1InOrTeam2In(teamNames, teamNames)
                    .stream()
                    .filter(match -> {
                        try {
                            // 경기 날짜 문자열 변환: "02월 01일 (토)" → "02-01"
                            String dateStr = match.getMatchDate()
                                    .replaceAll("[^0-9]", "-") // 숫자가 아닌 문자("월", "일", "(", ")")를 "-"로 변경
                                    .replaceAll("-+", "-") // 연속된 "-"를 하나로 변환
                                    .replaceAll("-$", ""); // 마지막 "-" 제거

                            // 경기 날짜 변환 (현재 연도 추가)
                            LocalDate matchDate = LocalDate.parse(now.getYear() + "-" + dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                            // 경기 시간 변환 (예: "15:00" → LocalTime)
                            LocalTime matchTime = LocalTime.parse(match.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));

                            // 경기 날짜 + 시간 합쳐서 LocalDateTime 생성 -> 각 경기의 일정 : yyyy-MM-dd HH:mm
                            LocalDateTime matchDateTime = LocalDateTime.of(matchDate, matchTime);

                            // 현재보다 나중의 경기들 & "현재 + 특정 시간" 보다 이전 경기들
                            return !matchDateTime.isBefore(now) && matchDateTime.isBefore(plusHour);
                        } catch (Exception e) {
                            log.error("날짜 변환 실패! 원본 데이터: " + match.getMatchDate() + " " + match.getStartTime(), e);
                            return false;
                        }
                    })
                    .toList();

            if (upcomingMatches.isEmpty()) {
                log.info(user.getName() + "님이 선호하는 예정된 경기 일정이 없습니다.");
                continue;
            }

            String token = user.getFcmToken();
            log.info("fcm token : " + token);

            if (user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_FCMTOKEN);
            }

            String matchDetails = upcomingMatches.stream()
                    .map(match -> String.format("%s %s - %s vs %s",
                            match.getMatchDate(), match.getStartTime(), match.getTeam1(), match.getTeam2()))
                    .collect(Collectors.joining("\n"));

            Message message = Message.builder()
                    .setNotification(
                            Notification.builder()
                                    .setTitle("선호하는 경기 알림")
                                    .setBody(matchDetails)
                                    .build()
                    )
                    .setToken(token)
                    .build();

            try {
                String response = FirebaseMessaging.getInstance().send(message);

                results.add("Message sent successfully to " + user.getId() + ": " + response);
            } catch (FirebaseMessagingException e) {

                if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                    // 만료된 토큰은 삭제 처리
                    log.warn(user.getEmail() + "님의 유효하지 않은 토큰 발견 (삭제 처리 진행)");
                    user.setFcmToken("deleted");
                    userRepository.save(user);
                } else {
                    results.add("Failed to send message to " + user.getId() + ": " + e.getMessage());
                }
            }

        }
        return String.join("\n", results);
    }

    public void registerFcm(FcmRequestDto dto) {

        User user = userRepository.findByEmail(dto.getEmail());

        if (user == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_USER);
        }

        user.setFcmToken(dto.getFcmToken());

        userRepository.save(user);
    }
}


package com.example.crawling.fcm;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.jwt.JwtUtil;
import com.example.crawling.oauth.CustomOAuth2User;
import com.example.crawling.schedule.MatchSchedule;
import com.example.crawling.schedule.MatchScheduleRepository;
import com.example.crawling.user.User;
import com.example.crawling.user.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class FcmService {

    private final UserRepository userRepository;
    private final MatchScheduleRepository matchScheduleRepository;
    private final JwtUtil jwtUtil;

    public String pushUserMatch() {

        String email = "acd2283@gmail.com";

        log.info("알림 전송 실행");

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_USER);
        }

        List<String> teamNames = user.getUserTeamMap().stream()
                .map(userTeamMap -> userTeamMap.getTeam().getTeamName())
                .toList();

        LocalDateTime now = LocalDateTime.now();

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

                        // 경기 날짜 + 시간 합쳐서 LocalDateTime 생성
                        LocalDateTime matchDateTime = LocalDateTime.of(matchDate, matchTime);

                        return !matchDateTime.isBefore(now); // 현재 시간 이전이면 제외
                    } catch (Exception e) {
                        log.error("날짜 변환 실패! 원본 데이터: " + match.getMatchDate() + " " + match.getStartTime(), e);
                        return false;
                    }
                })
                .toList();

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
            // 메시지 전송
            String response = FirebaseMessaging.getInstance().send(message);

            return "Message sent successfully: " + response;
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            return "Failed to send message";
        }

    }
}


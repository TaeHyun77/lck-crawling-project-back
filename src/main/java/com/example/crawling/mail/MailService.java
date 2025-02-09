package com.example.crawling.mail;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.schedule.MatchSchedule;
import com.example.crawling.schedule.MatchScheduleRepository;
import com.example.crawling.user.User;
import com.example.crawling.user.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final MatchScheduleRepository matchScheduleRepository;

    @Scheduled(cron = "0 0 * * * *")  // 매 정각 실행
    public void sendReminder24() {
        sendSimpleMailMessage(23);
    }

    @Scheduled(cron = "0 1 * * * *") // 매시간 1분에 실행, 경기 시작 3시간 전부터 1시간마다 알림
    public void sendReminder3() {
        sendSimpleMailMessage(3);
    }

    public void sendSimpleMailMessage(int hoursBefore) {

        User user;

        try {
            user = userRepository.findByEmail("btaehyeon552@gmail.com");
        } catch (CustomException e) {
            log.info("해당 이메일의 유저를 찾을 수 없습니다");
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_USER);
        }

        List<String> teamNames = user.getUserTeamMap().stream()
                .map(userTeamMap -> userTeamMap.getTeam().getTeamName())
                .toList();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetTime = now.plusHours(hoursBefore);

        List<MatchSchedule> userMatchSchedules = matchScheduleRepository.findByTeam1InOrTeam2In(teamNames, teamNames)
                .stream()
                .filter(match -> {
                    try {

                        // 경기 날짜 문자열 변환: "02월 01일 (토)" → "02-01"
                        String dateStr = match.getMatchDate()
                                .replaceAll("[^0-9]", "-")
                                .replaceAll("-+", "-")
                                .replaceAll("-$", "");

                        LocalDate matchDate = LocalDate.parse(now.getYear() + "-" + dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                        LocalTime matchTime = LocalTime.parse(match.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));

                        LocalDateTime matchDateTime = LocalDateTime.of(matchDate, matchTime);
                        log.info("현재 시각 : " + now);

                        // 경기 시각이 현재 시각보다 나중 + (경기 시각이 다음 날 오전 12시를 기준으로 전)
                        return !matchDateTime.isBefore(now) && matchDateTime.isBefore(targetTime);
                    } catch (Exception e) {
                        log.error("날짜 변환 실패 !");
                        return false;
                    }
                })
                .toList();

        if (!userMatchSchedules.isEmpty()) {
            sendEmail(user, userMatchSchedules);
        }
    }

    private void sendEmail(User user, List<MatchSchedule> matchSchedules) {

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        String matchDetails = matchSchedules.stream()
                .map(match -> String.format(
                        "%s %s  %s  %s vs %s",
                        match.getMatchDate(), match.getStartTime(),
                        match.getStageType(), match.getTeam1(),
                        match.getTeam2()
                ))
                .collect(Collectors.joining("\n"));

        try {
            // 메일을 받을 수신자 설정
            simpleMailMessage.setTo(user.getEmail());

            // 메일의 제목 설정
            simpleMailMessage.setSubject("선호 팀 경기 일정 알림");

            // 메일의 내용 설정
            simpleMailMessage.setText("경기 일정 알람 \n\n" + matchDetails);

            javaMailSender.send(simpleMailMessage);

            log.info("메일 발송 성공!");
        } catch (Exception e) {
            log.info("메일 발송 실패!");
            throw new RuntimeException(e);
        }
    }
}

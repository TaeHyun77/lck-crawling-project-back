package com.example.crawling.user;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public ResponseEntity<?> userInfo(String token) {

        try {

            if (jwtUtil.isExpired(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UNAUTHORIZED");
            }

            String username = jwtUtil.getUsername(token);
            String role = jwtUtil.getRole(token);

            User user = userRepository.findByUsername(username);

            String name = user.getName();
            String email = user.getEmail();

            List<String> teamNames = user.getUserTeamMap().stream()
                    .map(userTeamMap -> userTeamMap.getTeam().getTeamName())
                    .toList();

            boolean notificationPermission = user.isNotificationPermission();

            UserResponseDto info = UserResponseDto.of(username, role, name, email, teamNames, notificationPermission);

            return new ResponseEntity<>(info, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
    }

    public void notificationPermission(UserNotificationDto dto) {

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_USER));

        user.setNotificationPermission(dto.isNotificationPermission());

        userRepository.save(user);

    }

    public ResponseEntity<String> googleLogout(HttpServletRequest request, HttpServletResponse response) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        log.info("로그 아웃 성공 !");

        return ResponseEntity.ok("로그 아웃 성공");
    }
}

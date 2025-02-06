package com.example.crawling.user;

import com.example.crawling.jwt.JwtUtil;
import com.example.crawling.oauth.CustomOAuth2User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @GetMapping("/info")
    public ResponseEntity<?> userInfo(HttpServletRequest request) {

        String authorizationHeader = request.getHeader("authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }

        String token = authorizationHeader.substring(7);

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

            UserResponseDto info = UserResponseDto.of(username, role, name, email, teamNames);

            return new ResponseEntity<>(info, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/googleLogin")
    public ResponseEntity<?> googleLogin(HttpServletResponse response) {
        log.info("Login request success");
        String redirectUrl = "http://localhost:8080/oauth2/authorization/google";
        return ResponseEntity.ok().body(Map.of("url", redirectUrl));
    }

    @PostMapping("/googleLogout")
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

package com.example.crawling.user;

import com.example.crawling.jwt.JwtUtil;
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

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {

    private final JwtUtil jwtUtil;

    @GetMapping("/info")
    public ResponseEntity<?> userInfo(HttpServletRequest request) {

        String authorizationHeader = request.getHeader("Authorization");
        log.info(authorizationHeader);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }

        String token = authorizationHeader.substring(7);

        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        UserResponseDto info = UserResponseDto.of(username, role);

        return new ResponseEntity<>(info, HttpStatus.OK);
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

        return ResponseEntity.ok("로그아웃 성공");
    }

}

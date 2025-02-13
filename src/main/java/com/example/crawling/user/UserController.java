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

    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<?> userInfo(HttpServletRequest request) {

        String authorizationHeader = request.getHeader("authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }

        String token = authorizationHeader.substring(7);

        return userService.userInfo(token);
    }

    @GetMapping("/googleLogin")
    public ResponseEntity<?> googleLogin(HttpServletResponse response) {
        log.info("Login request success");
        String redirectUrl = "http://localhost:8080/oauth2/authorization/google";

        return ResponseEntity.ok().body(Map.of("url", redirectUrl));
    }

    @PostMapping("/googleLogout")
    public ResponseEntity<String> googleLogout(HttpServletRequest request, HttpServletResponse response) {

        return userService.googleLogout(request, response);

    }
}

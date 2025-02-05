package com.example.crawling.refresh_retoken;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.jwt.JwtUtil;
import com.example.crawling.refresh_retoken.RefreshRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReissueService {

    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public ResponseEntity<?> reissue(String refreshAuthorization, HttpServletRequest request, HttpServletResponse response) {

        String refresh = refreshAuthorization.substring(7);
        log.info("Reissue : " + refresh);

        try {
            jwtUtil.isExpired(refresh);
        } catch (CustomException e){
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.EXPIRED_TOKEN);
        }

        String category = jwtUtil.getCategory(refresh);

        if (!category.equals("refresh")) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        /*
        if (refreshRepository.existsByRefresh(refresh)) {
            refreshRepository.deleteByRefresh(refresh);
        } else {
            log.info("refresh 토큰이 DB에 존재하지 않습니다.");
        }
        */

        String newJwt = jwtUtil.createJwt("access", username, role, 60*10000L);
        String newRefresh = jwtUtil.createJwt("refresh", username, role, 60*60*10000L);

        log.info("New Access Token: " + newJwt);

        response.setHeader("authorization", newJwt );
        response.addCookie(createCookie("refreshAuthorization", newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setSecure(false);
        cookie.setPath("/");

        return cookie;
    }

}

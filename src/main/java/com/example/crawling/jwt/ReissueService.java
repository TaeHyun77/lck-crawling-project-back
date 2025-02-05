package com.example.crawling.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ReissueService {

    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public ResponseEntity<String> reissue(HttpServletRequest request, HttpServletResponse response) {

        String refresh = null;

        Cookie[] cookies=request.getCookies();

        for (Cookie cookie:cookies){
            if(cookie.getName().equals("refreshAuthorization")){
                refresh = cookie.getValue();
            }
        }

        if (refresh == null){
            return ResponseEntity.badRequest().body("Refresh token is empty");
        }

        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e){
            return ResponseEntity.badRequest().body("refresh token expired");
        }

        String category = jwtUtil.getCategory(refresh);

        if(!category.equals("refresh")){
            return ResponseEntity.badRequest().body("invalid refresh token");
        }

        String username=jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        refreshRepository.deleteByRefresh(refresh);

        String newJwt = jwtUtil.createJwt("access", username, role, 60*10000L);
        String newRefresh = jwtUtil.createJwt("refresh", username, role, 60*60*10000L);

        response.setHeader("Authorization", newJwt);
        response.addCookie(createCookie("refreshAuthorization", newRefresh));

        return ResponseEntity.ok("새 access 토큰 발급");
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setSecure(true);
        cookie.setPath("/");

        return cookie;
    }
}

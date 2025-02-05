package com.example.crawling.jwt;


import com.example.crawling.oauth.CustomOAuth2User;
import com.example.crawling.user.User;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorization = null;

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            filterChain.doFilter(request, response);
            return;
        }

        for (Cookie cookie : cookies) {

            if (cookie.getName().equals("Authorization")) {

                authorization = cookie.getValue();
                log.info("authorization : " + authorization);
            }
        }

        if (authorization == null) {

            log.info("token is null");
            filterChain.doFilter(request, response);

            return;
        }

        String token = authorization;

        try {
            if (jwtUtil.isExpired(token)) {
                log.info("token expired");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 반환
                return;
            }

            String username = jwtUtil.getUsername(token);
            String role = jwtUtil.getRole(token);

            User user = User.builder()
                    .username(username)
                    .role(role)
                    .build();

            Map<String, Object> attributes = new HashMap<>();
            CustomOAuth2User customOAuth2User = new CustomOAuth2User(user, attributes);

            Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token detected, returning 401 Unauthorized");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 반환
        } catch (Exception e) {
            log.error("Error during JWT processing", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 예기치 않은 에러는 500 반환
        }
    }
}

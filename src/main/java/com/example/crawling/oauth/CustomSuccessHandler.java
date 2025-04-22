package com.example.crawling.oauth;

import com.example.crawling.jwt.JwtUtil;
import com.example.crawling.refresh_retoken.Refresh;
import com.example.crawling.refresh_retoken.RefreshRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.Security;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String token = jwtUtil.createJwt("access", username, role, 1800000L); // 30분
        String refreshToken = jwtUtil.createJwt("refresh", username, role, 259200000L); // 3일

        Refresh refresh = Refresh.builder()
                .username(username)
                .refresh(refreshToken)
                .expiration((new Date(System.currentTimeMillis() + 60*60*10000L)).toString())
                .build();


        refreshRepository.save(refresh);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        response.setHeader("authorization", token);
        response.addCookie(createCookie("authorization", token));
        response.addCookie(createCookie("refreshAuthorization", refreshToken));

        Date expirationTime = jwtUtil.getExpiration(token);
        Date reExpirationTime = jwtUtil.getExpiration(refreshToken);

        log.info("Access 토큰이 발급 되었습니다.");
        log.info("Access 토큰 만료 시간: " + expirationTime);
        log.info("refresh 토큰 만료 시간: " + reExpirationTime);

        response.sendRedirect("http://localhost:3000/?month=4");

        //response.sendRedirect("https://d26j07ydg0bh96.cloudfront.net/?month=2");
    }

//    private Cookie createCookie(String key, String value) {
//
//        Cookie cookie = new Cookie(key, value);
//        cookie.setMaxAge(24*60*60);
//        //cookie.setSecure(false);
//         cookie.setPath("/");
//
//        return cookie;
//    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60); // 1일
        cookie.setSecure(true); // HTTPS에서만 쿠키 전달
        cookie.setHttpOnly(false); // JavaScript에서 쿠키 접근 불가 (보안 강화)
        cookie.setPath("/"); // 모든 경로에서 쿠키 사용 가능

        // Spring Boot 2.1 이상에서는 setAttribute 사용 가능
        cookie.setAttribute("SameSite", "None"); // 크로스 도메인에서도 쿠키 전달 가능

        return cookie;
    }

}

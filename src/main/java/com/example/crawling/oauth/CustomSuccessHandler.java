package com.example.crawling.oauth;

import com.example.crawling.jwt.JwtUtil;
import com.example.crawling.jwt.Refresh;
import com.example.crawling.jwt.RefreshRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

@RequiredArgsConstructor
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String token = jwtUtil.createJwt("access", username, role, 60*10000L); // 10분
        String refreshToken = jwtUtil.createJwt("refresh", username, role, 60*60*10000L); // 1시간

        Refresh refresh = Refresh.builder()
                .username(username)
                .refresh(refreshToken)
                .expiration((new Date(System.currentTimeMillis() + 60*60*10000L)).toString())
                .build();

        refreshRepository.save(refresh);

        response.addCookie(createCookie("Authorization", token));
        response.setHeader("Authorization", token);

        response.addCookie(createCookie("refreshAuthorization", refreshToken));
        response.sendRedirect("http://localhost:3000/");
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setSecure(true);
        cookie.setPath("/");

        return cookie;
    }
}

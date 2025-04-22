package com.example.crawling.refresh_retoken;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.jwt.JwtUtil;
import com.example.crawling.refresh_retoken.RefreshRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        deleteRefreshToken(refresh);

        String newJwt = jwtUtil.createJwt("access", username, role, 1800000L); // 3시간
        String newRefresh = jwtUtil.createJwt("refresh", username, role, 259200000L); // 3일

        log.info("New Access Token: " + newJwt);

        response.setHeader("authorization", newJwt );
        response.addCookie(createCookie("refreshAuthorization", newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    public void deleteRefreshToken(String refresh) {
        try {
            refreshRepository.findByRefresh(refresh)
                    .ifPresentOrElse(refreshRepository::delete,
                            () -> log.info("Refresh 토큰이 DB에 존재하지 않습니다."));

        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            log.warn("낙관적 락 충돌 발생! 다른 요청이 먼저 삭제 되었습니다.");
            throw new CustomException(HttpStatus.CONFLICT, ErrorCode.OPTIMISTICLOCKING, "다른 요청이 먼저 Refresh 토큰을 삭제했습니다. 다시 시도하세요.");
        }
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60); // 1일
        cookie.setSecure(true); // HTTPS에서만 쿠키 전달
        cookie.setHttpOnly(false); // JavaScript에서 쿠키 접근 불가 (보안 강화)
        cookie.setPath("/"); // 모든 경로에서 쿠키 사용 가능

        cookie.setAttribute("SameSite", "None"); // 크로스 도메인에서도 쿠키 전달 가능

        return cookie;
    }
}

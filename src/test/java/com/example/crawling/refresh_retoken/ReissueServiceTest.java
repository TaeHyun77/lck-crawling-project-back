/*

package com.example.crawling.refresh_retoken;

import com.example.crawling.refresh_retoken.Refresh;
import com.example.crawling.refresh_retoken.RefreshRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("낙관적 락을 활용한 Refresh 토큰 동시 삭제 테스트")
@SpringBootTest
class ReissueServiceTest {

    @Autowired
    private RefreshRepository refreshRepository;

    @Test
    void 낙관적락_refresh_삭제_테스트() throws InterruptedException {
        // Refresh 토큰 1개 생성
        Refresh savedRefresh = refresh_토큰_생성();

        // 동시에 3개의 스레드가 삭제 요청을 보냄
        int numberOfThreads = 3;

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        Future<?> future1 = executorService.submit(
                () -> refreshRepository.deleteByRefresh(savedRefresh.getRefresh()));
        Future<?> future2 = executorService.submit(
                () -> refreshRepository.deleteByRefresh(savedRefresh.getRefresh()));
        Future<?> future3 = executorService.submit(
                () -> refreshRepository.deleteByRefresh(savedRefresh.getRefresh()));

        Exception result = new Exception();

        try {
            future1.get();
            future2.get();
            future3.get();
        } catch (ExecutionException e) {
            result = (Exception) e.getCause();
        }

        // OptimisticLockingFailureException 발생 확인
        assertTrue(result instanceof OptimisticLockingFailureException);
    }

    private Refresh refresh_토큰_생성() {
        Refresh refresh = Refresh.builder()
                .username("testUser")
                .refresh("sample_refresh_token")
                .expiration("2024-12-31")
                .build();

        return refreshRepository.save(refresh);
    }
}
*/
package com.example.crawling.refresh_retoken;

import com.example.crawling.config.BaseTime;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
public class Refresh extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String refresh;

    private String expiration;

    // 낙관적 락을 사용하여 refresh 토큰을 삭제하는 transaction에서 여러 요청이 들어올 경우 에러를 방지
    @Version
    private Integer version;

    @Builder
    public Refresh(String username, String refresh, String expiration) {
        this.username = username;
        this.refresh = refresh;
        this.expiration = expiration;
    }
}

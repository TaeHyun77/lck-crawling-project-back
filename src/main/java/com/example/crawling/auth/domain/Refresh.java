package com.example.crawling.auth.domain;

import com.example.crawling.global.BaseTime;
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

    // 낙관적 락: 동시에 여러 재발급 요청이 들어올 때 중복 삭제 방지
    @Version
    private Integer version;

    @Builder
    public Refresh(String username, String refresh, String expiration) {
        this.username = username;
        this.refresh = refresh;
        this.expiration = expiration;
    }
}

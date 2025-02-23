package com.example.crawling.user;

import com.example.crawling.config.BaseTime;
import com.example.crawling.team.UserTeamMap;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class User extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String name;

    private String email;

    private String role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserTeamMap> userTeamMap = new ArrayList<>();

    private String fcmToken;

    private boolean notificationPermission;

    @Builder
    public User(String username, String name, String email, String role, boolean notificationPermission) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.role = role;
        this.notificationPermission = notificationPermission;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void updateNotificationPermission(boolean notificationPermission) {
        this.notificationPermission = notificationPermission;
    }
}

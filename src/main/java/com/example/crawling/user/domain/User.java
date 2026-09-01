package com.example.crawling.user.domain;

import com.example.crawling.global.BaseTime;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
public class User extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String name;

    private String email;

    private String role;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preferred_team", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "team_name")
    private Set<String> preferredTeams = new HashSet<>();

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

    public void updatePreferredTeams(Set<String> newTeams) {
        this.preferredTeams.clear();
        this.preferredTeams.addAll(newTeams);
    }

    public void updateNotificationPermission(boolean permission) {
        this.notificationPermission = permission;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}

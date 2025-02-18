package com.example.crawling.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDto {

    private String username;
    private String name;
    private String email;
    private String role;

    private List<String> teamName;

    private boolean notificationPermission;

    @Builder
    public UserDto(String username, String name, String email, String role, boolean notificationPermission) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.role = role;
        this.notificationPermission = notificationPermission;
    }

    public User toUser() {
        return User.builder()
                .username(username)
                .name(name)
                .email(email)
                .role(role)
                .notificationPermission(notificationPermission)
                .build();
    }
}

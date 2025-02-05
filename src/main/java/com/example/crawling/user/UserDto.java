package com.example.crawling.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    private String username;
    private String name;
    private String email;
    private String role;

    @Builder
    public UserDto(String username, String name, String email, String role) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public User toUser() {
        return User.builder()
                .username(username)
                .name(name)
                .email(email)
                .role(role)
                .build();
    }
}

package com.example.crawling.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserResponseDto {
    private String username;

    private String role;

    private String name;

    private String email;

    private List<String> teamNames;

    public static UserResponseDto of(String username,String role, String name, String email, List<String> teamNames) {
        return UserResponseDto.builder()
                .username(username)
                .role(role)
                .name(name)
                .email(email)
                .teamNames(teamNames)
                .build();
    }

}

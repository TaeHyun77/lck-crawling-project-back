package com.example.crawling.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserResponseDto {
    private String username;

    private String role;

    public static UserResponseDto of(String username,String role) {
        return UserResponseDto.builder()
                .username(username)
                .role(role)
                .build();
    }

}

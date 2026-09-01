package com.example.crawling.notification.ui.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FcmRequestDto {
    private String fcmToken;
    private String email;
    private String notificationPermission;
}

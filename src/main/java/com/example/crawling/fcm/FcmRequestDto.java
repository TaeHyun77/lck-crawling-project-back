package com.example.crawling.fcm;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FcmRequestDto {

    private String fcmToken;

    private String email;

}

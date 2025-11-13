package com.example.crawling.fcm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/register/fcmToken")
    public void registerFcmToken(@RequestBody FcmRequestDto dto) {

        fcmService.registerFcmToken(dto);
    }

    @PostMapping("/push/notification/{time}")
    public String notification(@PathVariable("time") int time) {
        log.info("알림 전송 실행");

        return fcmService.pushMatchSchedule(time);
    }

    @PostMapping("/push/notification/user/{notice}")
    public String pushAllUser(@PathVariable("notice") String notice) {
        return fcmService.pushAllUser(notice);
    }
}

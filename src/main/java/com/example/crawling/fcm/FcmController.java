package com.example.crawling.fcm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/fcm/register")
    public void getFcmToken(@RequestBody FcmRequestDto dto) {

        fcmService.registerFcm(dto);

    }

    @PostMapping("/push/notification")
    public String notification(@RequestParam("param") int param) {
        log.info("알림 전송 실행");
        return fcmService.pushUserMatch(param);
    }

//    @PostMapping("/test/fcm")
//    public String test() {
//        String token = "e_41W2F9y6aW3Rtnh_fmR6:APA91bF0y5usJSHUZnO078vKCCYrs0RQh7FxPjKQ0Vt3VHyO6-aMgMQtAiRGrw0gAdz9sAu9LKgBsFZkRM3aEwOV-FoPYZhegK5aVbmuLPrKhGsuUizoXjQ";
//
//        Message message = Message.builder()
//                .setNotification(
//                        Notification.builder()
//                                .setTitle("테스트 알림")
//                                .setBody("이것은 테스트 알림입니다.")
//                                .build()
//                )
//                .setToken(token)
//                .build();
//
//        try {
//            // 메시지 전송
//            String response = FirebaseMessaging.getInstance().send(message);
//
//            return "Message sent successfully: " + response;
//        } catch (FirebaseMessagingException e) {
//            e.printStackTrace();
//            return "Failed to send message";
//        }
//    }
}

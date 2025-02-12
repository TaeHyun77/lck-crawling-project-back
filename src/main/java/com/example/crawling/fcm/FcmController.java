package com.example.crawling.fcm;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.oauth.CustomOAuth2User;
import com.example.crawling.user.User;
import com.example.crawling.user.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class FcmController {

    private final UserRepository userRepository;
    private final FcmService fcmService;

    @PostMapping("/fcm/register")
    public void getFcmToken(@RequestBody FcmRequestDto dto) {

        User user = userRepository.findByEmail(dto.getEmail());

        if (user == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_USER);
        }

        user.setFcmToken(dto.getFcmToken());

        userRepository.save(user);
    }

    @PostMapping("/push/notification")
    public String test22() {
        log.info("알림 전송 실행");
        return fcmService.pushUserMatch();
    }

    @GetMapping("/me")
    public String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User user) {
            return "현재 로그인한 사용자: " + user.getUsername() + ", 이메일: " + user.getEmail();
        } else {
            return "로그인된 사용자가 없습니다.";
        }
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

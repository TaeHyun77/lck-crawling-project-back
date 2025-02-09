package com.example.crawling.mail;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @GetMapping("/simple")
    public void sendSimpleMailMessage() {
        mailService.sendSimpleMailMessage(1000);
    }
}


// 푸시 알림 동의 ??
// 리액트에서 선호 팀 변경하면 여기로 요청 오게끔 하고 유저 정보, 선호 팀 리스트 받아서 하게끔

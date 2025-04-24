package com.example.crawling.oauth;

import com.example.crawling.user.User;
import com.example.crawling.user.UserRequestDto;
import com.example.crawling.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("oAuth2User : " + oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;

        if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        String username = oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
        User findUser = userRepository.findByUsername(username);

        User user;

        if (findUser == null) {

            UserRequestDto userRequestDto = UserRequestDto.builder()
                    .username(username)
                    .name(oAuth2Response.getName())
                    .email(oAuth2Response.getEmail())
                    .role("ROLE_USER")
                    .notificationPermission(false)
                    .build();

            userRepository.save(userRequestDto.toUser());

            return new CustomOAuth2User(userRequestDto.toUser(), oAuth2User.getAttributes());
        } else {
            log.info(username + ", 이미 가입한 사용자입니다.");
            user = findUser;
        }

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }
}

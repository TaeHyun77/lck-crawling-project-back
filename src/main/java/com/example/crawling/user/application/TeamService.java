package com.example.crawling.user.application;

import com.example.crawling.global.exception.CustomException;
import com.example.crawling.global.exception.ErrorCode;
import com.example.crawling.user.domain.User;
import com.example.crawling.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TeamService {
    private final UserRepository userRepository;

    @Transactional
    public void saveTeamName(String username, List<String> selectedTeams) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_USER));

        user.updatePreferredTeams(new HashSet<>(selectedTeams));
    }
}

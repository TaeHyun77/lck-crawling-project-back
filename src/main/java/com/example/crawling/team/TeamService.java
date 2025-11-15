package com.example.crawling.team;

import com.example.crawling.exception.CustomException;
import com.example.crawling.exception.ErrorCode;
import com.example.crawling.user.User;
import com.example.crawling.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TeamService {

    private final UserRepository userRepository;
    private final UserTeamMapRepository userTeamMapRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public void saveTeamName(String username, List<String> selectedTeams) {

        // user가 선호하는 팀 목록을 모두 삭제하고
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_FOUND_USER));

        // 해당 유저의 UserTeamMap 정보들을 조회
        List<UserTeamMap> existingUserTeams = userTeamMapRepository.findByUserId(user.getId());

        // 해당 유저의 팀 목록을 Set으로
        Set<String> currentTeamNames = existingUserTeams.stream()
                .map(utm -> utm.getTeam().getTeamName())
                .collect(Collectors.toSet());

        Set<String> newTeamNames = new HashSet<>(selectedTeams);

        // 새로 추가해야 할 팀 목록 구하기
        Set<String> teamsToAdd = newTeamNames.stream()
                .filter(teamName -> !currentTeamNames.contains(teamName))
                .collect(Collectors.toSet());

        // 삭제해야 할 팀 목록 구하기
        Set<String> teamsToRemove = currentTeamNames.stream()
                .filter(teamName -> !newTeamNames.contains(teamName))
                .collect(Collectors.toSet());

        // 삭제 처리 ( UserTeamMap 삭제 )
        if (!teamsToRemove.isEmpty()) {
            userTeamMapRepository.deleteByUserIdAndTeamNames(user.getId(), teamsToRemove);
        }

        // 추가
        addTeam(user, teamsToAdd);
    }

    private void addTeam(User user, Set<String> teamsToAdd) {
        for (String teamName : teamsToAdd) {
            Team team = teamRepository.findByTeamName(teamName)
                    .orElseGet(() -> {
                        Team newTeam = Team.builder()
                                .teamName(teamName)
                                .build();

                        return teamRepository.save(newTeam);
                    });

            userTeamMapRepository.save(UserTeamMap.builder()
                    .user(user)
                    .team(team)
                    .build());
        }
    }
}

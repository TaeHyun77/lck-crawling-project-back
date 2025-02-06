package com.example.crawling.team;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class TeamController {

    private final TeamService teamService;

    @PostMapping("/team")
    public void receiveTeams(@RequestBody TeamRequestDto req) {
        log.info("받은 팀 리스트: " + req.getUsername() + " , " +  req.getSelectedTeams());

        teamService.saveTeamName(req.getUsername(), req.getSelectedTeams());
    }
}


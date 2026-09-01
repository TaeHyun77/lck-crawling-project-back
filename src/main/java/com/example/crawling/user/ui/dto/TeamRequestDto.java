package com.example.crawling.user.ui.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TeamRequestDto {
    private String username;
    private List<String> selectedTeams;
}

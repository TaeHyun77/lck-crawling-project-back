package com.example.crawling.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface UserTeamMapRepository extends JpaRepository<UserTeamMap, Long> {

    List<UserTeamMap> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM UserTeamMap utm WHERE utm.user.id = :userId AND utm.team.teamName IN :teamNames")
    void deleteByUserIdAndTeamNames(@Param("userId") Long userId, @Param("teamNames") Set<String> teamNames);
}

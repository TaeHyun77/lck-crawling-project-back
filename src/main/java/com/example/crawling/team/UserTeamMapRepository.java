package com.example.crawling.team;

import com.example.crawling.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserTeamMapRepository extends JpaRepository<UserTeamMap, Long> {

    List<UserTeamMap> findByUser(User user);

    @Modifying
    @Query("DELETE FROM UserTeamMap utm WHERE utm.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    boolean existsByTeam(Team team);
}

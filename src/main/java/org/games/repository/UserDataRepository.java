package org.games.repository;

import org.games.model.Task;
import org.games.model.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface UserDataRepository extends JpaRepository<UserData, Long> {

    Optional<UserData> findByUsernameAndActiveTrue(String username);

    Optional<UserData> findByIdAndActiveTrue(Long id);

    @Query("""
    SELECT t FROM UserData u JOIN u.solvedTasks t
    WHERE u.id = :userId AND u.active = true AND t.gameType.name = :gameType
    """)
    Set<Task> findUserSolvedTasksByGameType(Long userId, String gameType);

}

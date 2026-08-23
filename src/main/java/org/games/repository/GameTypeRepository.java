package org.games.repository;

import org.games.model.GameType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GameTypeRepository extends JpaRepository<GameType, String> {

    @EntityGraph(attributePaths = {"pieces", "tasks"})
    @Query("SELECT g FROM GameType g WHERE g.name = :gameTypeId")
    Optional<GameType> findByIdWithDetails(String gameTypeId);

    @Query("SELECT g.name FROM GameType g ORDER BY g.name")
    List<String> findAllNames();

}
package org.games.service;

import org.games.BaseRepositoryTest;
import org.games.model.GameType;
import org.games.model.Piece;
import org.games.model.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Sql(
        scripts = "/db/gameTasksAndPieces.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class GameServiceTest extends BaseRepositoryTest {

    @Autowired
    private GameService gameService;

    @Transactional
    @Test
    void shouldGetAllGameTypes() {
        List<String> gameTypes = gameService.getAllGameTypes();
        assertThat(gameTypes).isNotNull();
        assertThat(gameTypes).hasSize(3);
        assertThat(gameTypes).containsExactly("house", "t", "tangram");
    }

    @Transactional
    @Test
    void shouldGetGameTypeById() {
        GameType gameType = gameService.getGameType("tangram");
        assertThat(gameType).isNotNull();
        assertThat(gameType.getName()).isEqualTo("tangram");
        assertThat(gameType.getTasks()).extracting(Task::getId)
                .containsExactlyInAnyOrder("tangram_001", "tangram_002", "tangram_003");
        assertThat(gameType.getPieces()).extracting(Piece::getId)
                .containsExactlyInAnyOrder("tangram_piece_1", "tangram_piece_2", "tangram_piece_3");
    }

}
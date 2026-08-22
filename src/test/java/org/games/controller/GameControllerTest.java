package org.games.controller;

import jakarta.persistence.EntityNotFoundException;
import org.games.model.GameType;
import org.games.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
@AutoConfigureMockMvc(addFilters = false)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @Test
    void shouldReturnAllGameTypes() throws Exception {
        when(gameService.getAllGameTypes()).thenReturn(
            List.of("game_type_1", "game_type_2", "game_type_3")
        );
        mockMvc.perform(get("/game-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("game_type_1"));
    }

    @Test
    void shouldReturnGameTypeById() throws Exception {
        when(gameService.getGameType("game_type_1"))
                .thenReturn(new GameType("game_type_1", Set.of(), Set.of()));
        mockMvc.perform(get("/game-types/game_type_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("game_type_1"));
    }

    @Test
    void shouldThrowWhenGameTypeNotFound() throws Exception {
        when(gameService.getGameType("game_type_1"))
                .thenThrow(new EntityNotFoundException("Game type game_type_1 not found"));
        mockMvc.perform(get("/game-types/game_type_1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Game type game_type_1 not found"));
    }

}
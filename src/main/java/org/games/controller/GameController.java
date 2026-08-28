package org.games.controller;

import lombok.RequiredArgsConstructor;
import org.games.dto.GameTypeSummaryDto;
import org.games.model.GameType;
import org.games.service.GameService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/game-types")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping
    public List<GameTypeSummaryDto> getAllGameTypes() {
        return gameService.getAllGameTypes();
    }

    @GetMapping("/{gameTypeId}")
    public GameType getGameType(@PathVariable String gameTypeId) {
        return gameService.getGameType(gameTypeId);
    }
}

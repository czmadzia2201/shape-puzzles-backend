package org.games.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.games.model.GameType;
import org.games.repository.GameTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameTypeRepository gameTypeRepository;

    public List<String> getAllGameTypes() {
        return gameTypeRepository.findAllNames();
    }

    public GameType getGameType(String gameTypeId) {
        return gameTypeRepository.findByIdWithDetails(gameTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Game type %s not found".formatted(gameTypeId)));
    }

}

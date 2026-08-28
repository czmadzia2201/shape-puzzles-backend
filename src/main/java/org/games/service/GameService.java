package org.games.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.games.dto.GameTypeSummaryDto;
import org.games.model.GameType;
import org.games.repository.GameTypeRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameTypeRepository gameTypeRepository;

    public List<GameTypeSummaryDto> getAllGameTypes() {
        List<GameType> allGameTypes = gameTypeRepository.findAll();
        List<GameTypeSummaryDto> gameTypeSummaries = allGameTypes.stream()
                .map(gt -> new GameTypeSummaryDto(gt.getName(), gt.getDisplayName()))
                .sorted(Comparator.comparing(GameTypeSummaryDto::name))
                .toList();
        return gameTypeSummaries;
    }

    public GameType getGameType(String gameTypeId) {
        return gameTypeRepository.findByIdWithDetails(gameTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Game type %s not found".formatted(gameTypeId)));
    }

}

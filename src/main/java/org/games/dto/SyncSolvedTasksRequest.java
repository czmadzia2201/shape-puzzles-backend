package org.games.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SyncSolvedTasksRequest(@NotNull List<String> taskIds) {
}

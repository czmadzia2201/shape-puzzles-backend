package org.games.dto;

import java.util.List;

public record SyncSolvedTasksRequest(List<String> taskIds) {
}

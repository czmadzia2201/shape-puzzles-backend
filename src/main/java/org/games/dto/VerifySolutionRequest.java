package org.games.dto;

import java.util.List;

public record VerifySolutionRequest(
        String taskId,
        List<List<GeometryPoint>> taskPolygons,
        List<PiecePlacement> pieces
) {}

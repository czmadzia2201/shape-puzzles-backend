package org.games.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record VerifySolutionRequest(
        @NotBlank String taskId,
        List<List<GeometryPoint>> taskPolygons,
        @NotEmpty @Valid List<PiecePlacement> pieces
) {}

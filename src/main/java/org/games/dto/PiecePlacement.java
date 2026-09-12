package org.games.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PiecePlacement(String id, @NotEmpty @Valid List<GeometryPoint> vertices) {
}

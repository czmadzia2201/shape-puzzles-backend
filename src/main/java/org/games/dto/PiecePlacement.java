package org.games.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PiecePlacement(String id, @NotEmpty @Size(min = 3) @Valid List<GeometryPoint> vertices) {
}

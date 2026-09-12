package org.games.dto;

import jakarta.validation.constraints.NotNull;

public record GeometryPoint(@NotNull Double x, @NotNull Double y) {
}

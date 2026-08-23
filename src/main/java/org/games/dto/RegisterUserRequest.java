package org.games.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(@NotBlank @Size(max = 25, min = 3) String username, @NotBlank String password) {}

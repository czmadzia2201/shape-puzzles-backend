package org.games.dto;

import java.util.List;

public record PiecePlacement(String id, List<GeometryPoint> vertices) {
}

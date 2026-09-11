package org.games.service;

import org.games.dto.GeometryPoint;
import org.games.dto.PiecePlacement;

import java.util.List;

public class GeometryTestHelper {

    static PiecePlacement piece(String id, GeometryPoint... points) {
        return new PiecePlacement(id, List.of(points));
    }

    static GeometryPoint point(double x, double y) {
        return new GeometryPoint(x, y);
    }

    static List<GeometryPoint> square(double minX, double minY, double maxX, double maxY) {
        return List.of(point(minX, minY), point(maxX, minY), point(maxX, maxY), point(minX, maxY));
    }
}

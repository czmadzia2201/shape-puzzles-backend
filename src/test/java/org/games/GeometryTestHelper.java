package org.games;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.games.dto.GeometryPoint;
import org.games.dto.PiecePlacement;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeometryTestHelper {

    public static PiecePlacement piece(String id, GeometryPoint... points) {
        return new PiecePlacement(id, List.of(points));
    }

    public static GeometryPoint point(double x, double y) {
        return new GeometryPoint(x, y);
    }

    public static List<GeometryPoint> square(double minX, double minY, double maxX, double maxY) {
        return List.of(point(minX, minY), point(maxX, minY), point(maxX, maxY), point(minX, maxY));
    }
}

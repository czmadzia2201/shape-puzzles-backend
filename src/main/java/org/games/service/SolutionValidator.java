package org.games.service;

import org.games.dto.GeometryPoint;
import org.games.dto.VerifySolutionRequest;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SolutionValidator {

    private static final double AREA_EPSILON = 1e-6;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    public boolean validate(VerifySolutionRequest request) {
        if (request.taskPolygons() == null
                || request.taskPolygons().isEmpty()
                || request.pieces() == null
                || request.pieces().isEmpty()) {
            return false;
        }

        Geometry task = createTaskGeometry(request.taskPolygons());

        List<Polygon> pieces = request.pieces().stream()
                .map(piece -> createPolygon(piece.vertices()))
                .toList();

        return allPiecesInsideTask(task, pieces)
                && noPiecesOverlap(pieces)
                && areasMatch(task, pieces);
    }

    private boolean allPiecesInsideTask(
            Geometry task,
            List<Polygon> pieces
    ) {
        return pieces.stream()
                .allMatch(piece ->
                        piece.difference(task).getArea() <= AREA_EPSILON
                );
    }

    private boolean noPiecesOverlap(List<Polygon> pieces) {
        for (int i = 0; i < pieces.size(); i++) {
            for (int j = i + 1; j < pieces.size(); j++) {

                double intersectionArea = pieces.get(i)
                        .intersection(pieces.get(j))
                        .getArea();

                if (intersectionArea > AREA_EPSILON) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean areasMatch(
            Geometry task,
            List<Polygon> pieces
    ) {
        double piecesArea = pieces.stream()
                .mapToDouble(Polygon::getArea)
                .sum();

        return Math.abs(task.getArea() - piecesArea) <= AREA_EPSILON;
    }

    private Geometry createTaskGeometry(
            List<List<GeometryPoint>> polygons
    ) {
        Geometry task = null;

        for (List<GeometryPoint> polygonPoints : polygons) {
            Polygon polygon = createPolygon(polygonPoints);

            task = task == null
                    ? polygon
                    : task.symDifference(polygon);
        }

        return task;
    }

    private Polygon createPolygon(List<GeometryPoint> points) {
        if (points == null || points.size() < 3) {
            throw new IllegalArgumentException(
                    "Polygon must have at least 3 vertices"
            );
        }

        Coordinate[] coordinates = new Coordinate[points.size() + 1];

        for (int i = 0; i < points.size(); i++) {
            GeometryPoint point = points.get(i);
            coordinates[i] = new Coordinate(point.x(), point.y());
        }

        GeometryPoint firstPoint = points.get(0);

        coordinates[points.size()] =
                new Coordinate(firstPoint.x(), firstPoint.y());

        return geometryFactory.createPolygon(coordinates);
    }
}
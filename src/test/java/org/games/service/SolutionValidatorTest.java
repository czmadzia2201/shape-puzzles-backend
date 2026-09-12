package org.games.service;

import org.games.dto.GeometryPoint;
import org.games.dto.PiecePlacement;
import org.games.dto.VerifySolutionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.games.GeometryTestHelper.*;

class SolutionValidatorTest {

    private SolutionValidator solutionValidator;

    @BeforeEach
    void setUp() {
        solutionValidator = new SolutionValidator();
    }

    @Test
    void shouldReturnTrueForCorrectSolution() {
        VerifySolutionRequest request = request(
                List.of(square(0, 0, 4, 4)),
                List.of(
                        piece(
                                "piece_1",
                                point(0, 0),
                                point(2, 0),
                                point(2, 4),
                                point(0, 4)
                        ),
                        piece(
                                "piece_2",
                                point(2, 0),
                                point(4, 0),
                                point(4, 4),
                                point(2, 4)
                        )
                )
        );

        boolean result = solutionValidator.validate(request);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenPieceIsOutsideTask() {
        VerifySolutionRequest request = request(
                List.of(square(0, 0, 4, 4)),
                List.of(
                        piece(
                                "piece_1",
                                point(0, 0),
                                point(2, 0),
                                point(2, 4),
                                point(0, 4)
                        ),
                        piece(
                                "piece_2",
                                point(2, 0),
                                point(4.5, 0),
                                point(4.5, 4),
                                point(2, 4)
                        )
                )
        );

        boolean result = solutionValidator.validate(request);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenPiecesOverlap() {
        VerifySolutionRequest request = request(
                List.of(square(0, 0, 4, 4)),
                List.of(
                        piece(
                                "piece_1",
                                point(0, 0),
                                point(2.5, 0),
                                point(2.5, 4),
                                point(0, 4)
                        ),
                        piece(
                                "piece_2",
                                point(2, 0),
                                point(3.5, 0),
                                point(3.5, 4),
                                point(2, 4)
                        )
                )
        );

        boolean result = solutionValidator.validate(request);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenPartOfTaskIsNotCovered() {
        VerifySolutionRequest request = request(
                List.of(square(0, 0, 4, 4)),
                List.of(
                        piece(
                                "piece_1",
                                point(0, 0),
                                point(2, 0),
                                point(2, 4),
                                point(0, 4)
                        )
                )
        );

        boolean result = solutionValidator.validate(request);

        assertThat(result).isFalse();
    }

    @Test
    void shouldAllowPiecesToTouchAtEdges() {
        VerifySolutionRequest request = request(
                List.of(square(0, 0, 4, 4)),
                List.of(
                        piece(
                                "piece_1",
                                point(0, 0),
                                point(2, 0),
                                point(2, 4),
                                point(0, 4)
                        ),
                        piece(
                                "piece_2",
                                point(2, 0),
                                point(4, 0),
                                point(4, 4),
                                point(2, 4)
                        )
                )
        );

        boolean result = solutionValidator.validate(request);

        assertThat(result).isTrue();
    }

    @Test
    void shouldHandleTaskWithHole() {
        List<GeometryPoint> outerPolygon = square(0, 0, 4, 4);
        List<GeometryPoint> hole = square(1, 1, 3, 3);

        VerifySolutionRequest request = request(
                List.of(outerPolygon, hole),
                List.of(
                        piece(
                                "top",
                                point(0, 0),
                                point(4, 0),
                                point(4, 1),
                                point(0, 1)
                        ),
                        piece(
                                "bottom",
                                point(0, 3),
                                point(4, 3),
                                point(4, 4),
                                point(0, 4)
                        ),
                        piece(
                                "left",
                                point(0, 1),
                                point(1, 1),
                                point(1, 3),
                                point(0, 3)
                        ),
                        piece(
                                "right",
                                point(3, 1),
                                point(4, 1),
                                point(4, 3),
                                point(3, 3)
                        )
                )
        );

        boolean result = solutionValidator.validate(request);

        assertThat(result).isTrue();
    }

    private VerifySolutionRequest request(
            List<List<GeometryPoint>> taskPolygons,
            List<PiecePlacement> pieces
    ) {
        return new VerifySolutionRequest(
                "task_1",
                taskPolygons,
                pieces
        );
    }

}
package org.games;

import com.fasterxml.jackson.databind.JsonNode;
import org.games.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.games.GeometryTestHelper.piece;
import static org.games.GeometryTestHelper.point;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "jwt.secret=test-secret-with-at-least-thirty-two-bytes")
@Sql(scripts = "/db/gameTasksAndPieces.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class E2EIntegrationTest extends BaseRepositoryTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCompleteUserJourney() {
        ResponseEntity<GameTypeSummaryDto[]> gameTypesResponse =
                restTemplate.getForEntity("/game-types", GameTypeSummaryDto[].class);

        assertThat(gameTypesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(gameTypesResponse.getBody()).extracting(GameTypeSummaryDto::name).contains("tangram");

        ResponseEntity<RegisterUserResponse> registerResponse = restTemplate.postForEntity(
                "/users",
                new RegisterUserRequest("player", "password1"),
                RegisterUserResponse.class
        );

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(registerResponse.getBody()).isNotNull();
        assertThat(registerResponse.getBody().username()).isEqualTo("player");

        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
                "/auth/login",
                new LoginRequest("player", "password1"),
                LoginResponse.class
        );

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        String accessToken = loginResponse.getBody().accessToken();
        String refreshToken = loginResponse.getBody().refreshToken();
        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        ResponseEntity<Void> syncResponse = restTemplate.exchange(
                "/users/me/solved-tasks/sync",
                HttpMethod.POST,
                authorizedRequest(
                        accessToken,
                        new SyncSolvedTasksRequest(List.of("tangram_001", "tangram_002"))
                ),
                Void.class
        );

        assertThat(syncResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<JsonNode> solvedTasksResponse = restTemplate.exchange(
                "/users/me/solved-tasks/tangram",
                HttpMethod.GET,
                authorizedRequest(accessToken, null),
                JsonNode.class
        );

        assertThat(solvedTasksResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(solvedTasksResponse.getBody()).isNotNull();
        List<String> solvedTaskIds = solvedTasksResponse.getBody().findValuesAsText("id");
        assertThat(solvedTaskIds).containsExactlyInAnyOrder("tangram_001", "tangram_002");

        ResponseEntity<Boolean> incorrectSolutionResponse = restTemplate.exchange(
                "/users/solved-tasks",
                HttpMethod.POST,
                authorizedRequest(accessToken, incorrectSolutionRequest("tangram_003")),
                Boolean.class
        );

        assertThat(incorrectSolutionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(incorrectSolutionResponse.getBody()).isFalse();

        ResponseEntity<JsonNode> solvedTasksAfterIncorrectSolutionResponse = restTemplate.exchange(
                "/users/me/solved-tasks/tangram",
                HttpMethod.GET,
                authorizedRequest(accessToken, null),
                JsonNode.class
        );

        assertThat(solvedTasksAfterIncorrectSolutionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<String> solvedTaskIdsAfterIncorrectSolution =
                solvedTasksAfterIncorrectSolutionResponse.getBody().findValuesAsText("id");

        assertThat(solvedTaskIdsAfterIncorrectSolution).containsExactlyInAnyOrder("tangram_001", "tangram_002");


        ResponseEntity<Boolean> correctSolutionResponse = restTemplate.exchange(
                "/users/solved-tasks",
                HttpMethod.POST,
                authorizedRequest(accessToken, correctSolutionRequest("tangram_003")),
                Boolean.class
        );

        assertThat(correctSolutionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(correctSolutionResponse.getBody()).isTrue();

        ResponseEntity<JsonNode> solvedTasksAfterCorrectSolutionResponse = restTemplate.exchange(
                "/users/me/solved-tasks/tangram",
                HttpMethod.GET,
                authorizedRequest(accessToken, null),
                JsonNode.class
        );

        assertThat(solvedTasksAfterCorrectSolutionResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        List<String> solvedTaskIdsAfterCorrectSolution =
                solvedTasksAfterCorrectSolutionResponse.getBody().findValuesAsText("id");

        assertThat(solvedTaskIdsAfterCorrectSolution).containsExactlyInAnyOrder("tangram_001", "tangram_002", "tangram_003");

        ResponseEntity<RefreshResponse> refreshResponse = restTemplate.postForEntity(
                "/auth/refresh",
                new RefreshRequest(refreshToken),
                RefreshResponse.class
        );

        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshResponse.getBody()).isNotNull();
        String refreshedAccessToken = refreshResponse.getBody().accessToken();
        assertThat(refreshedAccessToken).isNotBlank();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/users/me",
                HttpMethod.DELETE,
                authorizedRequest(refreshedAccessToken, null),
                Void.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> loginAfterDeletionResponse = restTemplate.postForEntity(
                "/auth/login",
                new LoginRequest("player", "password1"),
                String.class
        );

        assertThat(loginAfterDeletionResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private <T> HttpEntity<T> authorizedRequest(String accessToken, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(body, headers);
    }

    private VerifySolutionRequest correctSolutionRequest(String taskId) {
        List<PiecePlacement> pieces = List.of(
                piece("piece_1", point(0, 0), point(2, 0), point(2, 4), point(0, 4)),
                piece("piece_2", point(2, 0), point(4, 0), point(4, 4), point(2, 4))
        );
        return new VerifySolutionRequest(taskId, pieces);
    }

    private VerifySolutionRequest incorrectSolutionRequest(String taskId) {
        List<PiecePlacement> pieces = List.of(
                piece("piece_1", point(0, 0), point(2, 0), point(2, 4), point(0, 4))
        );
        return new VerifySolutionRequest(taskId, pieces);
    }

}

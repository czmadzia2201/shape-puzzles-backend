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
}

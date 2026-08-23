package org.games;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.games.dto.LoginRequest;
import org.games.dto.RefreshRequest;
import org.games.dto.RegisterUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestPropertySource(properties = "jwt.secret=test-secret-with-at-least-thirty-two-bytes")
class SecurityIntegrationTest extends BaseRepositoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void shouldRejectRequestsToProtectedEndpointsWithoutToken() throws Exception {
        mockMvc.perform(get("/users/me/solved-tasks/tangram"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/users/me/solved-tasks/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taskIds\":[]}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidTokenAndAcceptValidAccessToken() throws Exception {
        mockMvc.perform(get("/users/me/solved-tasks/tangram")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        Tokens tokens = registerAndLogin("player", "password1");

        mockMvc.perform(get("/users/me/solved-tasks/tangram")
                        .header("Authorization", bearer(tokens.accessToken())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAccessToPublicEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/game-types"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotAcceptRefreshTokenOnProtectedEndpoint() throws Exception {
        Tokens tokens = registerAndLogin("player", "password1");

        mockMvc.perform(get("/users/me/solved-tasks/tangram")
                        .header("Authorization", bearer(tokens.refreshToken())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLoginWithCorrectCredentialsAndRejectWrongPassword() throws Exception {
        register("player", "password1")
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequest("player", "password1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.refreshToken", not(blankOrNullString())));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequest("player", "wrong-password"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRefreshAccessTokenWithValidRefreshToken() throws Exception {
        Tokens tokens = registerAndLogin("player", "password1");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshRequest(tokens.refreshToken()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())));
    }

    @Test
    void shouldNotRefreshWithAccessToken() throws Exception {
        Tokens tokens = registerAndLogin("player", "password1");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshRequest(tokens.accessToken()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotRefreshWithInvalidToken() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshRequest("invalid-token"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotRefreshWithExpiredToken() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshRequest(expiredRefreshToken()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotExposePasswordHashAfterRegistration() throws Exception {
        register("player", "password1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("player"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    private Tokens registerAndLogin(String username, String password) throws Exception {
        register(username, password).andExpect(status().isOk());

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode body = objectMapper.readTree(response);
        return new Tokens(body.get("accessToken").asText(), body.get("refreshToken").asText());
    }

    private ResultActions register(String username, String password) throws Exception {
        return mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(new RegisterUserRequest(username, password))));
    }

    private String expiredRefreshToken() {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("1")
                .claim("username", "player")
                .issuedAt(now.minusSeconds(120))
                .expiresAt(now.minusSeconds(60))
                .claim("token_type", "refresh")
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record Tokens(String accessToken, String refreshToken) {
    }
}

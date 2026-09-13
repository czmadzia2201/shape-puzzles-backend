package org.games.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.games.dto.*;
import org.games.exception.UserNotFoundException;
import org.games.exception.UsernameAlreadyExistsException;
import org.games.model.GameType;
import org.games.model.Task;
import org.games.model.UserData;
import org.games.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.games.GeometryTestHelper.piece;
import static org.games.GeometryTestHelper.point;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldReturnCreatedUser() throws Exception {
        String username = "user1";
        String password = "password1";
        UserData userData = UserData.builder()
                .id(1L)
                .username(username)
                .passwordHash("hashedPassword1")
                .build();
        RegisterUserRequest request = new RegisterUserRequest(username, password);
        when(userService.registerUser(username, password)).thenReturn(userData);
        mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    void shouldNotReturnCreatedUser_usernameConflict() throws Exception {
        String username = "user1";
        String password = "password1";
        RegisterUserRequest request = new RegisterUserRequest(username, password);
        when(userService.registerUser(username, password))
                .thenThrow(new UsernameAlreadyExistsException(username));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username user1 already exists"));
    }

    @Test
    void shouldNotReturnCreatedUser_invalidRequestBody() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest("user1", "");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }

    @Test
    void shouldCheckIfUsernameIsAvailable() throws Exception {
        when(userService.isUsernameAvailable("user1")).thenReturn(true);
        mockMvc.perform(get("/users?username=user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("true"));
    }

    @Test
    void shouldCheckCurrentUser() throws Exception {
        Authentication authentication = getAuthentication(1L);
        mockMvc.perform(get("/users/me")
                        .principal(authentication))
                .andExpect(status().isOk());
        verify(userService).getUserData(authentication);
    }

    @Test
    void shouldDeleteUser() throws Exception {
        Authentication authentication = getAuthentication(1L);
        mockMvc.perform(delete("/users/me")
                        .principal(authentication))
                .andExpect(status().isOk());
        verify(userService).deactivateUser(authentication);
    }

    @Test
    void shouldNotDeleteUser_usernameNotFound() throws Exception {
        Authentication authentication = getAuthentication(1L);
        doThrow(new UserNotFoundException("User ID 1 not found"))
                .when(userService).deactivateUser(authentication);
        mockMvc.perform(delete("/users/me")
                        .principal(authentication))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetUserSolvedTasks() throws Exception {
        Authentication authentication = getAuthentication(1L);
        GameType tangram = new GameType("tangram", "Tangram", Set.of(), Set.of(), List.of(), 50);
        when(userService.getUserSolvedTasks(authentication, "tangram")).thenReturn(Set.of(
                new Task("tg1", tangram, List.of()),
                new Task("tg2", tangram, List.of()),
                new Task("tg3", tangram, List.of())
                ));
        mockMvc.perform(get("/users/me/solved-tasks/tangram")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id")
                        .value(containsInAnyOrder("tg1", "tg2", "tg3")));
    }

    @Test
    void shouldValidateAndSaveSolution() throws Exception {
        Authentication authentication = getAuthentication(1L);
        VerifySolutionRequest request = createSolutionRequest("tg1");
        when(userService.validateAndSaveSolution(authentication, request)).thenReturn(true);
        mockMvc.perform(post("/users/solved-tasks")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("true"));
    }

    @Test
    void shouldNotValidateAndSaveSolution_taskNotFound() throws Exception {
        Authentication authentication = getAuthentication(1L);
        VerifySolutionRequest request = createSolutionRequest("tg1");
        when(userService.validateAndSaveSolution(authentication, request))
                .thenThrow(new EntityNotFoundException("Task tg1 was not found"));
        mockMvc.perform(post("/users/solved-tasks")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task tg1 was not found"));
    }

    @Test
    void shouldNotValidateAndSaveSolution_emptyPieces() throws Exception {
        Authentication authentication = getAuthentication(1L);
        VerifySolutionRequest request = new VerifySolutionRequest("tg1", List.of());
        mockMvc.perform(post("/users/solved-tasks")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }

    @Test
    void shouldNotValidateAndSaveSolution_blankTaskId() throws Exception {
        Authentication authentication = getAuthentication(1L);
        VerifySolutionRequest request = createSolutionRequest("   ");
        mockMvc.perform(post("/users/solved-tasks")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }

    @Test
    void shouldNotValidateAndSaveSolution_missingCoordinate() throws Exception {
        String body = """
        {
          "taskId": "tg1",
          "pieces": [
            {
              "id": "tg01",
              "vertices": [
                {
                  "x": 1.0
                }
              ]
            }
          ]
        }
        """;
        mockMvc.perform(post("/users/solved-tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }

    @Test
    void shouldSyncSolvedTasks() throws Exception {
        Authentication authentication = getAuthentication(1L);
        SyncSolvedTasksRequest request = new SyncSolvedTasksRequest(List.of("t01", "h01", "t05"));
        mockMvc.perform(post("/users/me/solved-tasks/sync")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(userService).syncSolvedTasks(authentication, request.taskIds());
    }

    @Test
    void shouldNotSyncSolvedTasks_nullTaskList() throws Exception {
        Authentication authentication = getAuthentication(1L);
        SyncSolvedTasksRequest request = new SyncSolvedTasksRequest(null);
        mockMvc.perform(post("/users/me/solved-tasks/sync")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }

    private Authentication getAuthentication(Long id) {
        return new UsernamePasswordAuthenticationToken(id.toString(), null, Collections.emptyList());
    }

    private VerifySolutionRequest createSolutionRequest(String taskId) {
        List<PiecePlacement> pieces = List.of(
                piece("piece_1", point(0, 0), point(2, 0), point(2, 4), point(0, 4)),
                piece("piece_2", point(2, 0), point(4, 0), point(4, 4), point(2, 4))
        );
        return new VerifySolutionRequest(taskId, pieces);
    }

}
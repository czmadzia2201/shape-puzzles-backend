package org.games.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.games.dto.SyncSolvedTasksRequest;
import org.games.model.Task;
import org.games.model.UserData;
import org.games.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserData registerUser(@RequestParam String username, @RequestParam String password) {
        return userService.registerUser(username, password);
    }

    @GetMapping("/me/solved-tasks/{gameTypeId}")
    @SecurityRequirement(name = "bearerAuth")
    public Set<Task> findUserSolvedTasks(Authentication authentication, @PathVariable String gameTypeId) {
        return userService.getUserSolvedTasks(authentication, gameTypeId);
    }

    @DeleteMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public void deleteUser(Authentication authentication) {
        userService.deactivateUser(authentication);
    }

    @PostMapping("/me/solved-tasks/sync")
    @SecurityRequirement(name = "bearerAuth")
    public void syncSolvedTasks(Authentication authentication, @RequestBody SyncSolvedTasksRequest request) {
        userService.syncSolvedTasks(authentication, request.taskIds());
    }
}

package org.games.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.games.dto.LoginRequest;
import org.games.dto.LoginResponse;
import org.games.dto.RefreshRequest;
import org.games.dto.RefreshResponse;
import org.games.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@RequestBody @Valid RefreshRequest request) {
        return authService.refresh(request);
    }

}

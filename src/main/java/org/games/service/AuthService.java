package org.games.service;

import lombok.RequiredArgsConstructor;
import org.games.dto.LoginRequest;
import org.games.dto.LoginResponse;
import org.games.dto.RefreshRequest;
import org.games.dto.RefreshResponse;
import org.games.exception.UserNotFoundException;
import org.games.model.UserData;
import org.games.repository.UserDataRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtDecoder refreshJwtDecoder;
    private final UserDataRepository userDataRepository;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        UserData user = userDataRepository.findByUsernameAndActiveTrue(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException(
                        "User %s not found".formatted(authentication.getName())
                ));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken);
    }

    public RefreshResponse refresh(RefreshRequest request) {
        Jwt jwt;

        try {
            jwt = refreshJwtDecoder.decode(request.refreshToken());
        } catch (JwtException ex) {
            throw new BadCredentialsException("Invalid or expired refresh token", ex);
        }

        String tokenType = jwt.getClaimAsString("token_type");
        if (!"refresh".equals(tokenType)) {
            throw new BadCredentialsException("Access token cannot be used as refresh token");
        }

        Long userId = Long.valueOf(jwt.getSubject());
        String username = jwt.getClaimAsString("username");

        UserData user = userDataRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new BadCredentialsException(
                        "Invalid refresh token for user %s".formatted(username)
                ));

        String accessToken = jwtService.generateAccessToken(user);

        return new RefreshResponse(accessToken);
    }
}

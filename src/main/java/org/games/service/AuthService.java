package org.games.service;

import lombok.RequiredArgsConstructor;
import org.games.dto.LoginRequest;
import org.games.dto.LoginResponse;
import org.games.dto.RefreshRequest;
import org.games.dto.RefreshResponse;
import org.games.repository.UserDataRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtDecoder jwtDecoder;
    private final UserDataRepository userDataRepository;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        String accessToken = jwtService.generateAccessToken(authentication.getName());
        String refreshToken = jwtService.generateRefreshToken(authentication.getName());

        return new LoginResponse(accessToken, refreshToken);
    }

    public RefreshResponse refresh(RefreshRequest request) {
        Jwt jwt = jwtDecoder.decode(request.refreshToken());

        String tokenType = jwt.getClaimAsString("token_type");
        if (!"refresh".equals(tokenType)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String username = jwt.getSubject();

        userDataRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User %s not found".formatted(username)
                ));

        String accessToken = jwtService.generateAccessToken(username);

        return new RefreshResponse(accessToken);
    }
}

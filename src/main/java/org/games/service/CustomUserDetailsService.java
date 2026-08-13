package org.games.service;

import lombok.RequiredArgsConstructor;
import org.games.model.UserData;
import org.games.repository.UserDataRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDataRepository userDataRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserData userData = userDataRepository
                .findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("User %s not found".formatted(username)));

        return User.builder()
                .username(userData.getUsername())
                .password(userData.getPasswordHash())
                .authorities(Collections.emptyList())
                .build();
    }

}

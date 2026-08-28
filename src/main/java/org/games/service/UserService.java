package org.games.service;

import lombok.RequiredArgsConstructor;
import org.games.exception.UserNotFoundException;
import org.games.exception.UsernameAlreadyExistsException;
import org.games.model.Task;
import org.games.model.UserData;
import org.games.repository.TaskRepository;
import org.games.repository.UserDataRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDataRepository userDataRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    public UserData registerUser(String username, String password) {
        UserData user = new UserData();
        user.setUsername(username);
        String passwordHash = passwordEncoder.encode(password);
        user.setPasswordHash(passwordHash);
        try {
            return userDataRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UsernameAlreadyExistsException(username);
        }
    }

    public boolean isUsernameAvailable(String username) {
        return userDataRepository.findByUsernameAndActiveTrue(username).isEmpty();
    }

    public Set<Task> getUserSolvedTasks(Authentication authentication, String gameTypeId) {
        Long userId = Long.valueOf(authentication.getName());
        return userDataRepository.findUserSolvedTasksByGameType(userId, gameTypeId);
    }

    public void deactivateUser(Authentication authentication) {
        UserData userData = getUserData(authentication);
        userData.setActive(false);
        userDataRepository.save(userData);
    }

    public void syncSolvedTasks(Authentication authentication, List<String> taskIds) {
        UserData userData = getUserData(authentication);
        Set<Task> solvedTasks = userData.getSolvedTasks();
        List<Task> newlySolvedTasks = taskRepository.findAllById(taskIds);
        solvedTasks.addAll(newlySolvedTasks);
        userDataRepository.save(userData);
    }

    private UserData getUserData(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return userDataRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User ID %s not found".formatted(userId)
                ));
    }
}

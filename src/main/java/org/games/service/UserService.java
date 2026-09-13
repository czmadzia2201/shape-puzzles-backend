package org.games.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.games.dto.GeometryPoint;
import org.games.dto.VerifySolutionRequest;
import org.games.exception.UserNotFoundException;
import org.games.exception.UsernameAlreadyExistsException;
import org.games.model.CoordinateValue;
import org.games.model.Point;
import org.games.model.Task;
import org.games.model.UserData;
import org.games.repository.TaskRepository;
import org.games.repository.UserDataRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDataRepository userDataRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;
    private final SolutionValidator solutionValidator;

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

    @Transactional
    public boolean validateAndSaveSolution(Authentication authentication, VerifySolutionRequest request) {
        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new EntityNotFoundException("Task %s was not found".formatted(request.taskId())));
        List<List<GeometryPoint>> taskPolygons = toGeometryPolygons(task.getPolygons());

        boolean isSolutionCorrect = solutionValidator.validate(taskPolygons, request.pieces());

        if (isSolutionCorrect && isUserAuthenticated(authentication)) {
            UserData userData = getUserData(authentication);
            userData.getSolvedTasks().add(task);
            userDataRepository.save(userData);
        }
        return isSolutionCorrect;
    }

    @Transactional
    public void deactivateUser(Authentication authentication) {
        UserData userData = getUserData(authentication);
        userData.setActive(false);
        userDataRepository.save(userData);
    }

    @Transactional
    public void syncSolvedTasks(Authentication authentication, List<String> taskIds) {
        UserData userData = getUserData(authentication);
        Set<Task> solvedTasks = userData.getSolvedTasks();
        List<Task> newlySolvedTasks = taskRepository.findAllById(taskIds);
        solvedTasks.addAll(newlySolvedTasks);
        userDataRepository.save(userData);
    }

    public UserData getUserData(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return userDataRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User ID %s not found".formatted(userId)
                ));
    }

    private boolean isUserAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private List<List<GeometryPoint>> toGeometryPolygons(List<List<Point>> polygons) {
        return polygons.stream()
                .map(polygon -> polygon.stream()
                        .map(point -> new GeometryPoint(coordinateToNumber(point.x()), coordinateToNumber(point.y())))
                        .toList())
                .toList();
    }

    private double coordinateToNumber(CoordinateValue coordinate) {
        return (coordinate.constant() == null ? 0 : coordinate.constant())
                + (coordinate.sqrt2() == null ? 0 : coordinate.sqrt2()) * Math.sqrt(2)
                + (coordinate.sqrt3() == null ? 0 : coordinate.sqrt3()) * Math.sqrt(3);
    }

}

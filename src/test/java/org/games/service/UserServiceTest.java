package org.games.service;

import org.games.exception.UsernameAlreadyExistsException;
import org.games.model.Task;
import org.games.model.UserData;
import org.games.BaseRepositoryTest;
import org.games.repository.UserDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserServiceTest extends BaseRepositoryTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDataRepository userDataRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void shouldRegisterUser() {
        UserData user = userService.registerUser("user1", "password1");
        assertThat(user).isNotNull();
        UserData savedUser = userDataRepository.findById(user.getId()).get();
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("user1");
        assertThat(savedUser.isActive()).isTrue();
        assertThat(passwordEncoder.matches("password1", savedUser.getPasswordHash())).isTrue();
    }

    @Test
    public void shouldThrowWhenUserExists() {
        userService.registerUser("user1", "password1");
        assertThatThrownBy(() -> userService.registerUser("user1", "password2"))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("Username user1 already exists");
    }

    @Test
    public void shouldRegisterUser_inactiveUsersWithSameUsername() {
        Authentication authentication = getAuthentication("user1");
        UserData user1 = userService.registerUser("user1", "password1");
        userService.deactivateUser(authentication);
        UserData user2 = assertDoesNotThrow(() -> userService.registerUser("user1", "password2"));
        userService.deactivateUser(authentication);
        UserData user3 = assertDoesNotThrow(() -> userService.registerUser("user1", "password3"));

        UserData savedUser1 = userDataRepository.findById(user1.getId()).get();
        UserData savedUser2 = userDataRepository.findById(user2.getId()).get();
        UserData savedUser3 = userDataRepository.findById(user3.getId()).get();

        assertThat(savedUser1.getUsername()).isEqualTo("user1");
        assertThat(savedUser2.getUsername()).isEqualTo("user1");
        assertThat(savedUser3.getUsername()).isEqualTo("user1");

        assertThat(savedUser1.isActive()).isFalse();
        assertThat(savedUser2.isActive()).isFalse();
        assertThat(savedUser3.isActive()).isTrue();
    }

    @Test
    public void shouldDeactivateUser() {
        UserData user = userService.registerUser("user1", "password1");
        userService.deactivateUser(getAuthentication("user1"));
        UserData savedUser = userDataRepository.findById(user.getId()).get();
        assertThat(savedUser.isActive()).isFalse();
    }

    @Test
    public void shouldThrowWhenUserDoesNotExist() {
        assertThatThrownBy(() -> userService.deactivateUser(getAuthentication("user1")))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Username user1 not found");
    }

    @Test
    public void shouldThrowWhenUserIsNotActive() {
        Authentication authentication = getAuthentication("user1");
        userService.registerUser("user1", "password1");
        userService.deactivateUser(authentication);
        assertThatThrownBy(() -> userService.deactivateUser(authentication))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Username user1 not found");
    }

    @Transactional
    @ParameterizedTest
    @MethodSource("userSolvedTasksData")
    @Sql(
            scripts = "/db/userSolvedTasks.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    public void shouldFindUserSolvedTasks(String username, String gameTypeId, int size, List<String> taskIds) {
        Set<Task> userTasks = userService.getUserSolvedTasks(getAuthentication(username), gameTypeId);
        assertThat(userTasks).isNotNull();
        assertThat(userTasks).hasSize(size);
        assertThat(userTasks).extracting(Task::getId).containsExactlyInAnyOrderElementsOf(taskIds);
    }

    private static Stream<Arguments> userSolvedTasksData() {
        return Stream.of(
                Arguments.of("user1", "tangram", 2, List.of("tangram_001", "tangram_002")),
                Arguments.of("user1", "house", 1, List.of("house_001")),
                Arguments.of("user2", "tangram", 1, List.of("tangram_003")),
                Arguments.of("user2", "house", 1, List.of("house_002")),
                Arguments.of("user2", "t", 0, List.of())
        );
    }

    @Transactional
    @ParameterizedTest
    @MethodSource("userSyncedTasksData")
    @Sql(
            scripts = "/db/userSolvedTasks.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    void shouldSyncSolvedTasks(String gameTypeId, int size, List<String> taskIds) {
        Authentication authentication = getAuthentication("user1");
        userService.syncSolvedTasks(authentication, List.of("tangram_003", "tangram_002", "tangram_003", "house_001", "house_003", "t_001"));
        Set<Task> userTasks = userService.getUserSolvedTasks(authentication, gameTypeId);
        assertThat(userTasks).isNotNull();
        assertThat(userTasks).hasSize(size);
        assertThat(userTasks).extracting(Task::getId).containsExactlyInAnyOrderElementsOf(taskIds);
    }

    private static Stream<Arguments> userSyncedTasksData() {
        return Stream.of(
                Arguments.of("tangram", 3, List.of("tangram_001", "tangram_002", "tangram_003")),
                Arguments.of("house", 1, List.of("house_001")),
                Arguments.of("t", 1, List.of("t_001"))
        );
    }

    private Authentication getAuthentication(String username) {
        return new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
    }

}
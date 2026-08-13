package org.games.repository;

import org.games.model.UserData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserDataRepositoryTest {

    @Autowired
    private UserDataRepository repository;

    @Test
    void shouldSaveAndReadUser() {
        UserData user = new UserData();
        user.setUsername("madzia1");
        user.setPasswordHash("test_hash");
//        user.setActive(true);

        UserData saved = repository.save(user);

        Optional<UserData> result = repository.findById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals("madzia1", result.get().getUsername());
    }

}
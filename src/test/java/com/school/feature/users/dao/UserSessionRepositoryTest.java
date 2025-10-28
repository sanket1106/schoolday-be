package com.school.feature.users.dao;

import com.school.BaseRepositoryTest;
import com.school.authentication.TokenUtils;
import com.school.feature.users.entity.User;
import com.school.feature.users.entity.UserSession;
import com.school.feature.users.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserSessionRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private IUserSessionRepository userSessionRepository;

    @Autowired
    private IUserRepository userRepository;

    @Test
    public void given_userSession_when_save_then_returnsSavedUserSession() {
        // Given
        User user = User.builder()
                .email("session@example.com")
                .firstName("Session")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        String token = TokenUtils.generateToken(32);
        UserSession userSession = UserSession.builder()
                .token(token)
                .user(savedUser)
                .active(true)
                .build();

        // When
        UserSession savedUserSession = userSessionRepository.save(userSession);

        // Then
        assertNotNull(savedUserSession);
        assertEquals(token, savedUserSession.getToken());
        assertEquals(savedUser.getId(), savedUserSession.getUser().getId());
        assertTrue(savedUserSession.isActive());
    }

    @Test
    public void given_savedUserSession_when_findById_then_returnsUserSession() {
        // Given
        User user = User.builder()
                .email("findsession@example.com")
                .firstName("Find")
                .lastName("Session")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        String token = TokenUtils.generateToken(32);
        UserSession userSession = UserSession.builder()
                .token(token)
                .user(savedUser)
                .active(true)
                .build();
        UserSession savedUserSession = userSessionRepository.save(userSession);

        // When
        Optional<UserSession> foundUserSession = userSessionRepository.findById(savedUserSession.getToken());

        // Then
        assertTrue(foundUserSession.isPresent());
        assertEquals(token, foundUserSession.get().getToken());
        assertEquals(savedUser.getId(), foundUserSession.get().getUser().getId());
        assertTrue(foundUserSession.get().isActive());
    }

    @Test
    public void given_savedUserSession_when_findByUserIdAndActive_then_returnsUserSession() {
        // Given
        User user = User.builder()
                .email("active@example.com")
                .firstName("Active")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        String token = TokenUtils.generateToken(32);
        UserSession userSession = UserSession.builder()
                .token(token)
                .user(savedUser)
                .active(true)
                .build();
        userSessionRepository.save(userSession);

        // When
        UserSession foundUserSession = userSessionRepository.findByUserIdAndActive(savedUser.getId(), true);

        // Then
        assertNotNull(foundUserSession);
        assertEquals(token, foundUserSession.getToken());
        assertEquals(savedUser.getId(), foundUserSession.getUser().getId());
        assertTrue(foundUserSession.isActive());
    }

    @Test
    public void given_inactiveUserSession_when_findByUserIdAndActive_then_returnsNull() {
        // Given
        User user = User.builder()
                .email("inactive@example.com")
                .firstName("Inactive")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        String token = TokenUtils.generateToken(32);
        UserSession userSession = UserSession.builder()
                .token(token)
                .user(savedUser)
                .active(false)
                .build();
        userSessionRepository.save(userSession);

        // When
        UserSession foundUserSession = userSessionRepository.findByUserIdAndActive(savedUser.getId(), true);

        // Then
        assertNull(foundUserSession);
    }

    @Test
    public void given_savedUserSessions_when_findAll_then_returnsAllUserSessions() {
        // Given
        User user1 = User.builder()
                .email("session1@example.com")
                .firstName("Session1")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedUser1 = userRepository.save(user1);

        User user2 = User.builder()
                .email("session2@example.com")
                .firstName("Session2")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedUser2 = userRepository.save(user2);

        String token1 = TokenUtils.generateToken(32);
        String token2 = TokenUtils.generateToken(32);
        UserSession userSession1 = UserSession.builder()
                .token(token1)
                .user(savedUser1)
                .active(true)
                .build();
        UserSession userSession2 = UserSession.builder()
                .token(token2)
                .user(savedUser2)
                .active(false)
                .build();

        userSessionRepository.save(userSession1);
        userSessionRepository.save(userSession2);

        // When
        List<UserSession> allUserSessions = userSessionRepository.findAll();

        // Then
        assertTrue(allUserSessions.size() >= 2);
        assertTrue(allUserSessions.stream().anyMatch(us -> us.getToken().equals(token1)));
        assertTrue(allUserSessions.stream().anyMatch(us -> us.getToken().equals(token2)));
    }

    @Test
    public void given_savedUserSession_when_update_then_returnsUpdatedUserSession() {
        // Given
        User user = User.builder()
                .email("update@example.com")
                .firstName("Update")
                .lastName("Session")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        String token = TokenUtils.generateToken(32);
        UserSession userSession = UserSession.builder()
                .token(token)
                .user(savedUser)
                .active(true)
                .build();
        UserSession savedUserSession = userSessionRepository.save(userSession);

        // When
        savedUserSession.setActive(false);
        UserSession updatedUserSession = userSessionRepository.save(savedUserSession);

        // Then
        assertFalse(updatedUserSession.isActive());
        assertEquals(token, updatedUserSession.getToken());
        assertEquals(savedUser.getId(), updatedUserSession.getUser().getId());
    }

    @Test
    public void given_savedUserSession_when_delete_then_userSessionIsRemoved() {
        // Given
        User user = User.builder()
                .email("delete@example.com")
                .firstName("Delete")
                .lastName("Session")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        String token = TokenUtils.generateToken(32);
        UserSession userSession = UserSession.builder()
                .token(token)
                .user(savedUser)
                .active(true)
                .build();
        UserSession savedUserSession = userSessionRepository.save(userSession);

        // When
        userSessionRepository.deleteById(token);

        // Then
        Optional<UserSession> deletedUserSession = userSessionRepository.findById(token);
        assertFalse(deletedUserSession.isPresent());
    }

    @Test
    public void given_nonExistentUserId_when_findByUserIdAndActive_then_returnsNull() {
        // When
        UserSession foundUserSession = userSessionRepository.findByUserIdAndActive("non-existent-user-id", true);

        // Then
        assertNull(foundUserSession);
    }

    @Test
    public void given_nonExistentToken_when_findById_then_returnsEmpty() {
        // When
        Optional<UserSession> foundUserSession = userSessionRepository.findById("non-existent-token");

        // Then
        assertFalse(foundUserSession.isPresent());
    }
} 
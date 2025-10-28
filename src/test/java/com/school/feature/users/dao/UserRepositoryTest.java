package com.school.feature.users.dao;

import com.school.BaseRepositoryTest;
import com.school.feature.users.entity.User;
import com.school.feature.users.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private IUserRepository userRepository;

    @Test
    public void given_user_when_findByEmail_then_returnsUser() {
        final var user = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .password("password")
                .lastName("User")
                .userStatus(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        final var savedUserOptional = userRepository.findByEmail("test@example.com");
        assertTrue(savedUserOptional.isPresent());

        final var savedUser = savedUserOptional.get();
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("Test", savedUser.getFirstName());
        assertEquals("User", savedUser.getLastName());
        assertEquals(UserStatus.ACTIVE, savedUser.getUserStatus());
    }

    @Test
    public void given_user_when_save_then_returnsSavedUser() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("Test", savedUser.getFirstName());
        assertEquals("User", savedUser.getLastName());
        assertEquals(UserStatus.ACTIVE, savedUser.getUserStatus());
    }

    @Test
    public void given_savedUser_when_findById_then_returnsUser() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
        assertEquals("Test", foundUser.get().getFirstName());
        assertEquals("User", foundUser.get().getLastName());
    }

    @Test
    public void given_savedUsers_when_findAll_then_returnsAllUsers() {
        // Given
        User user1 = User.builder()
                .email("test1@example.com")
                .firstName("Test1")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User user2 = User.builder()
                .email("test2@example.com")
                .firstName("Test2")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        // When
        List<User> allUsers = userRepository.findAll();

        // Then
        assertTrue(allUsers.size() >= 2);
        assertTrue(allUsers.stream().anyMatch(u -> u.getEmail().equals("test1@example.com")));
        assertTrue(allUsers.stream().anyMatch(u -> u.getEmail().equals("test2@example.com")));
    }

    @Test
    public void given_savedUser_when_update_then_returnsUpdatedUser() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        // When
        savedUser.setFirstName("Updated");
        savedUser.setLastName("Name");
        savedUser.setUserStatus(UserStatus.DISABLED);
        User updatedUser = userRepository.save(savedUser);

        // Then
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
        assertEquals(UserStatus.DISABLED, updatedUser.getUserStatus());
        assertEquals("test@example.com", updatedUser.getEmail());
    }

    @Test
    public void given_savedUser_when_delete_then_userIsRemoved() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        // When
        userRepository.deleteById(savedUser.getId());

        // Then
        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertFalse(deletedUser.isPresent());
    }

    @Test
    public void given_userWithInactiveStatus_when_save_then_savesSuccessfully() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.DISABLED)
                .build();

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertNotNull(savedUser);
        assertEquals(UserStatus.DISABLED, savedUser.getUserStatus());
    }

    @Test
    public void given_nonExistentEmail_when_findByEmail_then_returnsEmpty() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    public void given_nonExistentId_when_findById_then_returnsEmpty() {
        // When
        Optional<User> foundUser = userRepository.findById("non-existent-id");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    public void given_duplicateEmail_when_save_then_throwsException() {
        // Given
        User user1 = User.builder()
                .email("duplicate@example.com")
                .firstName("First")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        userRepository.save(user1);

        User user2 = User.builder()
                .email("duplicate@example.com")
                .firstName("Second")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();

        // When & Then
        assertThrows(Exception.class, () -> userRepository.save(user2));
    }
}
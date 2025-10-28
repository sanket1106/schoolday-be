package com.school.service;

import com.school.BaseServiceTest;
import com.school.authentication.TokenUtils;
import com.school.exceptions.InvalidCredentialsException;
import com.school.feature.users.dao.IRoleRepository;
import com.school.feature.users.dao.IUserRepository;
import com.school.feature.users.dao.IUserRoleRepository;
import com.school.feature.users.dao.IUserSessionRepository;
import com.school.feature.users.entity.*;
import com.school.web.dtos.LogInLogOutDTO;
import com.school.web.dtos.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest extends BaseServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private IUserSessionRepository userSessionRepository;

    @Autowired
    private IUserRoleRepository userRoleRepository;

    private User testUser;
    private Role adminRole;
    private Role parentRole;

    @BeforeEach
    void setUp() {
        // Get existing admin role from base data
        adminRole = roleRepository.findByName("ADMIN");
        assertNotNull(adminRole, "ADMIN role should exist in base data");

        // Get existing parent role from base data
        parentRole = roleRepository.findByName("PARENT");
        assertNotNull(parentRole, "PARENT role should exist in base data");

        // Create test user with admin role
        testUser = User.builder()
                .email("admin@test.com")
                .firstName("Admin")
                .lastName("User")
                .password(BCrypt.hashpw("password", BCrypt.gensalt()))
                .userStatus(UserStatus.ACTIVE)
                .build();
        testUser = userRepository.save(testUser);

        // Assign admin role to test user
        UserRole userRole = UserRole.builder()
                .id(UserRoleId.builder()
                        .userId(testUser.getId())
                        .roleId(adminRole.getId())
                        .build())
                .user(testUser)
                .role(adminRole)
                .build();
        userRoleRepository.save(userRole);
    }

    @Test
    public void given_validEmail_when_getUserByEmail_then_returnsUser() {
        // When
        User foundUser = userService.getUserByEmail("admin@test.com");

        // Then
        assertNotNull(foundUser);
        assertEquals("admin@test.com", foundUser.getEmail());
        assertEquals("Admin", foundUser.getFirstName());
        assertEquals("User", foundUser.getLastName());
    }

    @Test
    public void given_invalidEmail_when_getUserByEmail_then_returnsNull() {
        // When
        User foundUser = userService.getUserByEmail("nonexistent@test.com");

        // Then
        assertNull(foundUser);
    }

    @Test
    public void given_validCredentials_when_validateUser_then_returnsUserSession() throws InvalidCredentialsException {
        // Given
        LogInLogOutDTO loginDTO = new LogInLogOutDTO();
        loginDTO.setEmail("admin@test.com");
        loginDTO.setPassword("password");

        // When
        UserSession userSession = userService.validateUser(loginDTO);

        // Then
        assertNotNull(userSession);
        assertTrue(userSession.isActive());
        assertEquals(testUser.getId(), userSession.getUser().getId());
        assertNotNull(userSession.getToken());
        assertEquals(32, userSession.getToken().length());
    }

    @Test
    public void given_invalidEmail_when_validateUser_then_throwsException() {
        // Given
        LogInLogOutDTO loginDTO = new LogInLogOutDTO();
        loginDTO.setEmail("wrong@test.com");
        loginDTO.setPassword("password");

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> userService.validateUser(loginDTO));
    }

    @Test
    public void given_invalidPassword_when_validateUser_then_throwsException() {
        // Given
        LogInLogOutDTO loginDTO = new LogInLogOutDTO();
        loginDTO.setEmail("admin@test.com");
        loginDTO.setPassword("wrongpassword");

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> userService.validateUser(loginDTO));
    }

    @Test
    public void given_existingActiveSession_when_validateUser_then_returnsExistingSession() throws InvalidCredentialsException {
        // Given
        String token = TokenUtils.generateToken(32);
        UserSession existingSession = UserSession.builder()
                .token(token)
                .user(testUser)
                .active(true)
                .build();
        userSessionRepository.save(existingSession);

        LogInLogOutDTO loginDTO = new LogInLogOutDTO();
        loginDTO.setEmail("admin@test.com");
        loginDTO.setPassword("password");

        // When
        UserSession userSession = userService.validateUser(loginDTO);

        // Then
        assertNotNull(userSession);
        assertEquals(token, userSession.getToken());
        assertTrue(userSession.isActive());
    }

    @Test
    public void given_validToken_when_invalidateSession_then_deactivatesSession() {
        // Given
        String token = TokenUtils.generateToken(32);
        UserSession userSession = UserSession.builder()
                .token(token)
                .user(testUser)
                .active(true)
                .build();
        userSessionRepository.save(userSession);

        // When
        boolean result = userService.invalidateSession(token);

        // Then
        assertTrue(result);
        Optional<UserSession> foundSession = userSessionRepository.findById(token);
        assertTrue(foundSession.isPresent());
        assertFalse(foundSession.get().isActive());
    }

    @Test
    public void given_nonExistentToken_when_invalidateSession_then_returnsTrue() {
        // When
        boolean result = userService.invalidateSession("non-existent-token");

        // Then
        assertTrue(result);
    }

    @Test
    public void given_inactiveSession_when_invalidateSession_then_returnsTrue() {
        // Given
        String token = TokenUtils.generateToken(32);
        UserSession userSession = UserSession.builder()
                .token(token)
                .user(testUser)
                .active(false)
                .build();
        userSessionRepository.save(userSession);

        // When
        boolean result = userService.invalidateSession(token);

        // Then
        assertTrue(result);
    }

    @Test
    public void given_adminUser_when_addParent_then_returnsCreatedParent() {
        // Given
        UserDTO parentDTO = UserDTO.builder()
                .email("newparent@test.com")
                .firstName("New")
                .lastName("Parent")
                .build();

        // When
        User createdParent = userService.addParent(testUser, parentDTO);

        // Then
        assertNotNull(createdParent);
        assertEquals("newparent@test.com", createdParent.getEmail());
        assertEquals("New", createdParent.getFirstName());
        assertEquals("Parent", createdParent.getLastName());
        assertEquals(UserStatus.ACTIVE, createdParent.getUserStatus());
        assertEquals("password", createdParent.getPassword());

        // Verify parent role was assigned
        UserRoleId userRoleId = UserRoleId.builder()
                .userId(createdParent.getId())
                .roleId(parentRole.getId())
                .build();
        Optional<UserRole> userRole = userRoleRepository.findById(userRoleId);
        assertTrue(userRole.isPresent());
        assertEquals(parentRole.getId(), userRole.get().getRole().getId());
    }

    @Test
    public void given_nonAdminUser_when_addParent_then_returnsNull() {
        // Given
        User nonAdminUser = User.builder()
                .email("nonadmin@test.com")
                .firstName("Non")
                .lastName("Admin")
                .password(BCrypt.hashpw("password", BCrypt.gensalt()))
                .userStatus(UserStatus.ACTIVE)
                .build();
        nonAdminUser = userRepository.save(nonAdminUser);

        UserDTO parentDTO = UserDTO.builder()
                .email("parent@test.com")
                .firstName("Test")
                .lastName("Parent")
                .build();

        // When
        User createdParent = userService.addParent(nonAdminUser, parentDTO);

        // Then
        assertNull(createdParent);
    }

    @Test
    public void given_adminUser_when_addParentWithExistingEmail_then_throwsException() {
        // Given
        User existingUser = User.builder()
                .email("existing@test.com")
                .firstName("Existing")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        userRepository.save(existingUser);

        UserDTO parentDTO = UserDTO.builder()
                .email("existing@test.com")
                .firstName("New")
                .lastName("Parent")
                .build();

        // When & Then
        assertThrows(Exception.class, () -> userService.addParent(testUser, parentDTO));
    }
} 
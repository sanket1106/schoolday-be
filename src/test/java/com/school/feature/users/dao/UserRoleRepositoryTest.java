package com.school.feature.users.dao;

import com.school.BaseRepositoryTest;
import com.school.feature.users.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserRoleRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private IUserRoleRepository userRoleRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Test
    public void given_userRole_when_save_then_returnsSavedUserRole() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        Role role = Role.builder()
                .name("USERROLE_TEACHER")
                .status(UserRoleStatus.ENABLED)
                .build();
        Role savedRole = roleRepository.save(role);

        UserRoleId userRoleId = UserRoleId.builder()
                .userId(savedUser.getId())
                .roleId(savedRole.getId())
                .build();

        UserRole userRole = UserRole.builder()
                .id(userRoleId)
                .user(savedUser)
                .role(savedRole)
                .build();

        // When
        UserRole savedUserRole = userRoleRepository.save(userRole);

        // Then
        assertNotNull(savedUserRole);
        assertEquals(savedUser.getId(), savedUserRole.getId().getUserId());
        assertEquals(savedRole.getId(), savedUserRole.getId().getRoleId());
        assertEquals(savedUser, savedUserRole.getUser());
        assertEquals(savedRole, savedUserRole.getRole());
    }

    @Test
    public void given_savedUserRole_when_findById_then_returnsUserRole() {
        // Given
        User user = User.builder()
                .email("find@example.com")
                .firstName("Find")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        Role role = Role.builder()
                .name("USERROLE_ADMIN")
                .status(UserRoleStatus.ENABLED)
                .build();
        Role savedRole = roleRepository.save(role);

        UserRoleId userRoleId = UserRoleId.builder()
                .userId(savedUser.getId())
                .roleId(savedRole.getId())
                .build();

        UserRole userRole = UserRole.builder()
                .id(userRoleId)
                .user(savedUser)
                .role(savedRole)
                .build();
        UserRole savedUserRole = userRoleRepository.save(userRole);

        // When
        Optional<UserRole> foundUserRole = userRoleRepository.findById(userRoleId);

        // Then
        assertTrue(foundUserRole.isPresent());
        assertEquals(savedUser.getId(), foundUserRole.get().getId().getUserId());
        assertEquals(savedRole.getId(), foundUserRole.get().getId().getRoleId());
    }

    @Test
    public void given_savedUserRoles_when_findAll_then_returnsAllUserRoles() {
        // Given
        User user1 = User.builder()
                .email("user1@example.com")
                .firstName("User1")
                .lastName("Test")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedUser1 = userRepository.save(user1);

        User user2 = User.builder()
                .email("user2@example.com")
                .firstName("User2")
                .lastName("Test")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedUser2 = userRepository.save(user2);

        Role role1 = Role.builder()
                .name("USERROLE_TEACHER")
                .status(UserRoleStatus.ENABLED)
                .build();
        Role savedRole1 = roleRepository.save(role1);

        Role role2 = Role.builder()
                .name("USERROLE_PARENT")
                .status(UserRoleStatus.ENABLED)
                .build();
        Role savedRole2 = roleRepository.save(role2);

        UserRole userRole1 = UserRole.builder()
                .id(UserRoleId.builder()
                        .userId(savedUser1.getId())
                        .roleId(savedRole1.getId())
                        .build())
                .user(savedUser1)
                .role(savedRole1)
                .build();

        UserRole userRole2 = UserRole.builder()
                .id(UserRoleId.builder()
                        .userId(savedUser2.getId())
                        .roleId(savedRole2.getId())
                        .build())
                .user(savedUser2)
                .role(savedRole2)
                .build();

        userRoleRepository.save(userRole1);
        userRoleRepository.save(userRole2);

        // When
        List<UserRole> allUserRoles = userRoleRepository.findAll();

        // Then
        assertTrue(allUserRoles.size() >= 2);
        assertTrue(allUserRoles.stream().anyMatch(ur -> ur.getUser().getId().equals(savedUser1.getId())));
        assertTrue(allUserRoles.stream().anyMatch(ur -> ur.getUser().getId().equals(savedUser2.getId())));
    }

    @Test
    public void given_savedUserRole_when_delete_then_userRoleIsRemoved() {
        // Given
        User user = User.builder()
                .email("delete@example.com")
                .firstName("Delete")
                .lastName("User")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        Role role = Role.builder()
                .name("USERROLE_STUDENT")
                .status(UserRoleStatus.ENABLED)
                .build();
        Role savedRole = roleRepository.save(role);

        UserRoleId userRoleId = UserRoleId.builder()
                .userId(savedUser.getId())
                .roleId(savedRole.getId())
                .build();

        UserRole userRole = UserRole.builder()
                .id(userRoleId)
                .user(savedUser)
                .role(savedRole)
                .build();
        userRoleRepository.save(userRole);

        // When
        userRoleRepository.deleteById(userRoleId);

        // Then
        Optional<UserRole> deletedUserRole = userRoleRepository.findById(userRoleId);
        assertFalse(deletedUserRole.isPresent());
    }

    @Test
    public void given_nonExistentUserRoleId_when_findById_then_returnsEmpty() {
        // Given
        UserRoleId nonExistentId = UserRoleId.builder()
                .userId("non-existent-user-id")
                .roleId("non-existent-role-id")
                .build();

        // When
        Optional<UserRole> foundUserRole = userRoleRepository.findById(nonExistentId);

        // Then
        assertFalse(foundUserRole.isPresent());
    }
} 
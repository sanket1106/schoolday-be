package com.school.feature.users.dao;

import com.school.BaseRepositoryTest;
import com.school.feature.users.entity.Role;
import com.school.feature.users.entity.UserRoleStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RoleRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private IRoleRepository roleRepository;

    @Test
    public void given_role_when_save_then_returnsSavedRole() {
        // Given
        Role role = Role.builder()
                .name("TEST_TEACHER")
                .status(UserRoleStatus.ENABLED)
                .permissions("MANAGE_STUDENTS,VIEW_GRADES")
                .build();

        // When
        Role savedRole = roleRepository.save(role);

        // Then
        assertNotNull(savedRole);
        assertNotNull(savedRole.getId());
        assertEquals("TEST_TEACHER", savedRole.getName());
        assertEquals(UserRoleStatus.ENABLED, savedRole.getStatus());
        assertEquals("MANAGE_STUDENTS,VIEW_GRADES", savedRole.getPermissions());
    }

    @Test
    public void given_savedRole_when_findById_then_returnsRole() {
        // Given
        Role role = Role.builder()
                .name("TEST_ADMIN")
                .status(UserRoleStatus.ENABLED)
                .permissions("ALL_PERMISSIONS")
                .build();
        Role savedRole = roleRepository.save(role);

        // When
        Optional<Role> foundRole = roleRepository.findById(savedRole.getId());

        // Then
        assertTrue(foundRole.isPresent());
        assertEquals("TEST_ADMIN", foundRole.get().getName());
        assertEquals(UserRoleStatus.ENABLED, foundRole.get().getStatus());
    }

    @Test
    public void given_savedRole_when_findByName_then_returnsRole() {
        // Given
        Role role = Role.builder()
                .name("FIND_PARENT")
                .status(UserRoleStatus.ENABLED)
                .permissions("VIEW_CHILD_GRADES")
                .build();
        roleRepository.save(role);

        // When
        Role foundRole = roleRepository.findByName("FIND_PARENT");

        // Then
        assertNotNull(foundRole);
        assertEquals("FIND_PARENT", foundRole.getName());
        assertEquals(UserRoleStatus.ENABLED, foundRole.getStatus());
    }

    @Test
    public void given_savedRoles_when_findAll_then_returnsAllRoles() {
        // Given
        Role role1 = Role.builder()
                .name("STUDENT")
                .status(UserRoleStatus.ENABLED)
                .permissions("VIEW_OWN_GRADES")
                .build();
        Role role2 = Role.builder()
                .name("PRINCIPAL")
                .status(UserRoleStatus.ENABLED)
                .permissions("MANAGE_SCHOOL")
                .build();

        roleRepository.save(role1);
        roleRepository.save(role2);

        // When
        List<Role> allRoles = roleRepository.findAll();

        // Then
        assertTrue(allRoles.size() >= 2);
        assertTrue(allRoles.stream().anyMatch(r -> r.getName().equals("STUDENT")));
        assertTrue(allRoles.stream().anyMatch(r -> r.getName().equals("PRINCIPAL")));
    }

    @Test
    public void given_savedRole_when_update_then_returnsUpdatedRole() {
        // Given
        Role role = Role.builder()
                .name("COUNSELOR")
                .status(UserRoleStatus.ENABLED)
                .permissions("VIEW_STUDENT_RECORDS")
                .build();
        Role savedRole = roleRepository.save(role);

        // When
        savedRole.setStatus(UserRoleStatus.DISABLED);
        savedRole.setPermissions("VIEW_STUDENT_RECORDS,MANAGE_SCHEDULES");
        Role updatedRole = roleRepository.save(savedRole);

        // Then
        assertEquals(UserRoleStatus.DISABLED, updatedRole.getStatus());
        assertEquals("VIEW_STUDENT_RECORDS,MANAGE_SCHEDULES", updatedRole.getPermissions());
        assertEquals("COUNSELOR", updatedRole.getName());
    }

    @Test
    public void given_savedRole_when_delete_then_roleIsRemoved() {
        // Given
        Role role = Role.builder()
                .name("LIBRARIAN")
                .status(UserRoleStatus.ENABLED)
                .permissions("MANAGE_BOOKS")
                .build();
        Role savedRole = roleRepository.save(role);

        // When
        roleRepository.deleteById(savedRole.getId());

        // Then
        Optional<Role> deletedRole = roleRepository.findById(savedRole.getId());
        assertFalse(deletedRole.isPresent());
    }

    @Test
    public void given_roleWithDisabledStatus_when_save_then_savesSuccessfully() {
        // Given
        Role role = Role.builder()
                .name("GUEST")
                .status(UserRoleStatus.DISABLED)
                .permissions("VIEW_PUBLIC_INFO")
                .build();

        // When
        Role savedRole = roleRepository.save(role);

        // Then
        assertNotNull(savedRole);
        assertEquals(UserRoleStatus.DISABLED, savedRole.getStatus());
    }

    @Test
    public void given_nonExistentRoleName_when_findByName_then_returnsNull() {
        // When
        Role foundRole = roleRepository.findByName("NON_EXISTENT_ROLE");

        // Then
        assertNull(foundRole);
    }
} 
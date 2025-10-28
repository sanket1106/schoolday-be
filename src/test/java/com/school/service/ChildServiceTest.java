package com.school.service;

import com.school.BaseServiceTest;
import com.school.feature.users.dao.IChildRepository;
import com.school.feature.users.dao.IParentChildRepository;
import com.school.feature.users.dao.IRoleRepository;
import com.school.feature.users.dao.IUserRepository;
import com.school.feature.users.dao.IUserRoleRepository;
import com.school.feature.users.entity.*;
import com.school.web.dtos.AddChildDTO;
import com.school.web.dtos.ChildDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChildServiceTest extends BaseServiceTest {

    @Autowired
    private ChildService childService;

    @Autowired
    private IChildRepository childRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IParentChildRepository parentChildRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private IUserRoleRepository userRoleRepository;

    private User adminUser;
    private User parentUser;
    private Role adminRole;
    private Role parentRole;

    @BeforeEach
    void setUp() {
        // Get existing roles from base data
        adminRole = roleRepository.findByName("ADMIN");
        assertNotNull(adminRole, "ADMIN role should exist in base data");

        parentRole = roleRepository.findByName("PARENT");
        assertNotNull(parentRole, "PARENT role should exist in base data");

        // Create admin user
        adminUser = User.builder()
                .email("admin@test.com")
                .firstName("Admin")
                .lastName("User")
                .password(BCrypt.hashpw("password", BCrypt.gensalt()))
                .userStatus(UserStatus.ACTIVE)
                .build();
        adminUser = userRepository.save(adminUser);

        // Create parent user
        parentUser = User.builder()
                .email("parent@test.com")
                .firstName("Parent")
                .lastName("User")
                .password(BCrypt.hashpw("password", BCrypt.gensalt()))
                .userStatus(UserStatus.ACTIVE)
                .build();
        parentUser = userRepository.save(parentUser);

        // Assign roles
        UserRole adminUserRole = UserRole.builder()
                .id(UserRoleId.builder()
                        .userId(adminUser.getId())
                        .roleId(adminRole.getId())
                        .build())
                .user(adminUser)
                .role(adminRole)
                .build();
        userRoleRepository.save(adminUserRole);

        UserRole parentUserRole = UserRole.builder()
                .id(UserRoleId.builder()
                        .userId(parentUser.getId())
                        .roleId(parentRole.getId())
                        .build())
                .user(parentUser)
                .role(parentRole)
                .build();
        userRoleRepository.save(parentUserRole);
    }

    @Test
    public void given_adminUser_when_addChild_then_returnsChildDTO() {
        // Given
        AddChildDTO addChildDTO = AddChildDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(2015, 5, 15))
                .parents(List.of(
                        AddChildDTO.ParentInfo.builder()
                                .parentId(parentUser.getId())
                                .relation("Father")
                                .build()
                ))
                .build();

        // When
        ChildDTO childDTO = childService.addChild(adminUser, addChildDTO);

        // Then
        assertNotNull(childDTO);
        assertEquals("John", childDTO.getFirstName());
        assertEquals("Doe", childDTO.getLastName());
        assertEquals(LocalDate.of(2015, 5, 15), childDTO.getDateOfBirth());
        assertEquals(UserStatus.ACTIVE, childDTO.getStatus());

        // Verify child was saved
        List<Child> children = childRepository.findAll();
        assertTrue(children.stream().anyMatch(c -> c.getFirstName().equals("John")));

        // Verify parent-child relationship was created
        List<ParentChild> parentChildRelations = parentChildRepository.findByParentId(parentUser.getId());
        assertEquals(1, parentChildRelations.size());
        assertEquals("Father", parentChildRelations.get(0).getRelation());
    }

    @Test
    public void given_nonAdminUser_when_addChild_then_returnsNull() {
        // Given
        AddChildDTO addChildDTO = AddChildDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(2016, 8, 20))
                .parents(List.of(
                        AddChildDTO.ParentInfo.builder()
                                .parentId(parentUser.getId())
                                .relation("Mother")
                                .build()
                ))
                .build();

        // When
        ChildDTO childDTO = childService.addChild(parentUser, addChildDTO);

        // Then
        assertNull(childDTO);
    }

    @Test
    public void given_adminUser_when_addChildWithMultipleParents_then_createsAllRelationships() {
        // Given
        User secondParent = User.builder()
                .email("parent2@test.com")
                .firstName("Second")
                .lastName("Parent")
                .password(BCrypt.hashpw("password", BCrypt.gensalt()))
                .userStatus(UserStatus.ACTIVE)
                .build();
        secondParent = userRepository.save(secondParent);

        UserRole secondParentRole = UserRole.builder()
                .id(UserRoleId.builder()
                        .userId(secondParent.getId())
                        .roleId(parentRole.getId())
                        .build())
                .user(secondParent)
                .role(parentRole)
                .build();
        userRoleRepository.save(secondParentRole);

        AddChildDTO addChildDTO = AddChildDTO.builder()
                .firstName("Multi")
                .lastName("Parent")
                .dateOfBirth(LocalDate.of(2017, 3, 10))
                .parents(List.of(
                        AddChildDTO.ParentInfo.builder()
                                .parentId(parentUser.getId())
                                .relation("Father")
                                .build(),
                        AddChildDTO.ParentInfo.builder()
                                .parentId(secondParent.getId())
                                .relation("Mother")
                                .build()
                ))
                .build();

        // When
        ChildDTO childDTO = childService.addChild(adminUser, addChildDTO);

        // Then
        assertNotNull(childDTO);
        assertEquals("Multi", childDTO.getFirstName());

        // Verify both parent-child relationships were created
        List<ParentChild> parent1Relations = parentChildRepository.findByParentId(parentUser.getId());
        List<ParentChild> parent2Relations = parentChildRepository.findByParentId(secondParent.getId());
        
        assertTrue(parent1Relations.stream().anyMatch(pc -> pc.getRelation().equals("Father")));
        assertTrue(parent2Relations.stream().anyMatch(pc -> pc.getRelation().equals("Mother")));
    }

    @Test
    public void given_adminUser_when_getAllChildren_then_returnsAllChildren() {
        // Given
        Child child1 = Child.builder()
                .firstName("Child1")
                .lastName("Test")
                .dateOfBirth(LocalDate.of(2015, 1, 1))
                .status(UserStatus.ACTIVE)
                .build();
        childRepository.save(child1);

        Child child2 = Child.builder()
                .firstName("Child2")
                .lastName("Test")
                .dateOfBirth(LocalDate.of(2016, 2, 2))
                .status(UserStatus.ACTIVE)
                .build();
        childRepository.save(child2);

        // When
        List<ChildDTO> children = childService.getAllChildren(adminUser);

        // Then
        assertNotNull(children);
        assertTrue(children.size() >= 2);
        assertTrue(children.stream().anyMatch(c -> c.getFirstName().equals("Child1")));
        assertTrue(children.stream().anyMatch(c -> c.getFirstName().equals("Child2")));
    }

    @Test
    public void given_nonAdminUser_when_getAllChildren_then_returnsNull() {
        // When
        List<ChildDTO> children = childService.getAllChildren(parentUser);

        // Then
        assertNull(children);
    }

    @Test
    public void given_adminUser_when_getChildById_then_returnsChildDTO() {
        // Given
        Child child = Child.builder()
                .firstName("Specific")
                .lastName("Child")
                .dateOfBirth(LocalDate.of(2015, 5, 15))
                .status(UserStatus.ACTIVE)
                .build();
        Child savedChild = childRepository.save(child);

        // When
        ChildDTO childDTO = childService.getChildById(adminUser, savedChild.getId());

        // Then
        assertNotNull(childDTO);
        assertEquals("Specific", childDTO.getFirstName());
        assertEquals("Child", childDTO.getLastName());
        assertEquals(savedChild.getId(), childDTO.getId());
    }

    @Test
    public void given_nonAdminUser_when_getChildById_then_returnsNull() {
        // Given
        Child child = Child.builder()
                .firstName("Protected")
                .lastName("Child")
                .dateOfBirth(LocalDate.of(2015, 5, 15))
                .status(UserStatus.ACTIVE)
                .build();
        Child savedChild = childRepository.save(child);

        // When
        ChildDTO childDTO = childService.getChildById(parentUser, savedChild.getId());

        // Then
        assertNull(childDTO);
    }

    @Test
    public void given_adminUser_when_getChildrenByParentId_then_returnsChildren() {
        // Given
        Child child = Child.builder()
                .firstName("Parent")
                .lastName("Child")
                .dateOfBirth(LocalDate.of(2015, 5, 15))
                .status(UserStatus.ACTIVE)
                .build();
        Child savedChild = childRepository.save(child);

        ParentChild parentChild = ParentChild.builder()
                .parent(parentUser)
                .child(savedChild)
                .relation("Father")
                .status(UserRoleStatus.ENABLED)
                .build();
        parentChildRepository.save(parentChild);

        // When
        List<ChildDTO> children = childService.getChildrenByParentId(adminUser, parentUser.getId());

        // Then
        assertNotNull(children);
        assertEquals(1, children.size());
        assertEquals("Parent", children.get(0).getFirstName());
        assertEquals("Child", children.get(0).getLastName());
    }

    @Test
    public void given_parentUser_when_getOwnChildren_then_returnsChildren() {
        // Given
        Child child = Child.builder()
                .firstName("Own")
                .lastName("Child")
                .dateOfBirth(LocalDate.of(2015, 5, 15))
                .status(UserStatus.ACTIVE)
                .build();
        Child savedChild = childRepository.save(child);

        ParentChild parentChild = ParentChild.builder()
                .parent(parentUser)
                .child(savedChild)
                .relation("Father")
                .status(UserRoleStatus.ENABLED)
                .build();
        parentChildRepository.save(parentChild);

        // When
        List<ChildDTO> children = childService.getChildrenByParentId(parentUser, parentUser.getId());

        // Then
        assertNotNull(children);
        assertEquals(1, children.size());
        assertEquals("Own", children.get(0).getFirstName());
    }

    @Test
    public void given_parentUser_when_getOtherParentChildren_then_returnsNull() {
        // Given
        User otherParent = User.builder()
                .email("other@test.com")
                .firstName("Other")
                .lastName("Parent")
                .password(BCrypt.hashpw("password", BCrypt.gensalt()))
                .userStatus(UserStatus.ACTIVE)
                .build();
        otherParent = userRepository.save(otherParent);

        Child child = Child.builder()
                .firstName("Other")
                .lastName("Child")
                .dateOfBirth(LocalDate.of(2015, 5, 15))
                .status(UserStatus.ACTIVE)
                .build();
        Child savedChild = childRepository.save(child);

        ParentChild parentChild = ParentChild.builder()
                .parent(otherParent)
                .child(savedChild)
                .relation("Father")
                .status(UserRoleStatus.ENABLED)
                .build();
        parentChildRepository.save(parentChild);

        // When
        List<ChildDTO> children = childService.getChildrenByParentId(parentUser, otherParent.getId());

        // Then
        assertNull(children);
    }

    @Test
    public void given_adminUser_when_getChildByNonExistentId_then_throwsException() {
        // When & Then
        assertThrows(RuntimeException.class, () -> childService.getChildById(adminUser, "non-existent-id"));
    }
} 
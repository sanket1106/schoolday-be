package com.school.feature.users.dao;

import com.school.BaseRepositoryTest;
import com.school.feature.users.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ParentChildRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private IParentChildRepository parentChildRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IChildRepository childRepository;

    @Test
    public void given_parentChild_when_save_then_returnsSavedParentChild() {
        // Given
        User parent = User.builder()
                .email("parent@example.com")
                .firstName("John")
                .lastName("Parent")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedParent = userRepository.save(parent);

        Child child = Child.builder()
                .firstName("Jane")
                .lastName("Child")
                .dateOfBirth(LocalDate.of(2015, 5, 15))
                .status(UserStatus.ACTIVE)
                .build();
        Child savedChild = childRepository.save(child);

        ParentChild parentChild = ParentChild.builder()
                .parent(savedParent)
                .child(savedChild)
                .relation("Father")
                .status(UserRoleStatus.ENABLED)
                .build();

        // When
        ParentChild savedParentChild = parentChildRepository.save(parentChild);

        // Then
        assertNotNull(savedParentChild);
        assertNotNull(savedParentChild.getId());
        assertEquals(savedParent.getId(), savedParentChild.getParent().getId());
        assertEquals(savedChild.getId(), savedParentChild.getChild().getId());
        assertEquals("Father", savedParentChild.getRelation());
        assertEquals(UserRoleStatus.ENABLED, savedParentChild.getStatus());
    }

    @Test
    public void given_savedParentChild_when_findById_then_returnsParentChild() {
        // Given
        User parent = User.builder()
                .email("findparent@example.com")
                .firstName("Find")
                .lastName("Parent")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedParent = userRepository.save(parent);

        Child child = Child.builder()
                .firstName("Find")
                .lastName("Child")
                .dateOfBirth(LocalDate.of(2016, 8, 20))
                .status(UserStatus.ACTIVE)
                .build();
        Child savedChild = childRepository.save(child);

        ParentChild parentChild = ParentChild.builder()
                .parent(savedParent)
                .child(savedChild)
                .relation("Mother")
                .status(UserRoleStatus.ENABLED)
                .build();
        ParentChild savedParentChild = parentChildRepository.save(parentChild);

        // When
        Optional<ParentChild> foundParentChild = parentChildRepository.findById(savedParentChild.getId());

        // Then
        assertTrue(foundParentChild.isPresent());
        assertEquals(savedParent.getId(), foundParentChild.get().getParent().getId());
        assertEquals(savedChild.getId(), foundParentChild.get().getChild().getId());
        assertEquals("Mother", foundParentChild.get().getRelation());
    }

    @Test
    public void given_savedParentChild_when_findByParentId_then_returnsParentChildList() {
        // Given
        User parent = User.builder()
                .email("parentbyid@example.com")
                .firstName("ParentById")
                .lastName("Test")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedParent = userRepository.save(parent);

        Child child1 = Child.builder()
                .firstName("Child1")
                .lastName("Test")
                .dateOfBirth(LocalDate.of(2014, 3, 10))
                .status(UserStatus.ACTIVE)
                .build();
        Child savedChild1 = childRepository.save(child1);

        Child child2 = Child.builder()
                .firstName("Child2")
                .lastName("Test")
                .dateOfBirth(LocalDate.of(2017, 12, 5))
                .status(UserStatus.ACTIVE)
                .build();
        Child savedChild2 = childRepository.save(child2);

        ParentChild parentChild1 = ParentChild.builder()
                .parent(savedParent)
                .child(savedChild1)
                .relation("Father")
                .status(UserRoleStatus.ENABLED)
                .build();

        ParentChild parentChild2 = ParentChild.builder()
                .parent(savedParent)
                .child(savedChild2)
                .relation("Father")
                .status(UserRoleStatus.ENABLED)
                .build();

        parentChildRepository.save(parentChild1);
        parentChildRepository.save(parentChild2);

        // When
        List<ParentChild> parentChildren = parentChildRepository.findByParentId(savedParent.getId());

        // Then
        assertEquals(2, parentChildren.size());
        assertTrue(parentChildren.stream().allMatch(pc -> pc.getParent().getId().equals(savedParent.getId())));
    }

    @Test
    public void given_savedParentChild_when_findByChildId_then_returnsParentChildList() {
        // Given
        User parent1 = User.builder()
                .email("parent1@example.com")
                .firstName("Parent1")
                .lastName("Test")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedParent1 = userRepository.save(parent1);

        User parent2 = User.builder()
                .email("parent2@example.com")
                .firstName("Parent2")
                .lastName("Test")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedParent2 = userRepository.save(parent2);

        Child child = Child.builder()
                .firstName("Shared")
                .lastName("Child")
                .dateOfBirth(LocalDate.of(2015, 7, 22))
                .status(UserStatus.ACTIVE)
                .build();
        Child savedChild = childRepository.save(child);

        ParentChild parentChild1 = ParentChild.builder()
                .parent(savedParent1)
                .child(savedChild)
                .relation("Father")
                .status(UserRoleStatus.ENABLED)
                .build();

        ParentChild parentChild2 = ParentChild.builder()
                .parent(savedParent2)
                .child(savedChild)
                .relation("Mother")
                .status(UserRoleStatus.ENABLED)
                .build();

        parentChildRepository.save(parentChild1);
        parentChildRepository.save(parentChild2);

        // When
        List<ParentChild> parentChildren = parentChildRepository.findByChildId(savedChild.getId());

        // Then
        assertEquals(2, parentChildren.size());
        assertTrue(parentChildren.stream().allMatch(pc -> pc.getChild().getId().equals(savedChild.getId())));
    }

    @Test
    public void given_savedParentChild_when_update_then_returnsUpdatedParentChild() {
        // Given
        User parent = User.builder()
                .email("updateparent@example.com")
                .firstName("Update")
                .lastName("Parent")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedParent = userRepository.save(parent);

        Child child = Child.builder()
                .firstName("Update")
                .lastName("Child")
                .dateOfBirth(LocalDate.of(2016, 4, 18))
                .status(UserStatus.ACTIVE)
                .build();
        Child savedChild = childRepository.save(child);

        ParentChild parentChild = ParentChild.builder()
                .parent(savedParent)
                .child(savedChild)
                .relation("Guardian")
                .status(UserRoleStatus.ENABLED)
                .build();
        ParentChild savedParentChild = parentChildRepository.save(parentChild);

        // When
        savedParentChild.setRelation("Step-Father");
        savedParentChild.setStatus(UserRoleStatus.DISABLED);
        ParentChild updatedParentChild = parentChildRepository.save(savedParentChild);

        // Then
        assertEquals("Step-Father", updatedParentChild.getRelation());
        assertEquals(UserRoleStatus.DISABLED, updatedParentChild.getStatus());
    }

    @Test
    public void given_savedParentChild_when_delete_then_parentChildIsRemoved() {
        // Given
        User parent = User.builder()
                .email("deleteparent@example.com")
                .firstName("Delete")
                .lastName("Parent")
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .build();
        User savedParent = userRepository.save(parent);

        Child child = Child.builder()
                .firstName("Delete")
                .lastName("Child")
                .dateOfBirth(LocalDate.of(2018, 9, 30))
                .status(UserStatus.ACTIVE)
                .build();
        Child savedChild = childRepository.save(child);

        ParentChild parentChild = ParentChild.builder()
                .parent(savedParent)
                .child(savedChild)
                .relation("Father")
                .status(UserRoleStatus.ENABLED)
                .build();
        ParentChild savedParentChild = parentChildRepository.save(parentChild);

        // When
        parentChildRepository.deleteById(savedParentChild.getId());

        // Then
        Optional<ParentChild> deletedParentChild = parentChildRepository.findById(savedParentChild.getId());
        assertFalse(deletedParentChild.isPresent());
    }

    @Test
    public void given_nonExistentParentId_when_findByParentId_then_returnsEmptyList() {
        // When
        List<ParentChild> parentChildren = parentChildRepository.findByParentId("non-existent-parent-id");

        // Then
        assertTrue(parentChildren.isEmpty());
    }

    @Test
    public void given_nonExistentChildId_when_findByChildId_then_returnsEmptyList() {
        // When
        List<ParentChild> parentChildren = parentChildRepository.findByChildId("non-existent-child-id");

        // Then
        assertTrue(parentChildren.isEmpty());
    }
} 
package com.school.feature.users.dao;

import com.school.BaseRepositoryTest;
import com.school.feature.users.entity.Child;
import com.school.feature.users.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ChildRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private IChildRepository childRepository;

    @Test
    public void given_child_when_save_then_returnsSavedChild() {
        // Given
        Child child = Child.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(2015, 5, 15))
                .status(UserStatus.ACTIVE)
                .build();

        // When
        Child savedChild = childRepository.save(child);

        // Then
        assertNotNull(savedChild);
        assertNotNull(savedChild.getId());
        assertEquals("John", savedChild.getFirstName());
        assertEquals("Doe", savedChild.getLastName());
        assertEquals(LocalDate.of(2015, 5, 15), savedChild.getDateOfBirth());
        assertEquals(UserStatus.ACTIVE, savedChild.getStatus());
    }

    @Test
    public void given_savedChild_when_findById_then_returnsChild() {
        // Given
        Child child = Child.builder()
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(2016, 8, 20))
                .status(UserStatus.ACTIVE)
                .build();
        Child savedChild = childRepository.save(child);

        // When
        Optional<Child> foundChild = childRepository.findById(savedChild.getId());

        // Then
        assertTrue(foundChild.isPresent());
        assertEquals("Jane", foundChild.get().getFirstName());
        assertEquals("Smith", foundChild.get().getLastName());
    }

    @Test
    public void given_savedChildren_when_findAll_then_returnsAllChildren() {
        // Given
        Child child1 = Child.builder()
                .firstName("Alice")
                .lastName("Johnson")
                .dateOfBirth(LocalDate.of(2014, 3, 10))
                .status(UserStatus.ACTIVE)
                .build();
        Child child2 = Child.builder()
                .firstName("Bob")
                .lastName("Wilson")
                .dateOfBirth(LocalDate.of(2017, 12, 5))
                .status(UserStatus.ACTIVE)
                .build();

        childRepository.save(child1);
        childRepository.save(child2);

        // When
        List<Child> allChildren = childRepository.findAll();

        // Then
        assertTrue(allChildren.size() >= 2);
        assertTrue(allChildren.stream().anyMatch(c -> c.getFirstName().equals("Alice")));
        assertTrue(allChildren.stream().anyMatch(c -> c.getFirstName().equals("Bob")));
    }

    @Test
    public void given_savedChild_when_update_then_returnsUpdatedChild() {
        // Given
        Child child = Child.builder()
                .firstName("Charlie")
                .lastName("Brown")
                .dateOfBirth(LocalDate.of(2015, 7, 22))
                .status(UserStatus.ACTIVE)
                .build();
        Child savedChild = childRepository.save(child);

        // When
        savedChild.setFirstName("Charles");
        savedChild.setStatus(UserStatus.DISABLED);
        Child updatedChild = childRepository.save(savedChild);

        // Then
        assertEquals("Charles", updatedChild.getFirstName());
        assertEquals(UserStatus.DISABLED, updatedChild.getStatus());
        assertEquals("Brown", updatedChild.getLastName());
    }

    @Test
    public void given_savedChild_when_delete_then_childIsRemoved() {
        // Given
        Child child = Child.builder()
                .firstName("David")
                .lastName("Miller")
                .dateOfBirth(LocalDate.of(2016, 4, 18))
                .status(UserStatus.ACTIVE)
                .build();
        Child savedChild = childRepository.save(child);

        // When
        childRepository.deleteById(savedChild.getId());

        // Then
        Optional<Child> deletedChild = childRepository.findById(savedChild.getId());
        assertFalse(deletedChild.isPresent());
    }

    @Test
    public void given_childWithInactiveStatus_when_save_then_savesSuccessfully() {
        // Given
        Child child = Child.builder()
                .firstName("Emma")
                .lastName("Davis")
                .dateOfBirth(LocalDate.of(2018, 9, 30))
                .status(UserStatus.DISABLED)
                .build();

        // When
        Child savedChild = childRepository.save(child);

        // Then
        assertNotNull(savedChild);
        assertEquals(UserStatus.DISABLED, savedChild.getStatus());
    }
} 
package com.school.feature.users.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User {

    @Id
    @UuidGenerator
    @Column(name = "id", length = 36, columnDefinition = "CHAR(36)", nullable = false)
    private String id;

    @Column(name = "first_name", columnDefinition = "VARCHAR(100)", nullable = false)
    private String firstName;

    @Column(name = "last_name", columnDefinition = "VARCHAR(100)", nullable = false)
    private String lastName;

    @Column(name = "email", columnDefinition = "VARCHAR(100)", nullable = false)
    private String email;

    @Column(name = "password", columnDefinition = "VARCHAR(255)", nullable = false)
    private String password;

    @Column(name = "created", nullable = false)
    @CreationTimestamp
    private Instant created;

    @Column(name = "updated", nullable = false)
    @UpdateTimestamp
    private Instant updated;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(50)", nullable = false)
    private UserStatus userStatus;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRole> userRoles;

    public boolean isAdmin() {
        if (getUserRoles() == null || getUserRoles().isEmpty()) {
            return false;
        }
        return getUserRoles().stream().anyMatch(userRole -> userRole.getRole().getName().equals("ADMIN"));
    }
}

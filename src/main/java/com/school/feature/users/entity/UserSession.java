package com.school.feature.users.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_session")
public class UserSession {

    @Id
    @Column(name = "token", length = 32, columnDefinition = "VARCHAR(32)", nullable = false)
    private String token;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", columnDefinition = "VARCHAR(36)", nullable = false, updatable = false)
    private User user;

    @Column(name = "active", columnDefinition = "TINYINT(1)", nullable = false)
    private boolean active;

    @Column(name = "created", nullable = false)
    @CreationTimestamp
    private Instant created;

    @Column(name = "updated", nullable = false)
    @UpdateTimestamp
    private Instant updated;
}

package com.school.feature.users.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "parent_child")
public class ParentChild {

    @Id
    @UuidGenerator
    @Column(name = "id", length = 36, columnDefinition = "CHAR(36)", nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private User parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private Child child;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(50)", nullable = false)
    private UserRoleStatus status;

    @Column(name = "relation", columnDefinition = "VARCHAR(50)", nullable = false)
    private String relation;

    @Column(name = "created", nullable = false)
    @CreationTimestamp
    private Instant created;

    @Column(name = "updated", nullable = false)
    @UpdateTimestamp
    private Instant updated;
} 
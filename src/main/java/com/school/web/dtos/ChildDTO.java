package com.school.web.dtos;

import com.school.feature.users.entity.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class ChildDTO {
    private String id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private UserStatus status;
    private Instant created;
    private Instant updated;
} 
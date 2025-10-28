package com.school.web.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AddChildDTO {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private List<ParentInfo> parents;
    
    @Data
    @Builder
    public static class ParentInfo {
        private String parentId;
        private String relation; // e.g., "FATHER", "MOTHER", "GUARDIAN"
    }
} 
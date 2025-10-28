package com.school.web.utils;

import com.school.feature.users.entity.Child;
import com.school.web.dtos.ChildDTO;

public class ChildDTOMapper {
    
    public static ChildDTO toDTO(Child entity) {
        return ChildDTO.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .dateOfBirth(entity.getDateOfBirth())
                .status(entity.getStatus())
                .created(entity.getCreated())
                .updated(entity.getUpdated())
                .build();
    }
    
    public static Child fromDTO(ChildDTO dto) {
        return Child.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .dateOfBirth(dto.getDateOfBirth())
                .status(dto.getStatus())
                .created(dto.getCreated())
                .updated(dto.getUpdated())
                .build();
    }
} 
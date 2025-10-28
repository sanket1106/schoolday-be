package com.school.web.utils;

import com.school.feature.users.entity.User;
import com.school.web.dtos.UserDTO;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

public class UserDTOMapper {


    public static User fromDTO(UserDTO dto) {
        return null;
    }

    public static UserDTO toDTO(User entity) {
        return UserDTO.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .status(entity.getUserStatus())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .build();
    }
}

package com.school.web.utils;

import com.school.feature.users.entity.UserSession;
import com.school.web.dtos.UserSessionDTO;

public class UserSessionDTOMapper {
    public static UserSession fromDTO(UserSessionDTO dto) {
        return null;
    }

    public static UserSessionDTO toDTO(UserSession entity) {
        return UserSessionDTO.builder()
                .token(entity.getToken())
                .user(UserDTOMapper.toDTO(entity.getUser()))
                .build();
    }
}

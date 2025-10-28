package com.school.web.controller;

import com.school.feature.users.dao.IRoleRepository;
import com.school.feature.users.dao.IUserRepository;
import com.school.feature.users.entity.User;
import com.school.feature.users.entity.UserRole;
import com.school.feature.users.entity.UserStatus;
import com.school.service.UserService;
import com.school.web.common.Error;
import com.school.web.common.Response;
import com.school.web.dtos.UserDTO;
import com.school.web.utils.SessionUtils;
import com.school.web.utils.UserDTOMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/parents")
@RequiredArgsConstructor
@Slf4j
public class ParentController {

    private final IRoleRepository roleRepository;
    private final IUserRepository userRepository;
    private final UserService userService;

    @PostMapping(value = "/add")
    public ResponseEntity<Response<UserDTO>> addParent(@RequestBody UserDTO parentDTO, HttpSession httpSession) {
        final var loggedInUser = SessionUtils.getUser(httpSession);

        final var parent = userService.addParent(loggedInUser, parentDTO);
        if (parent == null) {
            final var response = new Response<UserDTO>();
            response.setError(Error.builder()
                    .message("Not authorized to add a parent")
                    .build());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        final var response = new Response<UserDTO>();
        response.setData(UserDTOMapper.toDTO(parent));
        return ResponseEntity.ok(response);
    }
}

package com.school.web.controller;

import com.school.exceptions.InvalidCredentialsException;
import com.school.exceptions.ValidationException;
import com.school.feature.users.entity.UserSession;
import com.school.service.UserService;
import com.school.web.common.Error;
import com.school.web.common.Response;
import com.school.web.dtos.LogInLogOutDTO;
import com.school.web.dtos.UserDTO;
import com.school.web.dtos.UserSessionDTO;
import com.school.web.utils.UserSessionDTOMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Response<UserSessionDTO>> login(@RequestBody LogInLogOutDTO logInLogOutDTO) throws ValidationException {
        if (!StringUtils.hasText(logInLogOutDTO.getEmail()) || !StringUtils.hasText(logInLogOutDTO.getPassword())) {
            throw new ValidationException("email and password required");
        }

        final UserSession userSession;
        try {
            userSession = userService.validateUser(logInLogOutDTO);
        } catch (InvalidCredentialsException e) {
            final var response = new Response<UserSessionDTO>();
            response.setError(Error.builder()
                    .message(e.getMessage())
                    .build());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        final var response = new Response<UserSessionDTO>();
        response.setData(UserSessionDTOMapper.toDTO(userSession));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Response<String>> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String token)
            throws ValidationException {
        if (!StringUtils.hasText(token)) {
            throw new ValidationException("Token is required");
        }

        token = token.replace("Bearer ", "");
        if (userService.invalidateSession(token)) {
            final var response = new Response<String>();
            response.setData("SUCCESS");
            return ResponseEntity.ok(response);
        }

        final var response = new Response<String>();
        response.setData("Failed");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

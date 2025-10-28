package com.school.web.controller;

import com.school.feature.users.entity.User;
import com.school.service.UserService;
import com.school.web.common.Error;
import com.school.web.common.Response;
import com.school.web.dtos.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/{email}")
    public ResponseEntity user(@PathVariable(value = "email") String email) {
        System.out.println("get by email: " + email);
        final var user = userService.getUserByEmail(email);
        final var response = new Response<User>();
        if (user == null) {
            response.setError(Error.builder()
                .fieldName("email")
                .message("User not found by provided email")
                .build());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        response.setData(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity user(UserDTO userDTO) {

        return null;
    }
}

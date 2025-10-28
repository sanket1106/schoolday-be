package com.school.service;

import com.school.authentication.TokenUtils;
import com.school.exceptions.InvalidCredentialsException;
import com.school.feature.users.dao.IRoleRepository;
import com.school.feature.users.dao.IUserRepository;
import com.school.feature.users.dao.IUserRoleRepository;
import com.school.feature.users.dao.IUserSessionRepository;
import com.school.feature.users.entity.*;
import com.school.web.common.Error;
import com.school.web.common.Response;
import com.school.web.dtos.LogInLogOutDTO;
import com.school.web.dtos.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final IUserSessionRepository userSessionRepository;
    private final IUserRoleRepository userRoleRepository;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }

    public UserSession validateUser(LogInLogOutDTO logInDTO) throws InvalidCredentialsException {
        final var user = getUserByEmail(logInDTO.getEmail());
        if (user == null) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (!BCrypt.checkpw(logInDTO.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        var userSession = userSessionRepository.findByUserIdAndActive(user.getId(), true);
        if (userSession != null) {
            return userSession;
        }

        userSession = UserSession.builder()
                .active(true)
                .token(TokenUtils.generateToken(32))
                .user(user)
                .build();
        userSessionRepository.saveAndFlush(userSession);
        return userSession;
    }

    public boolean invalidateSession(String token) {
        final var userSession = userSessionRepository.findById(token).orElse(null);
        if (userSession == null) {
            log.warn("Session not found for token {}", token);
            return true;
        }

        if (!userSession.isActive()) {
            log.warn("Session is already deactivated for token {}", token);
            return true;
        }

        userSession.setActive(false);
        userSessionRepository.save(userSession);
        return true;

    }

    @Transactional
    public User addParent(User loggedInUser, UserDTO parentDTO) {
        final var user = userRepository.findById(loggedInUser.getId()).get();
        if (!user.isAdmin()) {
            return null;
        }

        final var parentRole = roleRepository.findByName("PARENT");

        final var parent = User.builder()
                .email(parentDTO.getEmail())
                .password("password")
                .userStatus(UserStatus.ACTIVE)
                .firstName(parentDTO.getFirstName())
                .lastName(parentDTO.getLastName())
                .build();
        userRepository.save(parent);

        final var userParentRole = UserRole.builder()
            .role(parentRole)
            .user(parent)
            .id(UserRoleId.builder()
                .roleId(parentRole.getId())
                .userId(parent.getId())
                .build())
            .build();
        userRoleRepository.save(userParentRole);

        return parent;
    }
}

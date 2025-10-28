package com.school.feature.users.dao;

import com.school.feature.users.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserSessionRepository extends JpaRepository<UserSession, String> {

    UserSession findByUserIdAndActive(String userId, boolean active);
}

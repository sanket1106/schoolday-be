package com.school.feature.users.dao;

import com.school.feature.users.entity.UserRole;
import com.school.feature.users.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

}

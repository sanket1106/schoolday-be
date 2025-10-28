package com.school.feature.users.dao;

import com.school.feature.users.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRoleRepository extends JpaRepository<Role, String> {

    Role findByName(String name);
}

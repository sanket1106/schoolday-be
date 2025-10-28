package com.school.feature.users.dao;

import com.school.feature.users.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IChildRepository extends JpaRepository<Child, String> {
} 
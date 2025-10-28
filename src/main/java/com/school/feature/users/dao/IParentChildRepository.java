package com.school.feature.users.dao;

import com.school.feature.users.entity.ParentChild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IParentChildRepository extends JpaRepository<ParentChild, String> {
    List<ParentChild> findByParentId(String parentId);
    List<ParentChild> findByChildId(String childId);
} 
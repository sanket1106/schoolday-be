package com.school.service;

import com.school.feature.users.dao.IChildRepository;
import com.school.feature.users.dao.IParentChildRepository;
import com.school.feature.users.dao.IUserRepository;
import com.school.feature.users.entity.Child;
import com.school.feature.users.entity.ParentChild;
import com.school.feature.users.entity.User;
import com.school.feature.users.entity.UserRoleStatus;
import com.school.feature.users.entity.UserStatus;
import com.school.web.dtos.AddChildDTO;
import com.school.web.dtos.ChildDTO;
import com.school.web.utils.ChildDTOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChildService {

    private final IChildRepository childRepository;
    private final IUserRepository userRepository;
    private final IParentChildRepository parentChildRepository;

    @Transactional
    public ChildDTO addChild(User loggedInUser, AddChildDTO addChildDTO) {
        final var user = userRepository.findById(loggedInUser.getId()).get();
        if (!user.isAdmin()) {
            return null;
        }

        // Create the child
        Child child = Child.builder()
                .firstName(addChildDTO.getFirstName())
                .lastName(addChildDTO.getLastName())
                .dateOfBirth(addChildDTO.getDateOfBirth())
                .status(UserStatus.ACTIVE)
                .build();
        
        child = childRepository.save(child);
        
        // Create parent-child relationships
        for (AddChildDTO.ParentInfo parentInfo : addChildDTO.getParents()) {
            User parent = userRepository.findById(parentInfo.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent not found with id: " + parentInfo.getParentId()));
            
            ParentChild parentChild = ParentChild.builder()
                    .parent(parent)
                    .child(child)
                    .relation(parentInfo.getRelation())
                    .status(UserRoleStatus.ENABLED)
                    .build();
            
            parentChildRepository.save(parentChild);
        }
        
        return ChildDTOMapper.toDTO(child);
    }

    public List<ChildDTO> getChildrenByParentId(User loggedInUser, String parentId) {
        final var user = userRepository.findById(loggedInUser.getId()).get();
        
        // Allow if user is admin or if the user is requesting their own children
        if (!user.isAdmin() && !user.getId().equals(parentId)) {
            return null;
        }
        
        List<ParentChild> parentChildRelations = parentChildRepository.findByParentId(parentId);
        return parentChildRelations.stream()
                .map(parentChild -> ChildDTOMapper.toDTO(parentChild.getChild()))
                .collect(Collectors.toList());
    }

    public List<ChildDTO> getAllChildren(User loggedInUser) {
        final var user = userRepository.findById(loggedInUser.getId()).get();
        if (!user.isAdmin()) {
            return null;
        }
        
        return childRepository.findAll().stream()
                .map(ChildDTOMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ChildDTO getChildById(User loggedInUser, String childId) {
        final var user = userRepository.findById(loggedInUser.getId()).get();
        if (!user.isAdmin()) {
            return null;
        }
        
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found with id: " + childId));
        return ChildDTOMapper.toDTO(child);
    }
} 
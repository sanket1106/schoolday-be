package com.school.web.controller;

import com.school.feature.users.entity.User;
import com.school.service.ChildService;
import com.school.web.common.Error;
import com.school.web.common.Response;
import com.school.web.dtos.AddChildDTO;
import com.school.web.dtos.ChildDTO;
import com.school.web.utils.SessionUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/children")
@RequiredArgsConstructor
@Slf4j
public class ChildController {

    private final ChildService childService;

    @PostMapping("/add")
    public ResponseEntity<Response<ChildDTO>> addChild(@RequestBody AddChildDTO addChildDTO, HttpSession httpSession) {
       final var loggedInUser = SessionUtils.getUser(httpSession);
            
        final var childDTO = childService.addChild(loggedInUser, addChildDTO);
        if (childDTO == null) {
            Response<ChildDTO> response = new Response<>();
            response.setError(Error.builder()
                    .message("Not authorized to add a child")
                    .build());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Response<ChildDTO> response = new Response<>();
        response.setData(childDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<Response<List<ChildDTO>>> getChildrenByParent(@PathVariable String parentId, HttpSession httpSession) {
        try {
            final var loggedInUser = SessionUtils.getUser(httpSession);
            
            final var children = childService.getChildrenByParentId(loggedInUser, parentId);
            if (children == null) {
                Response<List<ChildDTO>> response = new Response<>();
                response.setError(Error.builder()
                        .message("Not authorized to view children for this parent")
                        .build());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Response<List<ChildDTO>> response = new Response<>();
            response.setData(children);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting children for parent {}: ", parentId, e);
            Response<List<ChildDTO>> response = new Response<>();
            response.setError(Error.builder()
                    .message("Failed to get children: " + e.getMessage())
                    .build());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{childId}")
    public ResponseEntity<Response<ChildDTO>> getChildById(@PathVariable String childId, HttpSession httpSession) {
        try {
            final var loggedInUser = SessionUtils.getUser(httpSession);
            
            final var childDTO = childService.getChildById(loggedInUser, childId);
            if (childDTO == null) {
                Response<ChildDTO> response = new Response<>();
                response.setError(Error.builder()
                        .message("Not authorized to view child details")
                        .build());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Response<ChildDTO> response = new Response<>();
            response.setData(childDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting child {}: ", childId, e);
            Response<ChildDTO> response = new Response<>();
            response.setError(Error.builder()
                    .message("Child not found: " + e.getMessage())
                    .build());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Response<List<ChildDTO>>> getAllChildren(HttpSession httpSession) {
        try {
            final var loggedInUser = SessionUtils.getUser(httpSession);
            
            final var children = childService.getAllChildren(loggedInUser);
            if (children == null) {
                Response<List<ChildDTO>> response = new Response<>();
                response.setError(Error.builder()
                        .message("Not authorized to view all children")
                        .build());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Response<List<ChildDTO>> response = new Response<>();
            response.setData(children);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting all children: ", e);
            Response<List<ChildDTO>> response = new Response<>();
            response.setError(Error.builder()
                    .message("Failed to get children: " + e.getMessage())
                    .build());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
} 
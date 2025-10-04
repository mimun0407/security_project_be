package com.example.demo.controller;

import com.example.demo.entity.UserEntity;
import com.example.demo.model.CreateRequest;
import com.example.demo.model.UserDetailResponse;
import com.example.demo.model.UserPageResponse;
import com.example.demo.model.UserUpdateRequest;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public void save(
            @RequestPart("user") CreateRequest user,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        userService.save(user, image);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    public void delete (@PathVariable String id) {
       userService.delete(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<UserPageResponse> findAll(@ParameterObject Pageable pageable) {
        return userService.findAll(pageable);
    }

    @GetMapping("{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    public UserDetailResponse findById(@PathVariable String username) {
        return userService.findByUsername(username);
    }

    @PutMapping("{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    public void update(@PathVariable String username,
                       @RequestPart("user") UserUpdateRequest request,
                       @RequestPart(value = "image", required = false) MultipartFile image) {
        userService.update(username, request, image);
    }
}
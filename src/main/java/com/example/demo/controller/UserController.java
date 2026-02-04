package com.example.demo.controller;

import com.example.demo.entity.UserEntity;
import com.example.demo.model.*;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @GetMapping("{email}")
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    public UserDetailResponse findByEmail(@PathVariable String email) {
        return userService.findByEmail(email);
    }

    @PutMapping("{email}")
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    public void update(@PathVariable String email,
                       @RequestPart("user") UserUpdateRequest request,
                       @RequestPart(value = "image", required = false) MultipartFile image) {
        userService.update(email, request, image);
    }
    @GetMapping("/suggestions")
    public ResponseEntity<List<UserSuggestResponse>> getSuggestions() {
        // Lấy username từ SecurityContext (đã giải mã từ JWT)
        String currentUserEmail = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        List<UserSuggestResponse> suggestions = userService.getSuggestions(currentUserEmail);
        return ResponseEntity.ok(suggestions);
    }
}
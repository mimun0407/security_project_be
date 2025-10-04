package com.example.demo.model;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String name;
    private String username;
    private String email;
    private Boolean isActive;
    private String password;
}
package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class RefreshTokenEntity {
    @Id
    @Column( columnDefinition = "TEXT", nullable = false)
    private String token;

    private String userId;

    private Date expiresTime;
}

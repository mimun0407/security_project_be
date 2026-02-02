package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostEntity extends BaseEntity {
    @ManyToOne
    private UserEntity userId;

    private int likes;

    private String content;

    private String musicLink;

    private String imageUrl;

    private String imageHash;
}

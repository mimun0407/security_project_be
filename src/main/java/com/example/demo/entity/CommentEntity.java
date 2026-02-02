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
public class CommentEntity extends BaseEntity {
    @ManyToOne
    private PostEntity postId;
    @ManyToOne
    private UserEntity userId;
    private String content;
}

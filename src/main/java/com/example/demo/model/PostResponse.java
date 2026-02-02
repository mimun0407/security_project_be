package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private String id;
    private String content;
    private String musicLink;
    private String imageUrl;
    private int likes;

    private String authorId;
    private String authorName;
    private String authorAvatar;
}
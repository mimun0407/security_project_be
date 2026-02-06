package com.example.demo.controller;

import com.example.demo.model.user.CreatePostRequest;
import com.example.demo.model.PostResponse;
import com.example.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createPost(
            @ModelAttribute CreatePostRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "music", required = false) MultipartFile musicFile) { // Thêm cái này

        postService.createPost(request, image, musicFile);
        return ResponseEntity.ok("Tạo bài viết và tải nhạc thành công!");
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable String id) {
        return ResponseEntity.ok(postService.findById(id));
    }
    @GetMapping("/my")
    public Page<PostResponse> myPosts(Pageable pageable) {
        return postService.userPosts(pageable);
    }
}

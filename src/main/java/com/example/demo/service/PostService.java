package com.example.demo.service;

import com.example.demo.common.AppException;
import com.example.demo.common.HashUtil;
import com.example.demo.entity.PostEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.user.CreatePostRequest;
import com.example.demo.model.PostResponse;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private static final String POST_IMAGE_PATH = "C:/image-for-porject/";

    public Page<PostResponse> userPosts(Pageable pageable) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Jwt jwt = (Jwt) authentication.getPrincipal();

        String email = jwt.getSubject();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "User not found"));

        Page<PostEntity> posts =
                postRepository.findByUserId(user, pageable);

        return posts.map(this::mapToResponse);
    }

    public void createPost(CreatePostRequest request, MultipartFile image, MultipartFile musicFile) {
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NF_001", "User not found"));

        PostEntity post = new PostEntity();
        post.setContent(request.getContent());
        post.setUserId(user);
        post.setLikes(0);

        // --- XỬ LÝ LƯU ẢNH ---
        if (image != null && !image.isEmpty()) {
            try {
                String imgFileName = System.currentTimeMillis() + "_img_" + image.getOriginalFilename();
                File imgDir = new File(POST_IMAGE_PATH);
                if (!imgDir.exists()) imgDir.mkdirs();

                File dest = new File(imgDir, imgFileName);
                image.transferTo(dest);
                String hash = HashUtil.sha256Hex(dest);

                post.setImageUrl("/public/" + imgFileName);
                post.setImageHash(hash);
            } catch (Exception e) {
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_ERR_001", "Error saving image: " + e.getMessage());
            }
        }

        // --- XỬ LÝ LƯU NHẠC ---
        if (musicFile != null && !musicFile.isEmpty()) {
            try {
                // Tạo tên file nhạc duy nhất
                String musicFileName = System.currentTimeMillis() + "_music_" + musicFile.getOriginalFilename();

                File musicDir = new File(POST_IMAGE_PATH);
                if (!musicDir.exists()) musicDir.mkdirs();

                File destMusic = new File(musicDir, musicFileName);
                musicFile.transferTo(destMusic); // Lưu file vào ổ cứng

                post.setMusicLink("/public/" + musicFileName);

            } catch (IOException e) {
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_ERR_002", "Error saving music file: " + e.getMessage());
            }
        }
        postRepository.save(post);
    }

    public Page<PostResponse> findAll(Pageable pageable) {
        Page<PostEntity> posts = postRepository.findAll(pageable);

        // Map từ Entity sang Response
        return posts.map(this::mapToResponse);
    }

    public PostResponse findById(String id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "POST_NF_001", "Post not found"));

        validateImageIntegrity(post);

        return mapToResponse(post);
    }

    private PostResponse mapToResponse(PostEntity post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setMusicLink(post.getMusicLink());
        response.setImageUrl(post.getImageUrl());
        response.setLikes(post.getLikes());

        if (post.getUserId() != null) {
            UserEntity author = post.getUserId();
            response.setAuthorId(author.getId());
            response.setAuthorName(author.getName());
            response.setAuthorAvatar(author.getImageUrl());
        }

        return response;
    }

    // Hàm kiểm tra hash ảnh
    private void validateImageIntegrity(PostEntity post) {
        if (post.getImageUrl() != null && post.getImageHash() != null) {
            String fileName = post.getImageUrl().replace("/public/", "");
            File file = new File(POST_IMAGE_PATH + fileName);

            if (file.exists()) {
                try {
                    String currentHash = HashUtil.sha256Hex(file);
                    if (!currentHash.equals(post.getImageHash())) {
                        throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_INTEGRITY_ERR", "Image file integrity check failed (Hash mismatch)");
                    }
                } catch (Exception e) {
                    if (e instanceof AppException) throw (AppException) e;
                    throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_HASH_ERR", "Error calculating file hash: " + e.getMessage());
                }
            }
        }
    }
}
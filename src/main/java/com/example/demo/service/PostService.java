package com.example.demo.service;

import com.example.demo.common.HashUtil;
import com.example.demo.entity.PostEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.CreatePostRequest;
import com.example.demo.model.PostResponse;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // Đường dẫn lưu ảnh bài viết (Khác với ảnh user để dễ quản lý)
    private static final String POST_IMAGE_PATH = "C:/image-for-porject/";

    public void createPost(CreatePostRequest request, MultipartFile image, MultipartFile musicFile) {
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        PostEntity post = new PostEntity();
        post.setContent(request.getContent());
        post.setUserId(user);
        post.setLikes(0);

        // --- XỬ LÝ LƯU ẢNH (Giữ nguyên) ---
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
                throw new RuntimeException("Lỗi lưu ảnh: " + e.getMessage());
            }
        }

        // --- XỬ LÝ LƯU NHẠC (Mới thêm) ---
        if (musicFile != null && !musicFile.isEmpty()) {
            try {
                // Tạo tên file nhạc duy nhất
                String musicFileName = System.currentTimeMillis() + "_music_" + musicFile.getOriginalFilename();

                File musicDir = new File(POST_IMAGE_PATH);
                if (!musicDir.exists()) musicDir.mkdirs();

                File destMusic = new File(musicDir, musicFileName);
                musicFile.transferTo(destMusic); // Lưu file vào ổ cứng

                // Lưu đường dẫn vào DB để frontend play
                post.setMusicLink("/public/" + musicFileName);

            } catch (IOException e) {
                throw new RuntimeException("Lỗi lưu file nhạc: " + e.getMessage());
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));

        // Kiểm tra Hash ảnh (Chống giả mạo file)
        validateImageIntegrity(post);

        return mapToResponse(post);
    }

    // --- Hàm phụ trợ ---

    // Hàm map dữ liệu: Chỗ này trả lời câu hỏi "lấy từ userID rồi mà" của bạn
    private PostResponse mapToResponse(PostEntity post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId()); // Giả sử BaseEntity có getId()
        response.setContent(post.getContent());
        response.setMusicLink(post.getMusicLink());
        response.setImageUrl(post.getImageUrl());
        response.setLikes(post.getLikes());

        // LẤY THÔNG TIN TÁC GIẢ TỪ RELATIONSHIP
        if (post.getUserId() != null) {
            UserEntity author = post.getUserId(); // getUserId() trả về object UserEntity
            response.setAuthorId(author.getId());
            response.setAuthorName(author.getName());
            response.setAuthorAvatar(author.getImageUrl());
        }

        return response;
    }

    // Hàm kiểm tra hash ảnh (Tách ra cho gọn)
    private void validateImageIntegrity(PostEntity post) {
        if (post.getImageUrl() != null && post.getImageHash() != null) {
            String fileName = post.getImageUrl().replace("/public/", "");
            File file = new File(POST_IMAGE_PATH + fileName);

            if (file.exists()) {
                try {
                    String currentHash = HashUtil.sha256Hex(file);
                    if (!currentHash.equals(post.getImageHash())) {
                        throw new RuntimeException("Ảnh bài viết bị lỗi hoặc đã bị thay đổi (Hash mismatch)");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Lỗi kiểm tra hash: " + e.getMessage());
                }
            }
        }
    }
}

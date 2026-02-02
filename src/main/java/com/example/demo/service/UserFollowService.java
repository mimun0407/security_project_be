package com.example.demo.service;

import com.example.demo.entity.UserEntity;
import com.example.demo.entity.UserFollow;
import com.example.demo.repository.UserFollowRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFollowService {

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;

    @Transactional
    public void followUser(String followerId, String followingId) {
        // 1. Validate: Không tự follow chính mình
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Không thể tự theo dõi chính mình.");
        }

        // 2. Tìm User trong DB (ID là String)
        UserEntity follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Người theo dõi không tồn tại"));

        UserEntity following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("Người được theo dõi không tồn tại"));

        // 3. Kiểm tra đã follow chưa
        if (userFollowRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new IllegalStateException("Đã theo dõi người dùng này rồi.");
        }

        // 4. Lưu vào DB
        UserFollow userFollow = new UserFollow();
        userFollow.setFollower(follower);
        userFollow.setFollowing(following);

        userFollowRepository.save(userFollow);
    }

    @Transactional
    public void unfollowUser(String followerId, String followingId) {
        UserEntity follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Người theo dõi không tồn tại"));

        UserEntity following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("Người được theo dõi không tồn tại"));

        // Tìm record để xóa
        UserFollow userFollow = userFollowRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new RuntimeException("Bạn chưa theo dõi người dùng này."));

        userFollowRepository.delete(userFollow);
    }
}

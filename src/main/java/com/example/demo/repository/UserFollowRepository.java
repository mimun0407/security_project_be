package com.example.demo.repository;

import com.example.demo.entity.UserEntity;
import com.example.demo.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFollowRepository extends JpaRepository<UserFollow, String> {
    boolean existsByFollowerAndFollowing(UserEntity follower, UserEntity following);

    Optional<UserFollow> findByFollowerAndFollowing(UserEntity follower, UserEntity following);

    long countByFollowing(UserEntity following);
    long countByFollower(UserEntity follower);
}

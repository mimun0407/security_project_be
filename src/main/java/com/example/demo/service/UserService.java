package com.example.demo.service;

import com.example.demo.common.HashUtil;
import com.example.demo.entity.RoleEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.*;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private static final String IMAGE_PATH = "C:/image-for-porject/";

    public void save(CreateRequest createRequest, MultipartFile image) {
        if (userRepository.existsById(createRequest.getUsername())) {
            throw new RuntimeException("Bị trùng rồi");
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setName(createRequest.getName());
        userEntity.setUsername(createRequest.getUsername());
        userEntity.setEmail(createRequest.getEmail());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        userEntity.setPassword(encoder.encode(createRequest.getPassword()));
        Set<RoleEntity> roles = new HashSet<>();
        for (String roleName : createRequest.getRoles()) {
            RoleEntity role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            roles.add(role);
        }
        userEntity.setRoles(roles);
        if (image != null && !image.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();

            File dir = new File(IMAGE_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File dest = new File(dir, fileName);
            try {
                image.transferTo(dest);
                String hash = HashUtil.sha256Hex(dest);
                userEntity.setImageUrl("/public/" + fileName);
                userEntity.setImageHash(hash);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        userRepository.save(userEntity);
    }

    public Page<UserPageResponse> findAll(Pageable pageable) {
        Page<UserEntity> userEntityPage = userRepository.findAll(pageable);

        return userEntityPage.map(userEntity -> {
            UserPageResponse response = new UserPageResponse();
            response.setName(userEntity.getName());
            response.setUsername(userEntity.getUsername());
            response.setEmail(userEntity.getEmail());
            response.setImageUrl(userEntity.getImageUrl());

            Set<String> roles = userEntity.getRoles()
                    .stream()
                    .map(RoleEntity::getName) // lấy name
                    .collect(Collectors.toSet());

            response.setRoles(roles);

            return response;
        });
    }

    public UserDetailResponse findByUsername(String username) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy username này"));

        if (userEntity.getImageUrl() != null && userEntity.getImageHash() != null) {
            File file = new File(IMAGE_PATH + userEntity.getImageUrl().replace("/public/", ""));
            if (file.exists()) {
                try {
                    String currentHash = HashUtil.sha256Hex(file);
                    if (!currentHash.equals(userEntity.getImageHash())) {
                        throw new RuntimeException("Ảnh của user đã bị thay đổi hoặc hỏng (hash mismatch).");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Không thể kiểm tra hash ảnh: " + e.getMessage());
                }
            } else {
                throw new RuntimeException("Ảnh không tồn tại trên hệ thống.");
            }
        }

        Set<String> roles = userEntity.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());

        return new UserDetailResponse(
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                userEntity.getImageUrl(),
                roles
        );
    }

    public void update(String username, UserUpdateRequest request, MultipartFile newImage) {
        UserEntity existingUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với username: " + username));

        existingUser.setName(request.getName());
        existingUser.setUsername(request.getUsername());
        existingUser.setEmail(request.getEmail());
        existingUser.setIsActive(request.getIsActive());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
            existingUser.setPassword(encoder.encode(request.getPassword()));
        }

        if (newImage != null && !newImage.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + newImage.getOriginalFilename();
            File dir = new File(IMAGE_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File dest = new File(dir, fileName);
            try {
                newImage.transferTo(dest);
                String hash = HashUtil.sha256Hex(dest);
                existingUser.setImageUrl("/public/" + fileName);
                existingUser.setImageHash(hash);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu ảnh mới: " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException("Không thể tạo hash ảnh mới: " + e.getMessage());
            }
        } else {
            if (existingUser.getImageUrl() != null && existingUser.getImageHash() != null) {
                File file = new File(IMAGE_PATH + existingUser.getImageUrl().replace("/public/", ""));
                if (file.exists()) {
                    try {
                        String currentHash = HashUtil.sha256Hex(file);
                        if (!currentHash.equals(existingUser.getImageHash())) {
                            throw new RuntimeException("Ảnh hiện tại đã bị thay đổi hoặc hỏng (hash mismatch).");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Không thể kiểm tra hash ảnh hiện tại: " + e.getMessage());
                    }
                } else {
                    throw new RuntimeException("Ảnh hiện tại không tồn tại trên hệ thống.");
                }
            }
        }

        userRepository.save(existingUser);
    }
    public List<UserSuggestResponse> getSuggestions(String currentUsername) {
        UserEntity currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        List<UserEntity> suggestedUsers = userRepository.findSuggestedUsers(
                currentUser.getId(),
                PageRequest.of(0, 7)
        );

        return suggestedUsers.stream().map(user -> {
            UserSuggestResponse response = new UserSuggestResponse();
            response.setUserId(user.getId());
            response.setName(user.getName());
            response.setImageUrl(user.getImageUrl());
            return response;
        }).collect(Collectors.toList());

    }

    public void delete(String id) {
        userRepository.deleteById(id);
    }
}

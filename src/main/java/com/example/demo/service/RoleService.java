package com.example.demo.service;

import com.example.demo.entity.RoleEntity;
import com.example.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    public void save(RoleEntity role) {
        roleRepository.save(role);
    }
    public Page<RoleEntity> findAll(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }
    public RoleEntity findById(String id) {
        return roleRepository.findById(id).orElseThrow(()->new RuntimeException("ko tim thay id nay"));
    }
}

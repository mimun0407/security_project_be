package com.example.demo.repository;

import com.example.demo.entity.LogCrud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogCRUDRepository extends JpaRepository<LogCrud,String> {
}

package com.kwnam.workreport.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kwnam.workreport.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
package com.library.repository;

import com.library.enums.UserType;
import com.library.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;import java.util.Optional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByUserTypeOrderByCreatedAtDesc(UserType userType);
     Long countByActiveTrue();
    Long countByActiveFalse();
    Long countByCreatedAtAfter(LocalDateTime date);
}
// src/main/java/com/example/recruitmenttrainingsystem/repository/UserRepository.java
package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // ⭐ NEW: Lấy tất cả user đang active theo role
    List<User> findByRole_RoleNameAndStatusTrue(String roleName);
}

package com.example.recruitmenttrainingsystem.dto;

import com.example.recruitmenttrainingsystem.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class UserManagementDTO {
    private UUID id;
    private String fullName;
    private String email;
    private String currentRoleName;
    private boolean status;
    private Instant createdAt;
    // Constructor để chuyển từ Entity -> DTO
    public UserManagementDTO(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();

        // Xử lý an toàn trường hợp user chưa có role (role == null)
        if (user.getRole() != null) {
            this.currentRoleName = user.getRole().getRoleName();
        } else {
            this.currentRoleName = null; // Hoặc "Chưa gán"
        }
        this.status = user.isStatus();
        this.createdAt = user.getCreatedAt();
    }
}
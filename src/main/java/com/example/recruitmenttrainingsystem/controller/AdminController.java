// Tên file: controller/AdminController.java
package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.AssignRoleRequest;
import com.example.recruitmenttrainingsystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.recruitmenttrainingsystem.dto.UserManagementDTO;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;


    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> assignRole(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRoleRequest request,
            Authentication auth // Dùng để lấy thông tin admin đang thực hiện
    ) {
        // auth.getName() sẽ trả về email của SUPER_ADMIN (đã được set trong JwtFilter)
        userService.assignRole(userId, request, auth.getName());
        return ResponseEntity.ok("Cập nhật role thành công.");
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserManagementDTO>> getAllUsers() {
        List<UserManagementDTO> users = userService.getAllUsersForAdmin();
        return ResponseEntity.ok(users);
    }
}
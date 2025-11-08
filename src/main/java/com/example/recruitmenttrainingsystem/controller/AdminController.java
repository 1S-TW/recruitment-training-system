// Tên file: controller/AdminController.java
package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.AssignRoleRequest;
import com.example.recruitmenttrainingsystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    /**
     * Endpoint cho SUPER_ADMIN thay đổi role của bất kỳ user nào khác.
     */
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
}
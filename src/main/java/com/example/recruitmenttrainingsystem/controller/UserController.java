package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.ChangePasswordRequest;
import com.example.recruitmenttrainingsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            Authentication auth,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(auth.getName(), request);
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }
}
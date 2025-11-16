package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.*;
import com.example.recruitmenttrainingsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.ok("Đăng ký thành công. Vui lòng kiểm tra email để xác thực.");
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String token) {
        userService.verifyEmail(token);
        return ResponseEntity.ok("Xác thực email thành công. Bạn có thể đăng nhập.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(userService.login(req));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgot(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request);
        return ResponseEntity.ok("Đã gửi email khôi phục mật khẩu.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> reset(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok("Đổi mật khẩu thành công.");
    }

}
package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.*;
import com.example.recruitmenttrainingsystem.entity.Role;
import com.example.recruitmenttrainingsystem.entity.User;
import com.example.recruitmenttrainingsystem.service.EmailService;
import com.example.recruitmenttrainingsystem.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        if (jwtSecret == null) {
            throw new IllegalStateException("JWT secret is not configured in application.properties!");
        }
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (!isValidPassword(request.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu phải có ≥8 ký tự, 1 chữ hoa và 1 ký tự đặc biệt");
        }

        if (userService.userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email đã tồn tại");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setTokenExpiry(LocalDateTime.now().plusHours(24));
        userService.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
        return ResponseEntity.ok("Đăng ký thành công! Vui lòng kiểm tra email để xác thực.");
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            User user = userService.findByVerificationToken(token);
            if (user.getTokenExpiry() == null || user.getTokenExpiry().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body("Token đã hết hạn hoặc không hợp lệ");
            }
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            user.setTokenExpiry(null);
            userService.save(user);
            return ResponseEntity.ok("Xác thực thành công! Vui lòng đăng nhập.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Token không hợp lệ hoặc đã hết hạn");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.findByEmail(request.getEmail());
            if (!user.isEmailVerified() || !user.isStatus() ||
                    !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                return ResponseEntity.badRequest().body("Email chưa xác thực hoặc thông tin đăng nhập sai");
            }

            String jwt = generateToken(user.getEmail(), user.getRole().getRoleName());
            return ResponseEntity.ok(new LoginResponse(jwt, user.getRole().getRoleName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Email không tồn tại hoặc lỗi đăng nhập");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            User user = userService.findByEmail(request.getEmail());
            user.setVerificationToken(UUID.randomUUID().toString());
            user.setTokenExpiry(LocalDateTime.now().plusHours(1));
            userService.save(user);
            emailService.sendResetPasswordEmail(user.getEmail(), user.getVerificationToken());
            return ResponseEntity.ok("Email đặt lại mật khẩu đã được gửi");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Email không tồn tại");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (!isValidPassword(request.getNewPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu mới không hợp lệ");
        }
        try {
            User user = userService.findByVerificationToken(request.getToken());
            if (user.getTokenExpiry() == null || user.getTokenExpiry().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body("Token đã hết hạn hoặc không hợp lệ");
            }
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            user.setVerificationToken(null);
            user.setTokenExpiry(null);
            userService.save(user);
            return ResponseEntity.ok("Đặt lại mật khẩu thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Token không hợp lệ hoặc lỗi xử lý");
        }
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<?> oauth2Success(OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            return ResponseEntity.badRequest().body("Không có thông tin xác thực từ Google. Vui lòng thử lại qua /oauth2/authorization/google.");
        }
        String email = authentication.getPrincipal().getAttribute("email");
        String name = authentication.getPrincipal().getAttribute("name");
        if (email == null) {
            return ResponseEntity.badRequest().body("Thông tin Google OAuth không hợp lệ");
        }
        name = name != null ? name : "Google User";

        try {
            User user = userService.findByEmail(email);
            String jwt = generateToken(user.getEmail(), user.getRole().getRoleName());
            return ResponseEntity.ok(new LoginResponse(jwt, user.getRole().getRoleName()));
        } catch (Exception e) {
            User user = new User();
            user.setFullName(name);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setEmailVerified(true);
            userService.save(user); // Role HR mặc định sẽ được gán trong UserService.save()
            String jwt = generateToken(user.getEmail(), user.getRole().getRoleName());
            return ResponseEntity.ok(new LoginResponse(jwt, user.getRole().getRoleName()));
        }
    }

    private String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", email);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();
    }

    private boolean isValidPassword(String password) {
        return password != null && password.matches("^(?=.*[A-Z])(?=.*[!@#$$ %^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,} $$");
    }
}
package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.*;
import com.example.recruitmenttrainingsystem.entity.*;
import com.example.recruitmenttrainingsystem.exception.CustomException;
import com.example.recruitmenttrainingsystem.repository.*;
import com.example.recruitmenttrainingsystem.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    // REGISTER
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("Email đã tồn tại");
        }

        Role hrRole = roleRepository.findByRoleName("HR")
                .orElseThrow(() -> new CustomException("Không tìm thấy role HR"));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .status(true)
                .createdAt(Instant.now())
                .role(hrRole)
                .build();

        userRepository.save(user);

        String token = UUID.randomUUID().toString();

        VerificationToken vt = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        verificationTokenRepository.save(vt);

        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    // VERIFY
    public void verifyEmail(String token) {
        VerificationToken vt = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new CustomException("Token không hợp lệ"));

        if (vt.isExpired()) {
            throw new CustomException("Token hết hạn");
        }

        User u = vt.getUser();
        u.setEmailVerified(true);
        userRepository.save(u);

        verificationTokenRepository.delete(vt);
    }

    // LOGIN
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("Email không tồn tại"));

        if (!user.isEmailVerified()) {
            throw new CustomException("Email chưa xác thực");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new CustomException("Sai mật khẩu");
        }

        String role = user.getRole().getRoleName();

        String token = jwtUtil.generateToken(user.getEmail(), role);

        return new LoginResponse(token, role, user.getFullName());
    }
}

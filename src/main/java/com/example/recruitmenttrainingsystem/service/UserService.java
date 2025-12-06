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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    // REGISTER
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("Email đã tồn tại");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .status(true)
                .createdAt(Instant.now())
                .build();

        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken vt = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        verificationTokenRepository.save(vt);
        emailService.sendVerificationEmail(user.getEmail(), token, user.getFullName());
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

    // LOGIN - cho phép tài khoản đã khóa vẫn đăng nhập, để FE hiển thị modal khóa tài khoản
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("Email không tồn tại"));

        // 1. Kiểm tra xác thực email
        if (!user.isEmailVerified()) {
            throw new CustomException("Email chưa xác thực");
        }

        // 2. KHÔNG chặn theo status nữa
        // if (!user.isStatus()) {
        //     throw new CustomException("Tài khoản đã bị khóa. Vui lòng liên hệ Admin.");
        // }

        // 3. Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new CustomException("Sai mật khẩu");
        }

        String role = null;
        if (user.getRole() != null) {
            role = user.getRole().getRoleName();
        }

        String token = jwtUtil.generateToken(user.getEmail(), role);

        // ✅ Trả thêm status để FE dùng cho AccountLockedModal
        return new LoginResponse(
                token,
                role,
                user.getFullName(),
                user.getId(),
                user.isStatus()
        );
    }

    // Forgot pasword
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("Email không tồn tại"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken prt = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(1800))
                .build();

        passwordResetTokenRepository.save(prt);
        emailService.sendResetPasswordEmail(user.getEmail(), token);
    }

    // Reset password
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new CustomException("Token không hợp lệ"));

        if (prt.isExpired()) {
            throw new CustomException("Token đã hết hạn");
        }

        User user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.delete(prt);
    }

    // Change password
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Không tìm thấy user"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new CustomException("Mật khẩu cũ không đúng");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // (Admin) Get all users
    public List<UserManagementDTO> getAllUsersForAdmin() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserManagementDTO::new)
                .collect(Collectors.toList());
    }

    // PHÂN QUYỀN (ADMIN)
    public void assignRole(UUID userId, AssignRoleRequest request, String adminEmail) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Không tìm thấy user với ID: " + userId));

        User adminUser = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new CustomException("Lỗi: Không tìm thấy admin user"));

        if (adminUser.getId().equals(targetUser.getId())) {
            throw new CustomException("Admin không thể tự thay đổi role hoặc trạng thái của chính mình.");
        }

        // Cập nhật Role
        String newRoleName = request.getRoleName();
        if (newRoleName == null || newRoleName.trim().isEmpty()) {
            targetUser.setRole(null);
        } else {
            Role newRole = roleRepository.findByRoleName(newRoleName)
                    .orElseThrow(() -> new CustomException("Không tìm thấy role: " + newRoleName));
            targetUser.setRole(newRole);
        }

        // Cập nhật Status
        if (request.getStatus() != null) {
            targetUser.setStatus(request.getStatus());
        }

        userRepository.save(targetUser);
    }
}

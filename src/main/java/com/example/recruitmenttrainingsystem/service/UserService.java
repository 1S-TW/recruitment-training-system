package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.*;
import com.example.recruitmenttrainingsystem.entity.*;
import com.example.recruitmenttrainingsystem.exception.CustomException;
import com.example.recruitmenttrainingsystem.repository.*;
import com.example.recruitmenttrainingsystem.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
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

        String role = null;
        if (user.getRole() != null) {
            role = user.getRole().getRoleName();
        }

        String token = jwtUtil.generateToken(user.getEmail(), role);

        return new LoginResponse(token, role, user.getFullName(), user.getId());
    }
    // Forgot pasword
    public void forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("Email không tồn tại"));

        String token = UUID.randomUUID().toString();

        PasswordResetToken prt = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(1800)) // 30 phút
                .build();

        passwordResetTokenRepository.save(prt);

        emailService.sendResetPasswordEmail(user.getEmail(), token);
    }
    //reset password
    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetToken prt = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new CustomException("Token không hợp lệ"));

        if (prt.isExpired()) {
            throw new CustomException("Token đã hết hạn");
        }

        User user = prt.getUser();

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Xoá token sau khi dùng
        passwordResetTokenRepository.delete(prt);
    }
    // change pasword
    public void changePassword(String email, ChangePasswordRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Không tìm thấy user"));

        // 1. Check mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new CustomException("Mật khẩu cũ không đúng");
        }

        // 2. Set mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    // (admin) lay tat ca tai khoan
    public List<UserManagementDTO> getAllUsersForAdmin() {
        // Dùng hàm findAll() có sẵn của JpaRepository
        List<User> users = userRepository.findAll();

        // Chuyển User (Entity) sang UserManagementDTO
        return users.stream()
                .map(UserManagementDTO::new)
                .collect(Collectors.toList());
    }
    // phan quyen ( ADMIN )
    public void assignRole(UUID userId, AssignRoleRequest request, String adminEmail) {

        // 1. Tìm user mục tiêu
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Không tìm thấy user với ID: " + userId));

        // --- BẮT ĐẦU SỬA ---
        String newRoleName = request.getRoleName();
        Role newRole = null; // 1. Khởi tạo role là null

        // 2. Chỉ tìm role nếu newRoleName không rỗng
        if (newRoleName != null && !newRoleName.trim().isEmpty()) {
            newRole = roleRepository.findByRoleName(newRoleName)
                    .orElseThrow(() -> new CustomException("Không tìm thấy role: " + newRoleName));
        }
        // --- KẾT THÚC SỬA ---

        // 3. (Rất quan trọng) Kiểm tra admin có tự đổi role của chính mình không
        User adminUser = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new CustomException("Lỗi: Không tìm thấy admin user"));

        if (adminUser.getId().equals(targetUser.getId())) {
            throw new CustomException("Admin không thể tự thay đổi role của chính mình.");
        }

        // 4. Kiểm tra xem role có thực sự thay đổi không
        String originalRoleName = (targetUser.getRole() != null) ? targetUser.getRole().getRoleName() : null;
        if ( (originalRoleName == null && newRoleName == null) ||
                (originalRoleName != null && originalRoleName.equals(newRoleName)) ) {
            throw new CustomException("User đã có role này rồi.");
        }

        // 5. Cập nhật và lưu
        targetUser.setRole(newRole); // Gán role (có thể là null)
        userRepository.save(targetUser);
    }

}

package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.entity.User;
import com.example.recruitmenttrainingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // ✅ Kiểm tra status
        boolean enabled = user.isStatus() && user.isEmailVerified();

        // ✅ Xử lý Role (tránh lỗi Null khi user chưa có role)
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName()));
        } else {
            // Nếu không có role, gán quyền mặc định thấp nhất hoặc để trống
            // authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        System.out.println("✅ User login: " + user.getEmail()
                + " | Role: " + (user.getRole() != null ? user.getRole().getRoleName() : "NONE"));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                enabled,                  // isEnabled()
                true,                     // accountNonExpired
                true,                     // credentialsNonExpired
                true,                     // accountNonLocked
                authorities
        );
    }
}